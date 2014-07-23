/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import com.itcs.commons.email.EmailClient;
import static com.itcs.commons.email.EmailClient.DISABLE_MAX_ATTACHMENTS_SIZE;
import static com.itcs.commons.email.EmailClient.MAX_ATTACHMENTS_SIZE_PROP_NAME;
import com.itcs.commons.email.EmailMessage;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;

/**
 *
 * @author Jonathan
 */
public class PopImapEmailClientImpl implements EmailClient {

    private static ExecutorService executorService;

    /**
     * @return the executorService
     */
    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(10);
        }
        return executorService;
    }

//    @Resource(name = "jonyGmail")
    Session mailSession = null;
    private Store store = null;
//    private String username, password;
    private Folder folder = null;
    private boolean useJNDI;
    private String jndiName;
    private Properties mailConnectionProps;
    private String username;
    private String password;

    public PopImapEmailClientImpl(String jndiName) {
        this.useJNDI = true;
        this.jndiName = jndiName;
        this.mailConnectionProps = null;
    }

    public PopImapEmailClientImpl(Properties mailConnectionProps) {
        this.useJNDI = false;
        this.jndiName = null;
        this.mailConnectionProps = mailConnectionProps;
    }

    private Session getSession() throws NamingException {

        if (useJNDI == true && jndiName != null && !jndiName.isEmpty()) {
            System.out.println("jndi");
            this.mailSession = getMailSessionFromJNDI(jndiName);
        } else {
//            System.out.println("local");
            username = mailConnectionProps.getProperty(Email.MAIL_SMTP_USER);
            password = mailConnectionProps.getProperty(Email.MAIL_SMTP_PASSWORD);

            // only create a new mail session with an authenticator if
            // authentication is required and no user name is given
            mailSession = Session.getInstance(mailConnectionProps, new DefaultAuthenticator(username, password));

        }
        return mailSession;
    }

    /**
     *
     * @throws EmailException
     * @throws MessagingException
     */
    @Override
    public String toString() {
        return "PopImapEmailClientImpl [" + "jndiName " + jndiName + " " + "mailConnectionProps " + mailConnectionProps + " " + "mailSession " + mailSession + " " + "store " + store + " " + "useJNDI " + useJNDI + " " + "username " + username + " " + "DISABLE_MAX_ATTACHMENTS_SIZE " + DISABLE_MAX_ATTACHMENTS_SIZE + " " + "MAX_ATTACHMENTS_SIZE_PROP_NAME " + MAX_ATTACHMENTS_SIZE_PROP_NAME + "]";
    }

    @Override
    public void connectStore() throws Exception {
        Session session;
//        try {
        session = getSession();
        store = session.getStore();
        store.connect();
//        } catch (NamingException ex) {
//            Logger.getLogger(PopImapEmailClientImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//            System.out.println("props : " + session.getProperties());
    }

    /**
     *
     * @throws javax.mail.MessagingException
     *
     */
    public void disconnectStore() throws Exception {
        store.close();
    }

    /**
     *
     * @return @throws EmailException
     * @throws MessagingException
     */
    @Override
    public int getMessageCount() throws EmailException, MessagingException {
        return folder.getMessageCount();
    }

    /**
     *
     * @return @throws EmailException
     * @throws MessagingException
     */
    @Override
    public int getNewMessageCount() throws EmailException, MessagingException {
        return folder.getNewMessageCount();
    }

    @Override
    public int getUnreadMessageCount() throws EmailException, MessagingException {
        return folder.getUnreadMessageCount();
    }

    /**
     *
     * @return @throws EmailException
     * @throws MessagingException
     */
    @Override
    public List<EmailMessage> getUnreadMessages() throws EmailException, MessagingException {
        List<EmailMessage> result = new LinkedList<EmailMessage>();
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        JavaMailMessageParser parser = new JavaMailMessageParser();
        Message[] msgs = folder.search(ft);
        for (Message m : msgs) {
//            printHeaders(m);
            result.add(parser.parse(m));
        }
        return result;
    }

    @Override
    public List<EmailMessage> getAllMessages() throws EmailException, MessagingException {
        List<EmailMessage> result = new LinkedList<EmailMessage>();
        JavaMailMessageParser parser = new JavaMailMessageParser();
        Message[] msgs = folder.getMessages();
        for (Message m : msgs) {
//            printHeaders(m);
            result.add(parser.parse(m));
        }
        return result;
    }

    /**
     *
     * @throws EmailException
     * @throws MessagingException
     */
    @Override
    public void closeFolder() throws EmailException, MessagingException {
        folder.close(true);
    }

    /**
     *
     * @param folderName
     * @throws EmailException
     * @throws MessagingException
     */
    @Override
    public void openFolder(String folderName) throws EmailException, MessagingException {
        // Open the Folder
        folder = store.getDefaultFolder();
        folder = folder.getFolder(folderName);

        if (folder == null) {
            throw new EmailException("Invalid folder");
        }

        // try to open read/write and if that fails try read-only
        try {

            folder.open(Folder.READ_WRITE);

        } catch (MessagingException ex) {
            ex.printStackTrace();
            folder.open(Folder.READ_ONLY);

        }
    }

//    @Override
//    public void setUserPass(String username, String password) {
//        this.username = username;
//        this.password = password;
//    }
    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    @Override
    public void sendHTML(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        getExecutorService().execute(new RunnableSendHTMLEmail(new String[]{to}, subject, body, attachments));
    }

    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    @Override
    public void sendHTML(String[] to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        getExecutorService().execute(new RunnableSendHTMLEmail(to, subject, body, attachments));
    }
    
    public void sendText(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//    /**
//     *
//     * @param to
//     * @param subject
//     * @param body
//     * @param attachments
//     * @throws EmailException
//     */
//    @Override
//    public void sendText(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
//        if (attachments != null && attachments.size() > 0) {
//            // Create the email message
//            MultiPartEmail email = new MultiPartEmail();
//            try {
//                email.setMailSession(getSession());
//            } catch (NamingException n) {
//                throw new EmailException("Error en la configuracion de la session de email:" + n.getMessage());
//            }
//            email.addTo(to);
////            email.setFrom("me@apache.org", "Me");
//            email.setSubject(subject);
//            email.setMsg(body);
//
//            // add the attachment
//            addAttachments(email, attachments);
//
//            // send the email
//            email.send();
//        } else {
//            SimpleEmail email = new SimpleEmail();
//            email.setMailSession(mailSession);
//            email.addTo(to);
////            email.setFrom("me@apache.org", "Me");
//            email.setSubject(subject);
//            email.setMsg(body);
//            email.send();
//        }
//    }
    /**
     * Supply a mail Session object to use. Please note that passing a username
     * and password (in the case of mail authentication) will create a new mail
     * session with a DefaultAuthenticator. This is a convience but might come
     * unexpected.
     *
     * If mail authentication is used but NO username and password is supplied
     * the implementation assumes that you have set a authenticator and will use
     * the existing mail session (as expected).
     *
     * @param aSession mail session to be used
     * @since 1.0
     */
    public void setMailSession(Session aSession) {
        if (aSession == null) {
            throw new IllegalArgumentException("no mail session supplied");
        }
        mailSession = aSession;
    }

    /**
     * Supply a mail Session object from a JNDI directory
     *
     * @param jndiName name of JNDI ressource (javax.mail.Session type),
     * ressource if searched in java:comp/env if name dont start with "java:"
     * @throws IllegalArgumentException JNDI name null or empty
     * @throws NamingException ressource can be retrieved from JNDI directory
     * @since 1.1
     */
    private Session getMailSessionFromJNDI(String jndiName) throws NamingException {
        if ((jndiName == null) || (jndiName.length() == 0)) {
            throw new IllegalArgumentException("JNDI name missing");
        }
        InitialContext ic = new InitialContext();
        return ((Session) ic.lookup(jndiName));
    }

    @Override
    public List<String> getAllMessagesIds() throws EmailException, MessagingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void markReadMessages(EmailMessage[] messages) throws EmailException, MessagingException {
        for (EmailMessage emailMessage : messages) {
            markReadMessage(emailMessage);
        }
    }

    @Override
    public void deleteMessages(EmailMessage[] messages) throws EmailException, MessagingException {
        for (EmailMessage emailMessage : messages) {
            deleteMessage(emailMessage);
        }
    }

    @Override
    public void markReadMessage(EmailMessage message) throws EmailException, MessagingException {
        ((Message) message.getTheOriginalMessage()).setFlag(Flags.Flag.SEEN, true);
    }

    @Override
    public void deleteMessage(EmailMessage message) throws EmailException, MessagingException {
        ((Message) message.getTheOriginalMessage()).setFlag(Flags.Flag.DELETED, true);
    }

    private void printHeaders(Message m) throws MessagingException {
        Enumeration enumeration = m.getAllHeaders();
        while (enumeration.hasMoreElements()) {
            Header header = (Header) enumeration.nextElement();
            System.out.println("[" + header.getName() + "]=" + header.getValue());
        }
    }

    

    class RunnableSendHTMLEmail implements Runnable {

        String[] to;
        String subject;
        String body;
        List<EmailAttachment> attachments;

        public RunnableSendHTMLEmail(String[] to, String subject, String body, List<EmailAttachment> attachments) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.attachments = attachments;
        }

        public void run() {
            System.out.println("executing Asynchronous task PopImapClientImpl.sendHTML()");

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
                email.setFrom(mailConnectionProps.getProperty(Email.MAIL_SMTP_FROM, Email.MAIL_SMTP_USER), mailConnectionProps.getProperty(Email.MAIL_SMTP_FROMNAME, Email.MAIL_SMTP_USER));
                // set the alternative message
                email.setTextMsg("Si ve este mensaje, significa que su cliente de correo no permite mensajes HTML.");
                // send the email
                if (attachments != null) {
                    addAttachments(email, attachments);
                }

                email.send();
            } catch (Exception e) {
                System.out.println("mailConnectionProps:" + mailConnectionProps);
                e.printStackTrace();
            }

        }

        private void addAttachments(MultiPartEmail email, List<EmailAttachment> attachments) throws EmailException, NamingException {

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
    }

}
