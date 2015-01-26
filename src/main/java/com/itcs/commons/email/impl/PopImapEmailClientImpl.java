/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import com.itcs.commons.email.EmailClient;
import static com.itcs.commons.email.EmailClient.DISABLE_MAX_ATTACHMENTS_SIZE;
import static com.itcs.commons.email.EmailClient.MAX_ATTACHMENTS_SIZE_PROP_NAME;
import com.itcs.commons.email.EmailMessage;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.SortTerm;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.search.AndTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;

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

        if (mailSession == null) {
            if (useJNDI == true && jndiName != null && !jndiName.isEmpty()) {
                System.out.println("loading jndi session...");
                this.mailSession = getMailSessionFromJNDI(jndiName);
            } else {
//            System.out.println("local");
                username = mailConnectionProps.getProperty(Email.MAIL_SMTP_USER);
                password = mailConnectionProps.getProperty(Email.MAIL_SMTP_PASSWORD);

                // only create a new mail session with an authenticator if
                // authentication is required and no user name is given
                mailSession = Session.getInstance(mailConnectionProps, new DefaultAuthenticator(username, password));

            }

            Logger.getLogger(PopImapEmailClientImpl.class.getName()).log(Level.INFO, "created New JavaMail Session: {0}", mailSession);

        } else {
            Logger.getLogger(PopImapEmailClientImpl.class.getName()).log(Level.INFO, "Using existing JavaMail Session: {0}", mailSession);
        }

        return mailSession;
    }

    /**
     * toString
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
        if (store != null && store.isConnected()) {
            store.close();
        }
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

    @Override
    public List<EmailMessage> getUnreadMessagesOnlyHeaders() throws EmailException, MessagingException {
        List<EmailMessage> result = new LinkedList<EmailMessage>();
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        JavaMailMessageParser parser = new JavaMailMessageParser();
        Message[] msgs = folder.search(ft);
        for (Message msg : msgs) {
            EmailMessage parsedMessage = parser.parseOnlyHeader(mailSession, msg);
            parsedMessage.setIdMessage(((UIDFolder) msg.getFolder()).getUID(msg));
            result.add(parsedMessage);
        }
        return result;
    }

    private Message[] getUnseenReverseSortedMessages() {
        try {
            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            return ((IMAPFolder) folder).getSortedMessages(new SortTerm[]{SortTerm.REVERSE}, ft);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Message[] getUnseenMessages() {
        try {
            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            return folder.search(ft);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Message[] getRecentMessages(Date date) {
        try {
            SearchTerm ft = new ReceivedDateTerm(DateTerm.GT, date);
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            return ((IMAPFolder) folder).search(ft);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<EmailMessage> getUnreadMessagesOnlyHeaders(int limit) throws EmailException, MessagingException {
        List<EmailMessage> result = new LinkedList<>();
        JavaMailMessageParser parser = new JavaMailMessageParser();
        Message[] msgs = getUnseenMessages();
//        Message[] msgs = folder.search(ft);
        int messageCount = 0;

        for (int i = (msgs.length - 1); i >= 0; i--) {
            Message msg = msgs[i];
            EmailMessage parsedMessage = parser.parseOnlyHeader(mailSession, msg);
            parsedMessage.setIdMessage(((UIDFolder) msg.getFolder()).getUID(msg));
            result.add(parsedMessage);
            messageCount += 1;
            if (messageCount >= limit) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<EmailMessage> getMessagesOnlyHeaders(long firstuid, long lastuid) throws EmailException, MessagingException {
        List<EmailMessage> result = new LinkedList<EmailMessage>();
        JavaMailMessageParser parser = new JavaMailMessageParser();
        Message[] msgs = ((UIDFolder) folder).getMessagesByUID(firstuid, lastuid);
        long lastuidFolder = ((IMAPFolder) folder).getUIDNext();
        while (msgs.length == 0) {
            if (firstuid > lastuidFolder) {
                break;
            }
            firstuid = lastuid;
            lastuid += 10;
            msgs = ((UIDFolder) folder).getMessagesByUID(firstuid, lastuid);
        }
//        if(msgs.length == 0){
//            lastuid = ((IMAPFolder) folder).getUIDNext();
//            msgs = ((IMAPFolder) folder).getMessagesByUID(firstuid, lastuid);
//        }
        for (Message msg : msgs) {
            EmailMessage parsedMessage = parser.parseOnlyHeader(mailSession, msg);
            parsedMessage.setIdMessage(((UIDFolder) msg.getFolder()).getUID(msg));
            result.add(parsedMessage);
        }
        return result;
    }

    @Override
    public EmailMessage getMessage(long id) throws MessagingException {
        JavaMailMessageParser parser = new JavaMailMessageParser();
        return parser.parse(mailSession, ((UIDFolder) folder).getMessageByUID(id));
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
            result.add(parser.parse(mailSession, m));
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
            result.add(parser.parse(mailSession, m));
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
        if (folder != null && folder.isOpen()) {
            folder.close(true);
        }
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
        try {
            getExecutorService().execute(new RunnableSendHTMLEmail(getSession(), new String[]{to}, subject, body, attachments));
        } catch (NamingException ex) {
            Logger.getLogger(PopImapEmailClientImpl.class.getName()).log(Level.SEVERE, "sendHTML", ex);
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
    @Override
    public void sendHTML(String[] to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        sendHTML(to, null, null, subject, body, attachments);
    }

    public void sendHTML(String[] to, String[] cc, String[] cco, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        try {
            getExecutorService().execute(new RunnableSendHTMLEmail(getSession(), to, cc, cco, subject, body, attachments));
        } catch (NamingException ex) {
            Logger.getLogger(PopImapEmailClientImpl.class.getName()).log(Level.SEVERE, "sendHTML", ex);
        }
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
        return (Session) ic.lookup(jndiName);
//        return ((Session) ic.lookup(jndiName));
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
}
