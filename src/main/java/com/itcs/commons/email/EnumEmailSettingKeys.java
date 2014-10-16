/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.commons.email;

/**
 *
 * @author jorge
 */
public enum EnumEmailSettingKeys {
    
    SMTP_SERVER("mail_smtp_host"),
    SMTP_PORT("mail_smtp_port"),
    SMTP_USER("mail_smtp_user"),
    SMTP_PASS("mail_smtp_password"),
    SMTP_FROM("mail_smtp_from"),
    SMTP_FROMNAME("mail_smtp_fromname"),
    SMTP_AUTH("mail_smtp_from"),
    SMTP_SOCKET_FACTORY_PORT("mail_smtp_socket_factory_port"),
    SMTP_CONNECTIONTIMEOUT("mail_smtp_connectiontimeout"),
    SMTP_TIMEOUT("mail_smtp_timeout"),
    SERVER_TYPE("mail_server_type"),
    TRANSPORT_PROTOCOL("mail_transport_protocol"),
    STORE_PROTOCOL("mail_store_protocol"),
    TRANSPORT_TLS("mail_transport_tls"),
    USE_JNDI("mail_use_jndi"),
    SESSION_JNDINAME("mail_session_jndiname"),
    INBOUND_SERVER("mail_inbound_host"),
    INBOUND_PORT("mail_inbound_port"),
    INBOUND_USER("mail_inbound_user"),
    INBOUND_PASS("mail_inbound_password"),
    INBOUND_SSL_ENABLED("mail_inbound_ssl_enabled"),
    CHECK_FREQUENCY("email_frecuencia"),
    ACUSE_RECIBO("email_acusederecibo"),
    SUBJECT_ACUSE_RECIBO("subject_resp_automatica"),
    TEXT_ACUSE_RECIBO("texto_resp_automatica"),
    TEXT_CASO_ACUSE_RECIBO("texto_resp_caso"),
    MAIL_DEBUG("mail_debug"),
    DOMINIO_EXCHANGE_SALIDA("dominio_exchange_salida"),
    DOMINIO_EXCHANGE_ENTRADA("dominio_exchange_inbound"),
    MAIL_SERVER_TYPE_SALIDA("mail_server_type_salida"),
    DOWNLOAD_ATTACHMENTS("download_attachments"),
    HIGHEST_UID("hightest_uid"),
    SMTP_SSL_ENABLED("mail_smtp_ssl_enable");
    
    private final String key;
    
    EnumEmailSettingKeys(String key)
    {
        this.key = key;
    }
    
    public String getKey()
    {
        return this.key;
    }
}
