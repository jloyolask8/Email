/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email;

import com.itcs.commons.email.impl.PopImapEmailClientImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

/**
 *
 * @author jorge
 */
public class EmailAutoconfigClient {

    private final static String DEFAULT_CONN_TIMEOUT = "60000";
    private final static String DEFAULT_IO_TIMEOUT = "1200000";

    private static final String MAIL_DEBUG = "mail.debug";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_USER = "mail.smtp.user";
    private static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    private static final String MAIL_SMTP_FROM = "mail.smtp.from";
    private static final String MAIL_SMTP_FROMNAME = "mail.smtp.fromname";
//    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    private static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";
    private static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
    private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
    private static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";
    //----------
    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";
    //----------
    private static final String MAIL_POP_HOST = "mail.pop3s.host";
    private static final String MAIL_POP_PORT = "mail.pop3s.port";
    private static final String MAIL_POP_USER = "mail.pop3s.user";
    private static final String MAIL_POP_PASSWORD = "mail.pop3s.password";
    private static final String MAIL_POP_SSL_ENABLED = "mail.pop3s.ssl.enable";
    //Socket connection timeout value in milliseconds. Default is infinite timeout.
    private static final String MAIL_POP3S_CONN_TIMEOUT = "mail.pop3s.connectiontimeout";
    //Socket I/O timeout value in milliseconds. Default is infinite timeout.
    private static final String MAIL_POP3S_SOCKETIO_TIMEOUT = "mail.pop3s.timeout";
    //----------
    private static final String MAIL_IMAPS_HOST = "mail.imaps.host";
    private static final String MAIL_IMAPS_PORT = "mail.imaps.port";
    private static final String MAIL_IMAPS_USER = "mail.imaps.user";
    private static final String MAIL_IMAPS_PASSWORD = "mail.imaps.password";
    private static final String MAIL_IMAPS_SSL_ENABLED = "mail.imaps.ssl.enable";
    //Socket connection timeout value in milliseconds. Default is infinite timeout.
    private static final String MAIL_IMAPS_CONN_TIMEOUT = "mail.imaps.connectiontimeout";
    private static final String MAIL_IMAP_CONN_TIMEOUT = "mail.imap.connectiontimeout";
    //Socket I/O timeout value in milliseconds. Default is infinite timeout.
    private static final String MAIL_IMAPS_SOCKETIO_TIMEOUT = "mail.imaps.timeout";
    private static final String MAIL_IMAP_SOCKETIO_TIMEOUT = "mail.imap.timeout";
    //----------
    public static final String EMAIL_STR_PATTERN = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";
    public static final String DOMAIN_STR_PATTERN = "(.*)(@)(.*)";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_STR_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern DOMAIN_PATTERN = Pattern.compile(DOMAIN_STR_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final String AUTOCONFIG_URL = "https://autoconfig.thunderbird.net/v1.1/";
    public static final String MX_DNS_URL = "http://mx.thunderbird.net/dns/mx/";
    public static final String GOOGLE_MX = "aspmx.l.google.com";
    private static final Map<String, Document> settingsCache = new HashMap<String, Document>();

    public static Map<String, String> getIncommingServerSettings(String emailAddress, String type) {
        if (existsAutoconfigSettings(emailAddress)) {
            try {
                String domain = "gmail.com";
                if (!isGmailAddress(emailAddress)) {
                    domain = extractDomain(emailAddress);
                }
                Document doc = settingsCache.get(domain);
                Map<String, String> settings = new HashMap<String, String>();
                extractIncommingServerSettings(doc, settings, type);
                settings.put(EnumEmailSettingKeys.STORE_PROTOCOL.getKey(), type);
                return settings;
//                Document doc = convertStringToDocument(getURLContent(AUTOCONFIG_URL + domain));
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return null;
    }

    public static Map<String, String> getAllServerSettings(String emailAddress, String type) {
        if (existsAutoconfigSettings(emailAddress)) {
            try {
                String domain = "gmail.com";
                if (!isGmailAddress(emailAddress)) {
                    domain = extractDomain(emailAddress);
                }
                Document doc = settingsCache.get(domain);
                Map<String, String> settings = new HashMap<String, String>();
                extractIncommingServerSettings(doc, settings, type);
                settings.put(EnumEmailSettingKeys.STORE_PROTOCOL.getKey(), type);
                extractOutgoingServerSettings(doc, settings);
                return settings;
//                Document doc = convertStringToDocument(getURLContent(AUTOCONFIG_URL + domain));
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return null;
    }

    public static boolean testServerSettings(Map<String, String> settings) {
        EmailClient cliente = new PopImapEmailClientImpl(generateEmailProperties(settings));
        try {
            cliente.connectStore();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(EmailAutoconfigClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static Properties generateEmailProperties(Map<String, String> settings) {
        Properties props = new Properties();
        if (settings.get(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()).equalsIgnoreCase("imaps")) {

            props.put(MAIL_IMAPS_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT);
            props.put(MAIL_IMAPS_SOCKETIO_TIMEOUT, DEFAULT_IO_TIMEOUT);

            props.put(MAIL_IMAP_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT);//Try imap even when using imaps
            props.put(MAIL_IMAP_SOCKETIO_TIMEOUT, DEFAULT_IO_TIMEOUT);//Try imap even when using imaps

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_SERVER.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.INBOUND_SERVER.getKey()))) {
                props.put(MAIL_IMAPS_HOST, settings.get(EnumEmailSettingKeys.INBOUND_SERVER.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_PORT.getKey())) {
                props.put(MAIL_IMAPS_PORT, settings.get(EnumEmailSettingKeys.INBOUND_PORT.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_USER.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.INBOUND_USER.getKey()))) {
                props.put(MAIL_IMAPS_USER, settings.get(EnumEmailSettingKeys.INBOUND_USER.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_PASS.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.INBOUND_PASS.getKey()))) {
                props.put(MAIL_IMAPS_PASSWORD, settings.get(EnumEmailSettingKeys.INBOUND_PASS.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey())) {
                props.put(MAIL_IMAPS_SSL_ENABLED, settings.get(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()));
            }

        } else if (settings.get(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()).equalsIgnoreCase("pop3s")) {

            props.put(MAIL_POP3S_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT);
            props.put(MAIL_POP3S_SOCKETIO_TIMEOUT, DEFAULT_IO_TIMEOUT);

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_SERVER.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.INBOUND_SERVER.getKey()))) {
                props.put(MAIL_POP_HOST, settings.get(EnumEmailSettingKeys.INBOUND_SERVER.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_PORT.getKey())) {
                props.put(MAIL_POP_PORT, settings.get(EnumEmailSettingKeys.INBOUND_PORT.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_USER.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.INBOUND_USER.getKey()))) {
                props.put(MAIL_POP_USER, settings.get(EnumEmailSettingKeys.INBOUND_USER.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_PASS.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.INBOUND_PASS.getKey()))) {
                props.put(MAIL_POP_PASSWORD, settings.get(EnumEmailSettingKeys.INBOUND_PASS.getKey()));
            }

            if (settings.containsKey(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey())) {
                props.put(MAIL_POP_SSL_ENABLED, settings.get(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()));
            }
        }
        if (settings.containsKey(EnumEmailSettingKeys.SMTP_SERVER.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_SERVER.getKey()))) {
            props.put(MAIL_SMTP_HOST, settings.get(EnumEmailSettingKeys.SMTP_SERVER.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_PORT.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_PORT.getKey()))) {
            props.put(MAIL_SMTP_PORT, settings.get(EnumEmailSettingKeys.SMTP_PORT.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_USER.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_USER.getKey()))) {
            props.put(MAIL_SMTP_USER, settings.get(EnumEmailSettingKeys.SMTP_USER.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_PASS.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_PASS.getKey()))) {
            props.put(MAIL_SMTP_PASSWORD, settings.get(EnumEmailSettingKeys.SMTP_PASS.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_FROM.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_FROM.getKey()))) {
            props.put(MAIL_SMTP_FROM, settings.get(EnumEmailSettingKeys.SMTP_FROM.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_FROMNAME.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_FROMNAME.getKey()))) {
            props.put(MAIL_SMTP_FROMNAME, settings.get(EnumEmailSettingKeys.SMTP_FROMNAME.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey()))
                && Boolean.getBoolean(settings.get(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey()))) {

            props.put(MAIL_SMTP_SSL_ENABLE, Boolean.TRUE);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            if (settings.containsKey(EnumEmailSettingKeys.SMTP_SOCKET_FACTORY_PORT.getKey())) {
                props.put(MAIL_SMTP_SOCKET_FACTORY_PORT, settings.get(EnumEmailSettingKeys.SMTP_SOCKET_FACTORY_PORT.getKey()));
            } else {
                if (settings.containsKey(EnumEmailSettingKeys.SMTP_PORT.getKey())
                        && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_PORT.getKey()))) {
                    props.put(MAIL_SMTP_SOCKET_FACTORY_PORT, settings.get(EnumEmailSettingKeys.SMTP_PORT.getKey()));
                }
            }
        } else {
            //check TLS
            if (settings.containsKey(EnumEmailSettingKeys.TRANSPORT_TLS.getKey())
                    && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.TRANSPORT_TLS.getKey()))) {
                if (Boolean.getBoolean(settings.get(EnumEmailSettingKeys.TRANSPORT_TLS.getKey()))) {
                    props.put(MAIL_TRANSPORT_TLS, Boolean.TRUE);
                    props.put("mail.smtp.auth", "true");
                } else {
                    props.put(MAIL_TRANSPORT_TLS, Boolean.FALSE);
                }
            }
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey()))) {
            props.put(MAIL_SMTP_CONNECTIONTIMEOUT, settings.get(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.SMTP_TIMEOUT.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.SMTP_TIMEOUT.getKey()))) {
            props.put(MAIL_SMTP_TIMEOUT, settings.get(EnumEmailSettingKeys.SMTP_TIMEOUT.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.TRANSPORT_PROTOCOL.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.TRANSPORT_PROTOCOL.getKey()))) {
            props.put(MAIL_TRANSPORT_PROTOCOL, settings.get(EnumEmailSettingKeys.TRANSPORT_PROTOCOL.getKey()));
        }

        if (settings.containsKey(EnumEmailSettingKeys.STORE_PROTOCOL.getKey())
                && StringUtils.isNotEmpty(settings.get(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()))) {
            props.put(MAIL_STORE_PROTOCOL, settings.get(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()));
        }
        return props;
    }

    public static Map<String, String> getOutgoingServerSettings(String emailAddress) {
        if (existsAutoconfigSettings(emailAddress)) {
            try {
                String domain = "gmail.com";
                if (!isGmailAddress(emailAddress)) {
                    domain = extractDomain(emailAddress);
                }
                Document doc = settingsCache.get(domain);
                Map<String, String> settings = new HashMap<String, String>();
                extractOutgoingServerSettings(doc, settings);
                return settings;
//                Document doc = convertStringToDocument(getURLContent(AUTOCONFIG_URL + domain));
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isGmailAddress(String emailAddress) {
        if (isValidEmail(emailAddress)) {
            try {
                String domain = extractDomain(emailAddress);
                return getURLContent(MX_DNS_URL + domain).toLowerCase().contains(GOOGLE_MX);
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return false;
    }

    private static String extractDomain(String emailAddress) {
        Matcher m = DOMAIN_PATTERN.matcher(emailAddress);
        m.find();
        String domain = m.group(3).toLowerCase();
        return domain;
    }

    public static boolean isImapAvailable(String emailAddress) {
        return existsIncommingType(emailAddress, "imap");
    }

    public static boolean isPop3Available(String emailAddress) {
        return existsIncommingType(emailAddress, "pop3");
    }

    private static boolean existsIncommingType(String emailAddress, String type) {
        if (existsAutoconfigSettings(emailAddress)) {
            try {
                String domain = "gmail.com";
                if (!isGmailAddress(emailAddress)) {
                    domain = extractDomain(emailAddress);
                }
                Document doc = settingsCache.get(domain);
                for (Element element : doc.select("incomingServer")) {
                    if (element.attr("type").equals(type)) {
                        return true;
                    }
                }

            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return false;
    }

    public static boolean existsAutoconfigSettings(String emailAddress) {
        if (isValidEmail(emailAddress)) {
            try {
                String domain = "gmail.com";
                if (!isGmailAddress(emailAddress)) {
                    domain = extractDomain(emailAddress);
                }
                if (settingsCache.containsKey(domain)) {
                    return true;
                }
                String xmlSettings = getURLContent(AUTOCONFIG_URL + domain);
                Document doc = Jsoup.parse(xmlSettings, "", Parser.xmlParser());
                settingsCache.put(domain, doc);
                return true;
//                Document doc = convertStringToDocument(getURLContent(AUTOCONFIG_URL + domain));
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return false;
    }

    private static void extractOutgoingServerSettings(Document doc, Map<String, String> settings) {
        for (Element element : doc.select("outgoingServer")) {
//            System.out.println("element.attr(\"type\"):"+element.attr("type"));
            if (element.attr("type").equals("smtp")) {
//                System.out.println("element.select(\"hostname\"):" + element.select("hostname").text());
                settings.put(EnumEmailSettingKeys.SMTP_SERVER.getKey(), element.select("hostname").text());
//                System.out.println("element.select(\"port\"):" + element.select("port").text());
                settings.put(EnumEmailSettingKeys.SMTP_PORT.getKey(), element.select("port").text());
//                System.out.println("element.select(\"socketType\"):" + element.select("socketType").text());
                settings.put(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey(), element.select("socketType").text().equals("SSL") ? "true" : "false");
                settings.put(EnumEmailSettingKeys.TRANSPORT_TLS.getKey(), element.select("socketType").text().equals("STARTTLS") ? "true" : "false");
            }
        }
    }

    private static void extractIncommingServerSettings(Document doc, Map<String, String> settings, String type) {
        for (Element element : doc.select("incomingServer")) {
//            System.out.println("element.attr(\"type\"):"+element.attr("type"));
            if (element.attr("type").equals(type)) {
//                System.out.println("element.select(\"hostname\"):" + element.select("hostname").text());
                settings.put(EnumEmailSettingKeys.INBOUND_SERVER.getKey(), element.select("hostname").text());
//                System.out.println("element.select(\"port\"):" + element.select("port").text());
                settings.put(EnumEmailSettingKeys.INBOUND_PORT.getKey(), element.select("port").text());
//                System.out.println("element.select(\"socketType\"):" + element.select("socketType").text());
                settings.put(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey(), element.select("socketType").text().trim().equals("SSL") ? "true" : "false");
            }
        }
    }

    public static boolean isValidEmail(String email) {
        if (email != null) {
            Matcher m = EMAIL_PATTERN.matcher(email);
            return m.find();
        }
        return false;
    }

    private static String getURLContent(String p_sURL) throws MalformedURLException, IOException {
        URL oURL;
        URLConnection oConnection;
        BufferedReader oReader;
        String sLine;
        StringBuilder sbResponse;
        String sResponse = null;

        oURL = new URL(p_sURL);
        oConnection = oURL.openConnection();
        oReader = new BufferedReader(new InputStreamReader(oConnection.getInputStream()));
        sbResponse = new StringBuilder();

        while ((sLine = oReader.readLine()) != null) {
            sbResponse.append(sLine);
        }

        sResponse = sbResponse.toString();

        return sResponse;
    }
}
