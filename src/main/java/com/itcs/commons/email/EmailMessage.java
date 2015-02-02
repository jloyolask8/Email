/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.mail.EmailAttachment;

/**
 *
 * @author Jonathan
 */
public class EmailMessage implements Serializable {

    private Object theOriginalMessage;
    private long idMessage;
    private boolean hasAttachment = false;
    private boolean textIsHtml;
    private String subject;
    private String text;
    private String fromName;
    private String fromEmail;
    private List<String> ccList;
    private List<String> toList;
    private Date receivedDate;

    private int parts = 0;
    private List<EmailAttachment> attachments = new ArrayList<>();

    /**
     *
     * @param m
     */
    public EmailMessage(Object m) {
        this.theOriginalMessage = m;
    }

    public EmailMessage() {
    }

    /**
     * @return the theOriginalMessage
     */
    public Object getTheOriginalMessage() {
        return theOriginalMessage;
    }

    /**
     * @param theOriginalMessage the theOriginalMessage to set
     */
    public void setTheOriginalMessage(Object theOriginalMessage) {
        this.theOriginalMessage = theOriginalMessage;
    }

    /**
     * @return the hasAttachment
     */
    public boolean isHasAttachment() {
        return hasAttachment;
    }

    /**
     * @param hasAttachment the hasAttachment to set
     */
    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    /**
     * @return the textIsHtml
     */
    public boolean isTextIsHtml() {
        return textIsHtml;
    }

    /**
     * @param textIsHtml the textIsHtml to set
     */
    public void setTextIsHtml(boolean textIsHtml) {
        this.textIsHtml = textIsHtml;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the fromName
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * @param fromName the fromName to set
     */
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    /**
     * @return the fromEmail
     */
    public String getFromEmail() {
        return fromEmail;
    }

    /**
     * @param fromEmail the fromEmail to set
     */
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    /**
     * @return the parts
     */
    public int getParts() {
        return parts;
    }

    /**
     * @param parts the parts to set
     */
    public void setParts(int parts) {
        this.parts = parts;
    }

    /**
     * @return the attachments
     */
    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    /**
     * @param attachment
     */
    public void addAttachment(EmailAttachment attachment) {
        this.attachments.add(attachment);
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Subject:").append(this.getSubject()).append("\n");
        sb.append("From:").append(this.getFromName()).append("\n");
        sb.append("From Email:").append(this.getFromEmail()).append("\n");
        sb.append("Parts:").append(this.getParts()).append("\n");
        sb.append("Body/Text:").append("\n").append(this.getText()).append("\n");
        sb.append("\n---Attachments:").append(getAttachments().size()).append("\n");
        for (EmailAttachment att : getAttachments()) {
            sb.append("Name:").append(att.getName()).append("\n");
        }

        return sb.toString();
    }

    /**
     * @return the idMessage
     */
    public long getIdMessage() {
        return idMessage;
    }

    /**
     * @param idMessage the idMessage to set
     */
    public void setIdMessage(long idMessage) {
        this.idMessage = idMessage;
    }

    /**
     * @return the receivedDate
     */
    public Date getReceivedDate() {
        return receivedDate;
    }

    /**
     * @param receivedDate the receivedDate to set
     */
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    /**
     * @return the ccList
     */
    public List<String> getCcList() {
        return ccList;
    }

    /**
     * @param ccList the ccList to set
     */
    public void setCcList(List<String> ccList) {
        this.ccList = ccList;
    }

    /**
     * @return the toList
     */
    public List<String> getToList() {
        return toList;
    }

    /**
     * @param toList the toList to set
     */
    public void setToList(List<String> toList) {
        this.toList = toList;
    }

}
