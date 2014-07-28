/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import com.itcs.commons.email.EmailClient;
import static com.itcs.commons.email.EmailClient.DISABLE_MAX_ATTACHMENTS_SIZE;
import static com.itcs.commons.email.EmailClient.MAX_ATTACHMENTS_SIZE_PROP_NAME;
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

    public RunnableSendHTMLEmail(Session session, String[] to, String subject, String body, List<EmailAttachment> attachments) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
        this.session = session;
    }

    public void run() {
        System.out.println("executing Asynchronous task RunnableSendHTMLEmail");

        try {
            HtmlEmail email = new HtmlEmail();
            email.setCharset("utf-8");
            email.setMailSession(getSession());
            for (String dir : to) {
                email.addTo(dir);
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

            System.out.println("email sent ok");
        } catch (Exception e) {
            System.out.println("error sending email...");
            e.printStackTrace();
        }

    }

    private void addAttachments(MultiPartEmail email, List<EmailAttachment> attachments) throws EmailException {

        if (attachments != null && attachments.size() > 0) {
            String maxStringValue = getSession().getProperty(MAX_ATTACHMENTS_SIZE_PROP_NAME);
            System.out.println("maxStringValue= " + maxStringValue);
            long maxAttachmentSize = DISABLE_MAX_ATTACHMENTS_SIZE;
            try {
                maxAttachmentSize = Long.parseLong(maxStringValue);
            } catch (Exception e) {
                e.printStackTrace();
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
                    } catch (Exception e) {
                        throw new EmailException("Attchment has errors," + e.getMessage());
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
