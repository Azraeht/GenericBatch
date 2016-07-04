package org.paris.batch.mailer;

/**
 * Classe listant l'ensemble des clés de fichier de propriété nécessaires pour le paramétrage du Mailer GenericBatch. 
 * @author lannoog
 * @version 1.1 - standardisation des paramètres, ajout du mode onemail
 *
 */
public class MailerParameters {
	/**
	 * Préfixe générique des clés de propriétés à charger
	 */
	public final static String propPrefix = "mail.";
	/** 
	 * Auth : envoi de mail avec authentification
	 */
	public final static String AUTH = "smtp.auth";

	/** 
	 * Auth : envoi de mail avec authentification - nom d'utilisateur
	 */
	public final static String USERNAME = "smtp.username";

	/** 
	 * Auth : envoi de mail avec authentification - mot de passe
	 */
	public final static String PASSWORD = "smtp.password";

	/**
	 * Auth : Mode d'authentification SSL ou TLS
	 */
	public static final String AUTHMODE = "smtp.authmode";
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
	public static final String SMTPPORT = "smtp.port";
	/**
	 * Auth : Hote de connexion smtp
	 */
	public static final String SMTPHOST = "smtp.host";
	/**
	 * Auth : Valide l'utilisation ou non du protocole IPV6
	 */
	public static final String IPV6ENABLE = "smtp.ipv6";
	
	/**
	 * Paramètre message - sujet
	 */
	public static final String MAIL_DEFAULT_SUBJECT = "default.subject";
	
	/**
	 * Expéditeur par défaut du message
	 */
	public static final String MAIL_DEFAULT_SENDER = "default.sender";
	
	/**
	 * Liste des destinataires par défaut du message
	 */
	public static final String MAIL_DEFAULT_SENDTO_LIST = "default.dest";
	
	/**
	 * Liste des destinataires en copie par défaut du message
	 */
	public static final String MAIL_DEFAULT_SENDTO_COPY_LIST = "default.cc";
	
	/**
	 * Liste des destinataires en copie cachée par défaut du message
	 */
	public static final String MAIL_DEFAULT_SENDTO_HIDDEN_LIST = "default.bcc";
}

