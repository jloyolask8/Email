package com.itcs.commons.email;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.MimeUtility;

/**
 * Hello world!
 *
 */
public class App 
{
//    public static void main( String[] args )
//    {
//        boolean existe = EmailAutoconfigClient.existsAutoconfigSettings("x@gmail.com");
//        System.out.println("existe: "+existe);
//        System.out.println("imap disponible:"+EmailAutoconfigClient.isImapAvailable("x@gmail.com"));
//        System.out.println("pop3 disponible:"+EmailAutoconfigClient.isPop3Available("x@gmail.com"));
//        System.out.println("is gmail:"+EmailAutoconfigClient.isGmailAddress("hola@godesk.cl"));
//        System.out.println("incomming settings: "+EmailAutoconfigClient.getIncommingServerSettings("x@gmail.com","pop3"));
//        System.out.println("outgoing settings: "+EmailAutoconfigClient.getOutgoingServerSettings("x@gmail.com"));
////        EmailAutoconfigClient.testIncommingServerSettings("sr_niceguy@hotmail.com", "imap");
////        EmailAutoconfigClient.getSettings("jorge.flores@itcs.com");
//    }
    
    public static void main(String[] args) {
        
        EmailAutoconfigClient.testServerSettings(createEmailSettingsMap());
        
//        String s = "\"=?ISO-8859-1?Q?Victoria_Riquelme_Mu=F1oz_(Invitaciones_LinkedIn)?=\"".replace("\"", "");
//        try {
//            System.out.println(MimeUtility.decodeText(s));
//        } catch (UnsupportedEncodingException ex) {
//            ex.printStackTrace();
//        }
    }
    
    public static Map<String, String> createEmailSettingsMap() {
        Map<String, String> settings = new HashMap<>();
        settings.put(EnumEmailSettingKeys.INBOUND_SERVER.getKey(), "mail.beltec.cl");
        settings.put(EnumEmailSettingKeys.INBOUND_PORT.getKey(), "143");
        settings.put(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey(), "false");
        settings.put(EnumEmailSettingKeys.INBOUND_STARTTLS.getKey(), "true");
        settings.put(EnumEmailSettingKeys.INBOUND_USER.getKey(), "godesk");
        settings.put(EnumEmailSettingKeys.INBOUND_PASS.getKey(), "GO_01a");
        settings.put(EnumEmailSettingKeys.SERVER_TYPE.getKey(), "popimap");
        settings.put(EnumEmailSettingKeys.STORE_PROTOCOL.getKey(), "imap");
        
        settings.put(EnumEmailSettingKeys.MAIL_DEBUG.getKey(), "true");

        settings.put(EnumEmailSettingKeys.MAIL_SERVER_TYPE_SALIDA.getKey(), "SMTP");
        settings.put(EnumEmailSettingKeys.SMTP_SERVER.getKey(), "mail.beltec.cl");
        settings.put(EnumEmailSettingKeys.SMTP_PORT.getKey(), "25");
        settings.put(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey(), "false");
        settings.put(EnumEmailSettingKeys.SMTP_STARTTLS.getKey(), "true");
        settings.put(EnumEmailSettingKeys.SMTP_USER.getKey(), "godesk");
        settings.put(EnumEmailSettingKeys.SMTP_FROM.getKey(), "godesk");
        settings.put(EnumEmailSettingKeys.SMTP_FROMNAME.getKey(), "SAC Beltec");
        settings.put(EnumEmailSettingKeys.SMTP_PASS.getKey(), "GO_01a");
        
        settings.put(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey(), "10000");
        return settings;
    }
}
