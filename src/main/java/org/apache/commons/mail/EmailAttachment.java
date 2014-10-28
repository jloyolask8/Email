/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail;

import java.net.URL;

/**
 * This class models an email attachment. Used by MultiPartEmail.
 *
 * @since 1.0
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @version $Id: EmailAttachment.java 480401 2006-11-29 04:40:04Z bayard $
 */
public class EmailAttachment {

    /**
     * Definition of the part being an attachment
     */
    public static final String ATTACHMENT = javax.mail.Part.ATTACHMENT;
    /**
     * Definition of the part being inline
     */
    public static final String INLINE = javax.mail.Part.INLINE;
    /**
     * The name of this attachment.
     */
    private String name = "";
    /**
     * The description of this attachment.
     */
    private String description = "";
    /**
     * The path to this attachment (ie c:/path/to/file.jpg).
     */
    private String path = "";
    /**
     * The HttpURI where the file can be got.
     */
    private URL url;
    private byte[] data = null;
    private String mimeType = null;
    private String contentId;
    /**
     * The disposition.
     */
    private String disposition = EmailAttachment.ATTACHMENT;
    /**
     * The attachment file size.
     */
    private long size;

    /**
     * Get the description.
     *
     * @return A String.
     * @since 1.0
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the name.
     *
     * @return A String.
     * @since 1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Get the path.
     *
     * @return A String.
     * @since 1.0
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the URL.
     *
     * @return A URL.
     * @since 1.0
     */
    public URL getURL() {
        return url;
    }

    /**
     * Get the disposition.
     *
     * @return A String.
     * @since 1.0
     */
    public String getDisposition() {
        return disposition;
    }

    /**
     * Set the description.
     *
     * @param desc A String.
     * @since 1.0
     */
    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * Set the name.
     *
     * @param aName A String.
     * @since 1.0
     */
    public void setName(String aName) {
        this.name = aName;
    }

    /**
     * Set the path to the attachment. The path can be absolute or relative and
     * should include the filename. <p> Example: /home/user/images/image.jpg<br>
     * Example: images/image.jpg
     *
     * @param aPath A String.
     * @since 1.0
     */
    public void setPath(String aPath) {
        this.path = aPath;
    }

    /**
     * Set the URL.
     *
     * @param aUrl A URL.
     * @since 1.0
     */
    public void setURL(URL aUrl) {
        this.url = aUrl;
    }

    /**
     * Set the disposition.
     *
     * @param aDisposition A String.
     * @since 1.0
     */
    public void setDisposition(String aDisposition) {
        this.disposition = aDisposition;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the size
     */
    public long getSize() {
        if (this.data != null && (size == 0)) {
            return this.data.length;
        }
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the contentId
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}
