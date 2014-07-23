/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email;

import java.util.List;
import javax.mail.MessagingException;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author Jonathan
 */
public interface EmailClient {

    /**
     * Constant value to understand that the max size validation is disabled
     */
    public static int DISABLE_MAX_ATTACHMENTS_SIZE = -1;
    public static String MAX_ATTACHMENTS_SIZE_PROP_NAME = "mail.attachment.maxsize";

//    void setUserPass(String username, String password);
    /**
     *
     * @throws EmailException
     * @throws MessagingException
     */
    public void connectStore() throws Exception;
    

    /**
     *
     * @throws Exception
     */
    public void disconnectStore() throws Exception;

    /**
     *
     * @param folderName
     * @throws EmailException
     * @throws MessagingException
     */
    void openFolder(String folderName) throws EmailException, MessagingException;

    /**
     *
     * @throws EmailException
     * @throws MessagingException
     */
    void closeFolder() throws EmailException, MessagingException;

    /**
     *
     * @return @throws EmailException
     * @throws MessagingException
     */
    int getNewMessageCount() throws EmailException, MessagingException;

    /**
     *
     * @return @throws EmailException
     * @throws MessagingException
     */
    int getMessageCount() throws EmailException, MessagingException;

    int getUnreadMessageCount() throws EmailException, MessagingException;
    /**
     *
     * @return @throws EmailException
     * @throws MessagingException
     */
    List<EmailMessage> getUnreadMessages() throws EmailException, MessagingException;
//    List<EmailMessage> getUnreadMessages(int start, int end) throws EmailException, MessagingException;

    List<EmailMessage> getAllMessages() throws EmailException, MessagingException;
    
    
    List<String> getAllMessagesIds() throws EmailException, MessagingException;

    /**
     *
     * @param messages
     * @throws EmailException
     * @throws MessagingException
     */
    void markReadMessages(EmailMessage[] messages) throws EmailException, MessagingException;

    /**
     *
     * @param messages
     * @throws EmailException
     * @throws MessagingException
     */
    void deleteMessages(EmailMessage[] messages) throws EmailException, MessagingException;

    /**
     *
     * @param message
     * @throws EmailException
     * @throws MessagingException
     */
    void markReadMessage(EmailMessage message) throws EmailException, MessagingException;

    /**
     *
     * @param message
     * @throws EmailException
     * @throws MessagingException
     */
    void deleteMessage(EmailMessage message) throws EmailException, MessagingException;

    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    void sendText(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException;

    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    void sendHTML(String to, String subject, String body, List<EmailAttachment> attachments) throws EmailException;
    
     /**
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws EmailException
     */
    void sendHTML(String[] to, String subject, String body, List<EmailAttachment> attachments) throws EmailException;
}
