package com.itcs.commons.email;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        boolean existe = EmailAutoconfigClient.existsAutoconfigSettings("x@gmail.com");
        System.out.println("existe: "+existe);
        System.out.println("imap disponible:"+EmailAutoconfigClient.isImapAvailable("x@gmail.com"));
        System.out.println("pop3 disponible:"+EmailAutoconfigClient.isPop3Available("x@gmail.com"));
        System.out.println("is gmail:"+EmailAutoconfigClient.isGmailAddress("hola@godesk.cl"));
        System.out.println("incomming settings: "+EmailAutoconfigClient.getIncommingServerSettings("x@gmail.com","pop3"));
        System.out.println("outgoing settings: "+EmailAutoconfigClient.getOutgoingServerSettings("x@gmail.com"));
//        EmailAutoconfigClient.testIncommingServerSettings("sr_niceguy@hotmail.com", "imap");
//        EmailAutoconfigClient.getSettings("jorge.flores@itcs.com");
    }
}
