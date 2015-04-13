/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import com.itcs.commons.email.EmailClient;
import static com.itcs.commons.email.EmailClient.DISABLE_MAX_ATTACHMENTS_SIZE;
import static com.itcs.commons.email.EmailClient.MAX_ATTACHMENTS_SIZE_PROP_NAME;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;

/**
 *
 * @author jonathan
 */
public class RunnableSendHTMLEmail implements Runnable {

    private Session session;
    private final String[] to;
    private final String subject;
    private final String body;
    private final List<EmailAttachment> attachments;
    private final String[] cco;
    private final String[] cc;

    public RunnableSendHTMLEmail(Session session, String[] to, String subject, String body, List<EmailAttachment> attachments) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
        this.session = session;
        this.cco = null;
        this.cc = null;
    }
    
    public RunnableSendHTMLEmail(Session session, String[] to, String[] cc, String[] cco, String subject, String body, List<EmailAttachment> attachments) {
        this.to = to;
        this.cc = cc;
        this.cco = cco;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
        this.session = session;
    }

    public void run() {
        Logger.getLogger(RunnableSendHTMLEmail.class.getName()).log(Level.INFO, "executing Asynchronous task RunnableSendHTMLEmail");
        try {
            HtmlEmail email = new HtmlEmail();
            email.setCharset("utf-8");
            email.setMailSession(getSession());
            for (String dir : to) {
                email.addTo(dir);
            }
            if(cc != null){
                for (String ccEmail : cc) {
                    email.addCc(ccEmail);
                }
            }
            if(cco != null){
                for (String ccoEmail : cco) {
                    email.addBcc(ccoEmail);
                }
            }
            email.setSubject(subject);
            // set the html message
            email.setHtmlMsg(body);
            email.setFrom(getSession().getProperties().getProperty(Email.MAIL_SMTP_FROM, Email.MAIL_SMTP_USER),
                    getSession().getProperties().getProperty(Email.MAIL_SMTP_FROMNAME, Email.MAIL_SMTP_USER));
            // set the alternative message
            email.setTextMsg("Si ve este mensaje, significa que su cliente de correo no permite mensajes HTML.");
            // send the email
            if (attachments != null) {
                addAttachments(email, attachments);
            }
            email.send();
            Logger.getLogger(RunnableSendHTMLEmail.class.getName()).log(Level.INFO, "Email sent successfully to:{0} cc:{1} bcc:{2}", new Object[]{Arrays.toString(to), Arrays.toString(cc), Arrays.toString(cco)});
        } catch (EmailException e) {
            Logger.getLogger(RunnableSendHTMLEmail.class.getName()).log(Level.SEVERE, "EmailException Error sending email... with properties:\n" + session.getProperties(), e);
        }
    }

    private void addAttachments(MultiPartEmail email, List<EmailAttachment> attachments) throws EmailException {

        if (attachments != null && attachments.size() > 0) {
            String maxStringValue = getSession().getProperty(MAX_ATTACHMENTS_SIZE_PROP_NAME);
//            System.out.println("maxStringValue= " + maxStringValue);
            long maxAttachmentSize = DISABLE_MAX_ATTACHMENTS_SIZE;
            try {
                maxAttachmentSize = Long.parseLong(maxStringValue);
            } catch (NumberFormatException e) {
                Logger.getLogger(RunnableSendHTMLEmail.class.getName()).log(Level.WARNING, "DISABLE_MAX_ATTACHMENTS_SIZE MailSession does not have property " + MAX_ATTACHMENTS_SIZE_PROP_NAME);
            }

            long size = 0;
            for (EmailAttachment attach : attachments) {
                if (maxAttachmentSize != EmailClient.DISABLE_MAX_ATTACHMENTS_SIZE) {
                    size += attach.getSize();
                    if (size > maxAttachmentSize) {
                        throw new EmailException("Adjuntos exceden el tamaño maximo permitido (" + maxAttachmentSize + "),"
                                + " pruebe enviando menos archivos adjuntos, o de menor tamaño.");
                    }
                }
                if (attach.getData() != null) {
                    try {
                        email.attach(new ByteArrayDataSource(attach.getData(), attach.getMimeType()), attach.getName(), attach.getMimeType());
                    } catch (IOException e) {
                        throw new EmailException("IOException Attachment has errors," + e.getMessage());
                    } catch (EmailException e) {
                        throw new EmailException("EmailException Attachment has errors," + e.getMessage());
                    }
                } else {
                    email.attach(attach);
                }
            }

        }

    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

}
