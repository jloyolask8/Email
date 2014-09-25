/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email.impl;

import com.itcs.commons.email.EmailClient;
import com.itcs.commons.email.EmailMessage;
import com.moonrug.exchange.AttachmentSource;
import com.moonrug.exchange.AttachmentType;
import com.moonrug.exchange.DefaultFolder;
import com.moonrug.exchange.EmailInfo;
import com.moonrug.exchange.ExchangeException;
import com.moonrug.exchange.IAttachment;
import com.moonrug.exchange.IContentItem;
import com.moonrug.exchange.IFolder;
import com.moonrug.exchange.IMapiSession;
import com.moonrug.exchange.IMessage;
import com.moonrug.exchange.NamedProperty;
import com.moonrug.exchange.Property;
import com.moonrug.exchange.Proptag;
import com.moonrug.exchange.Recipient;
import com.moonrug.exchange.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author jorge
 */
public class ExchangeEmailClientImpl implements EmailClient {

    public final static Property FROM = new NamedProperty("internet_from", NamedProperty.INTERNET_HEADERS,
            "FROM", Property.Type.STRING);
    public final static Property FROM1 = new Proptag(0x5d01001f);
    public final static Property ALL_HEADERS = new Proptag(0x007d001f);
    protected final static Property[] DEFAULT_PROPS = {
        com.moonrug.exchange.IMessage.IMPORTANCE,
        com.moonrug.exchange.IMessage.MESSAGE_CLASS,
        com.moonrug.exchange.IMessage.HAS_ATTACH,
        //IMessage.HTML,
        com.moonrug.exchange.IMessage.SUBJECT,
        com.moonrug.exchange.IMessage.SENT_REPRESENTING_NAME,
        com.moonrug.exchange.IMessage.FLAGS,
        IMessage.SENT_REPRESENTING_ADDRESS,
        FROM,
        FROM1,};
    protected final static Property[] BODY_PROPS = {
        IContentItem.BODY/*,
     IContentItem.HTML*/

    };
    protected final static Property[] MINIMAL_PROPS = {
        com.moonrug.exchange.IMessage.SUBJECT,
        FROM
    };
    private Properties mailConnectionProps;
    private Session sessionReceiver;
    private IFolder folder;

    public ExchangeEmailClientImpl(Properties mailConnectionProps) {
        this.mailConnectionProps = mailConnectionProps;
    }

    @Override
    public void connectStore() throws EmailException, MessagingException {
        String userName = mailConnectionProps.getProperty(Email.RECEIVER_USERNAME);
        String password = mailConnectionProps.getProperty(Email.RECEIVER_PASSWORD);
        String server = mailConnectionProps.getProperty(Email.MAIL_EXCHANGE_SERVER);
        String domain = mailConnectionProps.getProperty(Email.MAIL_EXCHANGE_DOMAIN);
        Map<String, String> map = new HashMap<String, String>();
        map.put(Session.USERNAME, userName);
        map.put(Session.PASSWORD, password);
        map.put(Session.DOMAIN, domain);
        map.put(Session.SERVER, server);
        map.put(IMapiSession.RECEIVE_TIMING, "10");
        setSessionReceiver(IMapiSession.Factory.create(map));
    }

    private Session connectAsSender() {
        String userName = mailConnectionProps.getProperty(Email.SENDER_USERNAME);
        String password = mailConnectionProps.getProperty(Email.SENDER_PASSWORD);
        String server = mailConnectionProps.getProperty(Email.MAIL_EXCHANGE_SERVER);
        String domain = mailConnectionProps.getProperty(Email.MAIL_EXCHANGE_DOMAIN);
        Map<String, String> map = new HashMap<String, String>();
        map.put(Session.USERNAME, userName);
        map.put(Session.PASSWORD, password);
        map.put(Session.DOMAIN, domain);
        map.put(Session.SERVER, server);
        map.put(IMapiSession.RECEIVE_TIMING, "10");
        return IMapiSession.Factory.create(map);
    }

    /**
     * @return the sessionReceiver
     */
    private Session getSessionReceiver() {
        return sessionReceiver;
    }

    /**
     * @param sessionReceiver the sessionReceiver to set
     */
    private void setSessionReceiver(Session sessionReceiver) {
        this.sessionReceiver = sessionReceiver;
    }

    @Override
    public void disconnectStore() throws Exception {
        getSessionReceiver().close();
    }

    @Override
    public void openFolder(String folderName) throws EmailException, MessagingException {
        try {
            if(folderName.equalsIgnoreCase("inbox"))
            {
                folder = getSessionReceiver().getStore().getFolder(DefaultFolder.INBOX);
            }
            else
            {
                folder = getSessionReceiver().getStore().getFolder(folderName);
            }
        } catch (ExchangeException ex) {
            throw new EmailException(ex);
        }
    }

    @Override
    public void closeFolder() throws EmailException, MessagingException {
        //Do nothing
    }

    @Override
    public int getNewMessageCount() throws EmailException, MessagingException {
        return folder.getUnreadCount();
    }

    @Override
    public int getMessageCount() throws EmailException, MessagingException {
        return folder.getContentCount();
    }

    @Override
    public List<EmailMessage> getUnreadMessages() throws EmailException, MessagingException {
        return getAllMessages();
    }

    @Override
    public List<EmailMessage> getAllMessages() throws EmailException, MessagingException {
        try {
            List<String> ids = getAllMessagesIds();
            List<EmailMessage> lista = new ArrayList<EmailMessage>(ids.size());
            for (String id : ids) {
                IContentItem message = getMessage(id);
                EmailMessage emailMessage = new EmailMessage(message);
                emailMessage.setFromEmail(getAddressMail(message));
                emailMessage.setFromName(message.getSender().getName());
                emailMessage.setSubject(message.getSubject());
                emailMessage.setText(getBody(id));
                emailMessage.setTextIsHtml(false);

                for (IAttachment attachment : message.getAttachments()) {
                    if (attachment.getType().equals(AttachmentType.BINARY)) {
                        EmailAttachment emailAttach = new EmailAttachment();
                        while (!downloadAttachment(attachment, emailAttach)) {
                            //System.out.print(".");
                        }
                        emailMessage.addAttachment(emailAttach);
                    }
                }
                emailMessage.setHasAttachment((emailMessage.getAttachments().size() > 0));
                lista.add(emailMessage);
            }
            return lista;
        } catch (Exception ex) {
            throw new EmailException(ex);
        }
    }

    private boolean downloadAttachment(IAttachment attachment, EmailAttachment emailAttachment) {
        ByteArrayOutputStream baos = null;
        try {
            String nombre = attachment.getLongName();
            nombre = nombre.substring(nombre.lastIndexOf(File.separator) + 1);
            nombre = nombre.substring(nombre.lastIndexOf('\\') + 1);
            baos = new ByteArrayOutputStream();
            attachment.writeTo(baos);
            emailAttachment.setData(baos.toByteArray());
            emailAttachment.setMimeType(attachment.getMimeTag());
            emailAttachment.setName(nombre);
            emailAttachment.setPath(nombre);
            emailAttachment.setSize(emailAttachment.getData().length);
            baos.close();
            return true;
        } catch (Exception e) {
            //logger.log(Level.SEVERE, null, e);
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private String getBody(String id) throws ExchangeException {
        IContentItem item = folder.getItem(id, BODY_PROPS);
        return item.getLargeString(IContentItem.BODY);
    }

    private EmailInfo[] getEmailInfo(String sFrom) throws ExchangeException {
        EmailInfo[] einfo = getSessionReceiver().getAddressBook().resolve(sFrom);
        return einfo;
    }

    private String getAddressMail(IContentItem item) throws IOException, ExchangeException {
        String sFrom = item.getString(IContentItem.SENT_REPRESENTING_ADDRESS);

        if (null == sFrom || -1 == sFrom.indexOf("@")) {
            sFrom = item.getString(FROM);
        }

        if (null == sFrom || -1 == sFrom.indexOf("@")) {
            sFrom = item.getString(FROM1);
        }

        if (null == sFrom || -1 == sFrom.indexOf("@")) {
            Properties headers = new Properties();
            headers.load(new java.io.StringReader(item.getLargeString(ALL_HEADERS)));
            sFrom = headers.getProperty("From", null);
        }

        if (null == sFrom || -1 == sFrom.indexOf("@")) {
            EmailInfo emailInfo = item.getSender();
            if ((emailInfo.getAddress() != null)) {
                if (emailInfo.getAddress().startsWith("/O=")) {
                    sFrom = emailInfo.getAddress();
                    int indexOfCn = sFrom.lastIndexOf("/CN=");
                    sFrom = sFrom.substring(indexOfCn + 4);
                    try {
                        EmailInfo[] einfo = getEmailInfo(sFrom);
                        if (einfo.length > 0) {
                            sFrom = einfo[0].getAddress();
                        }
                    } catch (ExchangeException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        sFrom = extractEmailAddress(sFrom);
        return sFrom;
    }

    private String extractEmailAddress(String rawAddress) {
        Pattern p = Pattern.compile("(.*?)<([^>]+)>\\s*,?", Pattern.DOTALL);
        Matcher matcher = p.matcher(rawAddress);
        String email = null;
        if (matcher.find()) {
            email = matcher.group(2).replaceAll("[\\n\\r]+", "");
        } else {
            email = rawAddress;
        }
        return email;
    }

    @Override
    public List<String> getAllMessagesIds() throws EmailException, MessagingException {
        try {
            IContentItem[] items = folder.getItems(MINIMAL_PROPS);
            List<String> ids = new ArrayList<String>(items.length);
            for (IContentItem iContentItem : items) {
                ids.add(iContentItem.getId());
            }
            return ids;
        } catch (ExchangeException ex) {
            throw new EmailException(ex);
        }
    }

    public IContentItem getMessage(String id) throws ExchangeException {
        return folder.getItem(id, DEFAULT_PROPS);
    }

    @Override
    public void sendText(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        sendMail(to, subject, body, attachments);
    }

    @Override
    public void sendHTML(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        sendMail(to, subject, body, attachments);
    }

    private void sendMail(String toAddres, String subject, String content, List<EmailAttachment> attachments) throws EmailException {
        try {
            Session sessionSender = connectAsSender();
            final com.moonrug.exchange.internal.Message email = (com.moonrug.exchange.internal.Message) sessionSender.getStore().getOutbox().add();
            email.addRecipient(new Recipient(Recipient.Type.TO, toAddres, toAddres));
            email.setHtml(content);
            //email.setBody(content);

            email.setSubject(subject);

            if (attachments != null) {
                for (EmailAttachment attachment : attachments) {
                    String nombre = attachment.getName();

                    AttachmentSource attSource = new AttachmentSource(nombre,
                            new ByteArrayInputStream(attachment.getData()));
                    email.addAttachment(attSource);
                }
            }
            email.send();
            eliminarBandejaDeSalida(sessionSender);
            sessionSender.close();
        } catch (Exception ex) {
            throw new EmailException(ex);
        }
    }

    private void eliminarBandejaDeSalida(Session sessionSender) throws ExchangeException {
        IContentItem[] items = sessionSender.getStore().getOutbox().getItems();
        String[] ids = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            ids[i] = items[i].getId();
        }

        for (String idMail : ids) {
            sessionSender.getStore().getOutbox().deleteItem(idMail);
        }
    }

    @Override
    public void markReadMessages(EmailMessage[] messages) throws EmailException, MessagingException {
        try {
            String[] ids = new String[messages.length];
            int index = 0;
            for (EmailMessage emailMessage : messages) {
                ids[index] = ((IContentItem) emailMessage.getTheOriginalMessage()).getId();
                index++;
            }
            folder.markRead(ids, true);
        } catch (ExchangeException ex) {
            throw new EmailException(ex);
        }
    }

    @Override
    public void deleteMessages(EmailMessage[] messages) throws EmailException, MessagingException {
        try {
            String[] ids = new String[messages.length];
            int index = 0;
            for (EmailMessage emailMessage : messages) {
                ids[index] = ((IContentItem) emailMessage.getTheOriginalMessage()).getId();
                index++;
            }
            folder.deleteItems(ids);
        } catch (ExchangeException ex) {
            throw new EmailException(ex);
        }
    }

    @Override
    public void markReadMessage(EmailMessage message) throws EmailException, MessagingException {
        try {
            folder.markRead(new String[]{((IContentItem) message.getTheOriginalMessage()).getId()}, true);
        } catch (ExchangeException ex) {
            throw new EmailException(ex);
        }
    }

    @Override
    public void deleteMessage(EmailMessage message) throws EmailException, MessagingException {
        try {
            folder.deleteItem(((IContentItem) message.getTheOriginalMessage()).getId());
        } catch (ExchangeException ex) {
            throw new EmailException(ex);
        }
    }

    public int getUnreadMessageCount() throws EmailException, MessagingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void sendHTML(String[] to, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void sendHTML(String[] to, String[] cc, String[] cco, String subject, String body, List<EmailAttachment> attachments) throws EmailException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
