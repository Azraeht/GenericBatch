package org.paris.batch.mailer;

public class MailerParameters {
	/** 
	 * Auth : envoi de mail avec authentification
	 */
	public final static String AUTH = "mail.smtp.auth";

	/** 
	 * Auth : envoi de mail avec authentification
	 */
	public final static String USERNAME = "mail.smtp.username";

	/** 
	 * Auth : envoi de mail avec authentification
	 */
	public final static String PASSWORD = "mail.smtp.password";

	/**
	 * Auth : Mode d'authentification SSL ou TLS
	 */
	public static final String AUTHMODE = "mail.smtp.authmode";
	/**
	 * Auth : authentification SSL
	 */
	public static final String AUTHSSL = "ssl";
	/**
	 * Auth : authentificationTLS
	 */
	public static final String AUTHTLS = "tls";

	/**
	 * Auth : Port de connexion smtp
	 */
	public static final String SMTPPORT = "mail.smtp.port";
	/**
	 * Auth : Hote de connexion smtp
	 */
	public static final String SMTPHOST = "mail.smtp.host";
	/**
	 * Auth : Valide l'utilisation ou non du protocole IPV6
	 */
	public static final String IPV6ENABLE = "mail.smtp.ipv6";
}

