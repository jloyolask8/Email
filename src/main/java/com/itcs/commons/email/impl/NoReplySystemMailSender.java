/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import static com.itcs.commons.email.impl.PopImapEmailClientImpl.getExecutorService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author jonathan
 */
public class NoReplySystemMailSender {

//    @Resource(mappedName = "java:mail/noReplyGodeskEmailSession")
    protected static Session noReplyGodeskEmailSession = null;

    /**
     * Singleton pattern
     * @return
     * @throws NamingException 
     */
    private static Session getSession() throws NamingException {
        if (noReplyGodeskEmailSession == null) {
            Context ctx = new InitialContext();
            noReplyGodeskEmailSession = (Session) ctx.lookup("mail/noReplyGodeskEmailSession");
        }
        return noReplyGodeskEmailSession;
    }

    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    public static void sendHTML(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        try {
            getExecutorService().execute(new RunnableSendHTMLEmail(getSession(), new String[]{to}, subject, body, attachments));
        } catch (NamingException ex) {
            Logger.getLogger(NoReplySystemMailSender.class.getName()).log(Level.INFO, "el nombre jndi de la session de email esta mal configurado!!", ex);
        }
    }

    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    public static void sendHTML(String[] to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        try {
            getExecutorService().execute(new RunnableSendHTMLEmail(getSession(), to, subject, body, attachments));
        } catch (NamingException ex) {
            Logger.getLogger(NoReplySystemMailSender.class.getName()).log(Level.INFO, "el nombre jndi de la session de email esta mal configurado!!", ex);
        }
    }
}
