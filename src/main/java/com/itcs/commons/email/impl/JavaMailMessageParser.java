/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import com.itcs.commons.email.EmailMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;
import javax.mail.util.SharedByteArrayInputStream;
import org.apache.commons.mail.EmailAttachment;

/**
 *
 * @author Jonathan
 */
public class JavaMailMessageParser {

    public static final String EXTRACT_MAIL_REGEXP = "(.*?)<([^>]+)>\\s*,?";
    public static final String MAIL_VALIDATOR_REGEXP = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";

    public static void main(String[] args) {
        JavaMailMessageParser jmmp = new JavaMailMessageParser();
        String emailAdress = "\"pico pal que lee\" <daniel.sanchez@e-puntaarenas.cl>\"";
        System.out.println("Es un correo valido: " + emailAdress.matches(MAIL_VALIDATOR_REGEXP));
        Pattern p = Pattern.compile(EXTRACT_MAIL_REGEXP, Pattern.DOTALL);
        Matcher matcher = p.matcher(emailAdress);

        if (matcher.find()) {
            // filter newline
            String name = matcher.group(1).replaceAll("[\\n\\r]+", "");
            String email = matcher.group(2).replaceAll("[\\n\\r]+", "");
            System.out.println("name: " + name);
            System.out.println("email: " + email);
        }
    }

    /**
     *
     * @param message
     * @return
     * @throws MessagingException
     */
    public EmailMessage parse(Session session, Message message) throws MessagingException {
        /*
         * Using isMimeType to determine the content type avoids
         * fetching the actual content data until we need it.
         */

//        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        EmailMessage emailMessage = new EmailMessage(message);
        emailMessage.setSubject(message.getSubject());

        Pattern p = Pattern.compile(EXTRACT_MAIL_REGEXP, Pattern.DOTALL);
        Address[] fromAddresses = message.getFrom();
        if (fromAddresses.length >= 1) {

            String firtsAddress = fromAddresses[0].toString();
            if (firtsAddress.matches(MAIL_VALIDATOR_REGEXP)) {
                emailMessage.setFromName(firtsAddress.substring(0, firtsAddress.indexOf('@')));
                emailMessage.setFromEmail(firtsAddress);
            } else {
                Matcher matcher = p.matcher(firtsAddress);

                if (matcher.find()) {
                    // filter newline
                    String name = matcher.group(1).replaceAll("[\\n\\r]+", "");
                    String email = matcher.group(2).replaceAll("[\\n\\r]+", "");
                    emailMessage.setFromName(name);
                    emailMessage.setFromEmail(email);
                }
            }

        }

        try {
            if (processMimeMessage((MimeMessage) message, "text/*")) {
                String s = (String) message.getContent();
                emailMessage.setText(s);
                emailMessage.setTextIsHtml(processMimeMessage((MimeMessage) message, "text/html"));

            } else if (processMimeMessage((MimeMessage) message, "multipart/*")) {

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                message.writeTo(bos);
                bos.close();
                SharedByteArrayInputStream bis
                        = new SharedByteArrayInputStream(bos.toByteArray());
                MimeMessage cmsg = new MimeMessage(session, bis);
                cmsg.getContent();
                Multipart multipart = (Multipart) cmsg.getContent();
                analyzeMultipart(multipart, emailMessage);
                bis.close();
            } else {
                /*
                 * If we actually want to see the data, and it's not a
                 * MIME type we know, fetch it and check its Java type.
                 */
                Object o = message.getContent();
                if (o instanceof String) {
                    emailMessage.setText(o.toString());
                } else {
                    emailMessage.setText("Unknown content: " + o.getClass());
                }
            }
        } catch (IOException io) {
            io.printStackTrace();
        }

        return emailMessage;
    }

    public boolean processMimeMessage(MimeMessage msg, String mimeType) throws MessagingException {
        try {
            return msg.isMimeType(mimeType);
        } catch (MessagingException messEx) {
            //making sure that it's a BODYSTRUCTURE error
            if (messEx.getMessage() != null && messEx.getMessage().toLowerCase().
                    contains("unable to load bodystructure")) {
                //creating local copy of given MimeMessage
                MimeMessage msgDownloaded = new MimeMessage((MimeMessage) msg);
                //calling same method with local copy of given MimeMessage
                return processMimeMessage(msgDownloaded, mimeType);
            } else {
                throw messEx;
            }
        }
    }

    private void analyzeMultipart(Multipart multipart, EmailMessage emailMessage) throws MessagingException, IOException {
        int nparts = multipart.getCount();
        //emailMessage.setParts(nparts);
        for (int i = 0; i < nparts; i++) {
            BodyPart bodypart = multipart.getBodyPart(i);
//            System.out.println("bodypart.getContentType(): " + bodypart.getContentType());
            if (bodypart.getContentType().contains("multipart")) {
                analyzeMultipart((Multipart) bodypart.getContent(), emailMessage);
            }
            String disposition = null;
            try {
                disposition = bodypart.getDisposition();
            } catch (ParseException ex) {/*can throw a parseException, this is a normal behaviour*/}
            // many mailers don't include a Content-Disposition
            if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                String filename = bodypart.getFileName();
                EmailAttachment att = new EmailAttachment();
                InputStream is = bodypart.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytearray = buffer.toByteArray();
                att.setData(bytearray);
                att.setDisposition(Part.ATTACHMENT);
                att.setName(filename);
                att.setMimeType(bodypart.getContentType());
                emailMessage.addAttachment(att);
            } else if (bodypart.isMimeType("image/*")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream imageInputStream = bodypart.getInputStream();
                byte[] bytes = new byte[1000];
                int leidos = 0;
                while ((leidos = imageInputStream.read(bytes)) > 0) {
                    baos.write(bytes, 0, leidos);
                }
                baos.flush();
                EmailAttachment attachment = new EmailAttachment();
                attachment.setContentId(((MimeBodyPart) bodypart).getContentID());
                attachment.setData(baos.toByteArray());
                attachment.setName(bodypart.getFileName());
                if (attachment.getName() == null) {
                    int startIndex = bodypart.getContentType().indexOf('/');
                    attachment.setName("image." + bodypart.getContentType().substring(startIndex + 1).toLowerCase());
                }
                attachment.setMimeType(bodypart.getContentType());
                attachment.setDisposition(Part.INLINE);
                emailMessage.addAttachment(attachment);
            } else if (bodypart.isMimeType("text/plain")) {
                if ((emailMessage.getText() == null) || (emailMessage.getText().isEmpty())) {
                    emailMessage.setText((String) bodypart.getContent());
                }
            } else if (bodypart.isMimeType("text/html")) {
                emailMessage.setText((String) bodypart.getContent());
            }

        }
    }
}
