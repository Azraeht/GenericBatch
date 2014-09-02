package org.paris.batch.mailer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.apache.log4j.Logger;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.CannotFindMessageException;
import org.paris.batch.exception.CannotJoinAttachementException;
import org.paris.batch.exception.CannotSendMailException;
import org.paris.batch.exception.CannotWriteTextToMessageException;
import org.paris.batch.exception.GeneralMailingException;

/**
 * Classe permettant l'envoi de mails
 * 
 * @author tatons
 * @author Brice SANTUS
 *
 */
public class Mailer {

	/**
	 * Propriéré contenant l'adresse utilisée
	 */
	protected String from;

	/**
	 *Propriété contenant l'adresse de destination
	 */
	protected String to;

	/**
	 *Propriété contenant le sujet du mail
	 */
	protected String subject;

	/**
	 * Propriété contenant l'adresse hote
	 */
	protected String host;

	/**
	 * Journal d'evenements
	 */
	protected Logger logger;

	/**
	 * Reçoit les informations spécifiées dans le fichier de configuration
	 */
	protected Properties props;

	/**
	 * Contient le mail
	 */
	protected MimeMessage message = null;

	/**
	 * Permet de verifer si le mail contient des pièces jointes
	 */
	protected boolean haveAttachment;

	/**
	 * Propriété récupérant toutes les pièces jointes du mail
	 */
	protected Multipart multipart = null;

	/**
	 * Propriété contenant la session d'envoi de mail
	 */
	protected Session session ;

	/**
	 * Propriété contenant le texte principale du mail
	 */
	protected String mainText = "";

	/**
	 * Propriété contenant la liste des pièces jointes
	 */
	protected ArrayList<String> attachements = new ArrayList<String>(); 

	/**
	 * Propriété contenant la liste des pièces jointes mail
	 */
	protected ArrayList<String> attachementsMail = new ArrayList<String>(); 

	/**
	 * Propriété contenant la liste des messages sauvegardés
	 */
	protected ArrayList<MimeMessage> messages = new ArrayList<MimeMessage>();

	/**
	 * Propriété contenant le port sur serveur d'envoi de mail 
	 */
	private String port;

	/**
	 *  Propriété contenant la valeur du mode no-commit
	 */
	private Boolean nocommit;

	/**
	 * Constructeur : Initialise la session d'envoi de mail en suivant le paramétrage de mail.properties
	 * */
	public Mailer(Properties properties, Logger logger)
	{	
		// Initialisation des propriétés
		this.setLogger(logger);
		this.setProps(properties);

		if(this.props.getProperty(MailerParameters.IPV6ENABLE).equals("false")){
			System.setProperty("java.net.preferIPv4Stack" , "true");
		}

		// Récupération de l'hôte et du port
		this.host = this.props.getProperty(MailerParameters.SMTPHOST);
		this.port = this.props.getProperty(MailerParameters.SMTPPORT);

		// On crée la session d'envoi de mail
		if (this.props.getProperty(MailerParameters.AUTH).equals("true")){

			// Si on est en mode authentifié : Récupération du login et mot de passe
			final String username = this.props.getProperty(MailerParameters.USERNAME); 
			final String password = this.props.getProperty(MailerParameters.PASSWORD);



			// si les info d'authentificatino sont présentes
			if(username != null && password != null && !username.equals("") && !password.equals("")){

				if(this.props.getProperty(MailerParameters.AUTHMODE).equals(MailerParameters.AUTHSSL)){
					// Mode de connexion SSL
					// Ajout de la classe de connexion SSL
					props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
					props.put("mail.smtp.socketFactory.port",this.props.getProperty(MailerParameters.SMTPPORT));
					this.setSession(Session.getInstance(props,
							new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication("username", "password");
						}
					}));
				}else if(this.props.getProperty(MailerParameters.AUTHMODE).equals(MailerParameters.AUTHTLS)){
					// Mode de connexion TLS
					this.session = Session.getDefaultInstance(props,
							new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username,password);
						}
					});
				}else{
					this.logger.error("Mail : Erreur de création de la session Mail. Le mode d'authentification n'est pas connu (SSL ou TLS acceptés)");
				}
			}else{
				this.logger.error("MAIL : Impossible de s'authentifier au serveur SMTP, les paramètres mail.smtp.username et mail.smtp.password ne sont pas renseignés dans mailer.properties");
			}
		}else{
			this.setSession(Session.getDefaultInstance(this.props));
		}


		// DEBUG : Affichage du paramétrage du mailer
		this.logger.debug("Mail : Paramétrage de session");
		this.logger.debug("Hôte : "+this.host+" : "+this.port);
		if (this.props.getProperty(MailerParameters.AUTH).equals("true")){
			this.logger.debug("Auth : "+this.props.getProperty(MailerParameters.AUTHMODE)+" using login: "+this.props.getProperty(MailerParameters.USERNAME));
		}else
			this.logger.debug("Auth : Non authentifié");
		if(this.props.getProperty(MailerParameters.IPV6ENABLE).equals("false")){
			this.logger.debug("Mail : Mode IPV4");
		}

		// Mode no-commit
		if(properties.getProperty(ConfigurationParameters.NOCOMMIT_KEY).equals("true")){
			this.nocommit = true;
		}else{
			this.nocommit = false;
		}
	}

	/**
	 * Initialise un nouveau message (à ce stade, le message ne contient rien du tout).
	 * On suppose par défaut qu'il n'y aura pas de PJ (mais ça peut se changer après)
	 */
	public void newMessage()
	{
		message = new MimeMessage(session);
		haveAttachment=false;
		try
		{
			logger.debug("Mail : Nouveau message créé (ID = " + message.getMessageID() + ")");
		}
		catch(MessagingException me)
		{
			logger.error("Mail : Erreur lors de la création de l'ID du message \n" + me.getLocalizedMessage());			
		}
	}

	/**
	 * Méthode permettant d'ajouter du texte dans le mail à envoyer
	 * @param text
	 * @throws MessagingException 
	 */
	public void setMainText(String text) throws CannotWriteTextToMessageException
	{
		this.mainText = text;
	}

	/**
	 * Ajoute au contenu du corps de texte la chaîne passée en paramètre.
	 * NB : tant que le corps de texte n'a pas été explicitement inclus dans le message
	 * cette méthode n'a pas d'effet sur le message
	 * @param textToAppend la chaîne à ajouter en fin de corps de texte
	 * @param newLine booléen indiquant s'il faut ou non insérer un retour chariot au moment de l'ajout
	 */
	private void appendToMainText(String textToAppend, boolean newLine)
	{
		this.mainText += textToAppend;
		if(newLine==true){
			mainText+="\n";
		}
	}

	/**
	 * Ajoute au contenu du corps de texte la chaîne passée en paramètre, sans insérer de retour chariot
	 * @param textToAppend la chaîne à ajouter en fin de corps de texte
	 */
	public void appendToMainText(String textToAppend)
	{
		appendToMainText(textToAppend, false);
	}

	/**
	 * Ajoute au contenu du corps de texte la chaîne passée en paramètre, avec insertion de retour chariot
	 * @param textToAppend la chaîne à ajouter en fin de corps de texte
	 */
	public void appendToMainTextWithNewLine(String textToAppend)
	{
		appendToMainText(textToAppend, true);
	}

	/**
	 * Méthode permettant de définir le destinataire du mail
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	private boolean setDestinataire() throws MessagingException{
		if(this.getTo() != null){
			try {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			} catch (AddressException e) {
				logger.error("MAIL : Erreur de création de l'adresse du destinataire :\n" + e.getLocalizedMessage());
				throw new MessagingException(e.getLocalizedMessage());
			} catch (MessagingException e) {
				logger.error("MAIL : Erreur d'ajout du destinataire :\n" + e.getLocalizedMessage());
				throw new MessagingException(e.getLocalizedMessage());
			}
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Méthode permettant de définir l'emetteur du mail
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	private boolean setExpediteur() throws MessagingException{
		if(this.getFrom() != null){
			try {
				message.setFrom(new InternetAddress(from));
			} catch (AddressException e) {
				logger.error("MAIL : Erreur de création de l'adresse  de l'émetteur :\n" + e.getLocalizedMessage());
				throw new MessagingException(e.getLocalizedMessage());
			} catch (MessagingException e) {
				logger.error("MAIL : Erreur d'ajout de l'émetteur :\n" + e.getLocalizedMessage());
				throw new MessagingException(e.getLocalizedMessage());
			}
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Méthode permettant de définir le sujet du mail
	 * @throws MessagingException
	 */
	public boolean setSujet() throws MessagingException{
		if(this.getSubject() != null){
			try
			{
				this.message.setSubject(this.subject);
				return true;
			}
			catch(MessagingException me)
			{
				logger.error("Erreur à l'écriture du texte du message :\n" + me.getLocalizedMessage());
				throw new MessagingException(me.getLocalizedMessage());
			}
		}
		else{
			return false;
		}
	}

	/**
	 * Méthode permettant d'ajouter un mail en tant que pièce jointe
	 * @param idMail : id de l'E-mail à joindre
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public void addMailAsAttachment(String idMail) throws MessagingException, IOException{
		//on crée une nouvelle partie du message
		this.attachementsMail.add(idMail);
		//le boolean est maintenant true car le mail contient une pièce jointe
		if(haveAttachment==false){
			haveAttachment=true;
		}	
	}

	public void addMailAttachement(String fileName) throws CannotJoinAttachementException{
		//on crée une nouvelle partie du message
		this.attachements.add(fileName);
		//le boolean est maintenant true car le mail contient une pièce jointe
		if(haveAttachment==false){
			haveAttachment=true;
		}	
	}
	/**
	 * Méthode permettant l'envoi d'un mail
	 * @param keep : Défini le message doit être sauvegardé par le mailer après envoi
	 * @return id : id du mail envoyé (0 si echec de l'envoi de mail)
	 * @throws CannotSendMailException signale que l'envoi du mail a échoué
	 * @throws CannotJoinAttachementException 
	 */
	public String send(boolean keep) throws CannotSendMailException, CannotJoinAttachementException
	{
		String id = "";
		this.newMessage();
		if(this.message != null){
			try
			{
				if(this.setDestinataire()){

					if(this.setExpediteur()){

						if(this.setSujet()){

							// Initialisation du multipart
							this.multipart = new MimeMultipart();

							// Ajout du corps
							MimeBodyPart messagePart = new MimeBodyPart();
							messagePart.setText(this.mainText);
							multipart.addBodyPart(messagePart);

							// Ajout des pièces jointes
							for (String attachement : this.attachements) {				
								MimeBodyPart messageBodyPart = new MimeBodyPart();
								DataSource source = new FileDataSource(attachement);
								messageBodyPart.setDataHandler(new DataHandler(source));
								messageBodyPart.setFileName(attachement);
								multipart.addBodyPart(messageBodyPart);
							}

							// Ajout des pièces jointes Mail
							for (String attachementMail : this.attachementsMail) {				
								//on crée une nouvelle partie du message
								MimeBodyPart messageBodyPart = new MimeBodyPart();
								MimeMessage messagetosend;
								try {
									messagetosend = this.searchMessage(attachementMail);
								} catch (CannotFindMessageException e1) {
									throw new CannotSendMailException(e1.getLocalizedMessage());
								}

								// On sauvegarde le mail à joindre dans le répertoire temporaire du batch
								String emlfile = (this.props.getProperty(ConfigurationParameters.TEMPDIR)+"/"+attachementMail.replaceAll("<", "").replaceAll(">","")+".eml");
								try {
									messagetosend.writeTo(new FileOutputStream(new File(emlfile)));
								} catch (FileNotFoundException e) {
									logger.error("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+e.getLocalizedMessage());
									throw new CannotJoinAttachementException("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+e.getLocalizedMessage());
								} catch (IOException e) {
									logger.error("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+e.getLocalizedMessage());
									throw new CannotJoinAttachementException("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+e.getLocalizedMessage());
								}

								// On joint le fichier local eml au mail
								DataSource source = new FileDataSource(emlfile);
								messageBodyPart.setDataHandler(new DataHandler(source));
								messageBodyPart.setFileName(messagetosend.getSubject()+".eml");
								multipart.addBodyPart(messageBodyPart);
							}
							this.message.setContent(this.multipart);


							if(this.props.getProperty(MailerParameters.AUTH).equals("true")){
								// Pour le mode connecté on se connect au serveur smtp
								if(!this.nocommit){
									Transport transport = session.getTransport("smtp");
									transport.connect(this.host, Integer.parseInt(this.port), this.props.getProperty(MailerParameters.USERNAME), this.props.getProperty(MailerParameters.PASSWORD));
									transport.sendMessage(this.message,this.message.getAllRecipients());
									this.logger.info("MAIL Envoyé : N°"+this.message.getMessageID());
								}else{
									this.logger.info("Mode No-Commit On : Pas d'envoi de mail effectué");
								}
							}else{
								if(!this.nocommit){
									Transport.send(this.message);
									this.logger.info("MAIL Envoyé : N°"+this.message.getMessageID());
								}else{
									this.logger.info("Mode No-Commit On : Pas d'envoi de mail effectué");
								}
							}

							// On sauvegarde le mail si nécessaire
							if(keep){
								this.messages.add(this.message);
							}
							id = this.message.getMessageID();
						}else
						{
							logger.error("MAIL : Pas de sujet défini, le mail n'a pas été envoyé");
							throw new CannotSendMailException("MAIL : Pas de sujet défini, le mail n'a pas été envoyé");
						}
					}else
					{
						logger.error("MAIL : Pas d'expéditeur défini, le mail n'a pas été envoyé");
						throw new CannotSendMailException("MAIL : Pas d'expéditeur défini, le mail n'a pas été envoyé");
					}
				}else{
					logger.error("MAIL : Pas d'émetteur défini, le mail n'a pas été envoyé");
					throw new CannotSendMailException("MAIL : Pas d'émetteur défini, le mail n'a pas été envoyé");
				}
			}
			catch(MessagingException me)
			{
				logger.error("Weuhl, mon mail y part pà ! Méga glandasses !\n" + me.getLocalizedMessage());
				throw new CannotSendMailException(me.getLocalizedMessage());
			}
		}

		// DEBUG : Récapitulatif de l'envoi de mail
		String recap ="";

		this.logger.debug("Mail : Récapitulatif envoi de mail");
		this.logger.debug("ID : "+id+"(Sauvegardé: "+keep);

		this.logger.debug("Emetteur : "+this.from);
		this.logger.debug("Destinataire : "+this.to);
		this.logger.debug("Sujet : "+this.subject);
		for (String attachement : this.attachements) {
			recap+=attachement+" / ";
		}
		this.logger.debug("Pièces jointes : "+recap);
		this.logger.debug("Corps : "+this.mainText);


		// Message envoyé, on vide les informations en attente
		this.cleanMessage();

		return id;
	}

	/**
	 * Méthode permetant de vider les informations en attente d'envoi
	 */
	private void cleanMessage() {

		// On vide les info du mail
		this.from = null;
		this.to = null;
		this.subject = null;
		this.mainText = null;
		this.message = null;
		this.attachements = new ArrayList<String>();
		this.attachementsMail = new ArrayList<String>();

		// On supprime les eml temporaire
		File folder = new File(this.props.getProperty(ConfigurationParameters.TEMPDIR));
		String [] listefichiers;
		int i;
		listefichiers=folder.list();
		for(i=0;i<listefichiers.length;i++){
			if(listefichiers[i].endsWith(".eml")==true){
				File eml = new File(this.props.getProperty(ConfigurationParameters.TEMPDIR)+"/"+listefichiers[i]);
				eml.delete();
			}
		} 

	}
	/**
	 * Méthode de recherche de Message dans la liste des messages sauvegardés
	 * @param id du message
	 * @return Message envoyé
	 * @throws CannotFindMessageException 
	 */
	protected MimeMessage searchMessage(String id) throws CannotFindMessageException{
		MimeMessage mes = null;
		for (MimeMessage messave : this.messages) {
			try {
				if(messave.getMessageID().equals(id))
					mes = messave;
			} catch (MessagingException e) {
				this.logger.error("Mail : Impossible de recherche le mail dans la liste des mails sauvegardés : "+e.getLocalizedMessage());
				throw new CannotFindMessageException("Mail : Impossible de recherche le mail dans la liste des mails sauvegardés : "+e.getLocalizedMessage());
			}
		}
		return mes;
	}

	/**
	 * Méthode simple d'envoi de mail à la volée
	 */
	public void quickSend(String from,String to,String titre,String message,Properties props) throws CannotSendMailException{

		// Création d'une session simple
		props.put("mail.transport.protocol", "smtp");
		Session session = Session.getDefaultInstance(props, null);
		try {
			if(!this.props.getProperty(MailerParameters.AUTH).equals("true")){
				// Création du message
				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(from));
				InternetAddress[] address = {new InternetAddress(to)};
				msg.setRecipients(Message.RecipientType.TO, address);
				msg.setSubject(titre);
				msg.setSentDate(new Date());
				msg.setText(message);

				// Envoi du message
				if(!this.nocommit){
					Transport.send(msg);
					this.logger.info("MAIL Envoyé");
					this.logger.debug("MAIL Expéditeur : "+this.from);
					this.logger.debug("MAIL Destinataire : "+this.to);
					this.logger.debug("MAIL Objet : "+this.subject);
				}else{
					this.logger.info("Mode No-Commit On : Pas d'envoi de mail effectué");
					this.logger.debug("MAIL Expéditeur : "+this.from);
					this.logger.debug("MAIL Destinataire : "+this.to);
					this.logger.debug("MAIL Objet : "+this.subject);
				}

			}else{
				this.logger.info("Erreur envoi message : QuickSend ne permet pas le mode d'envoi Authentifié"); 
			}
		}
		catch (Exception me) {
			logger.error("\n" + me.getMessage());
			throw new CannotSendMailException(me.getMessage());
		}
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

	public MimeMessage getMessage(){
		return message;
	}

	/**
	 * 
	 * @return du texte
	 */
	public String getMainText(){
		return mainText;
	}


	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}


	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}


	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}


	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}


	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}


	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}


	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}


	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}


	/**
	 * @return the props
	 */
	public Properties getProps() {
		return props;
	}


	/**
	 * @param props the props to set
	 */
	public void setProps(Properties props) {
		this.props = props;
	}


	/**
	 * @return the haveAttachment
	 */
	public boolean isHaveAttachment() {
		return haveAttachment;
	}


	/**
	 * @param haveAttachment the haveAttachment to set
	 */
	public void setHaveAttachment(boolean haveAttachment) {
		this.haveAttachment = haveAttachment;
	}


	/**
	 * @return the multipart
	 */
	public Multipart getMultipart() {
		return multipart;
	}


	/**
	 * @param multipart the multipart to set
	 */
	public void setMultipart(Multipart multipart) {
		this.multipart = multipart;
	}


	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}


	/**
	 * @param session the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}


	/**
	 * @param message the message to set
	 */
	public void setMessage(MimeMessage message) {
		this.message = message;
	}


}

