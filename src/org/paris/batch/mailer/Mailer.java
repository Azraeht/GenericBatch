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
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.CannotFindMessageException;
import org.paris.batch.exception.CannotJoinAttachementException;
import org.paris.batch.exception.CannotSendMailException;
import org.paris.batch.exception.CannotWriteTextToMessageException;
import org.paris.batch.exception.ConfigurationBatchException;

/**
 * Classe permettant l'envoi de mails
 * 
 * @author tatons
 * @author Brice SANTUS
 *
 */
public class Mailer {

    protected String to_cc;


    protected String to_bcc;

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
	private Boolean nomail;
	
	private Boolean isAuth = Boolean.FALSE;
	
	private String username = "";
	
	private String password = "";
	
	/**
	 * Constructeur simple d'objet Mailer (apelle le constructeur complet, sans suffixe de filtrage des propriétés)
	 * @see org.paris.batch#Mailer.Mailer(Properties,String,Logger)
	 * @param properties
	 * @param logger
	 */
	public Mailer(Properties properties, Logger logger) throws ConfigurationBatchException
	{
		this(properties, "", logger);
	}
	
	/**
	 * Constructeur : Initialise la session d'envoi de mail en suivant le paramétrage de mail.properties
	 * */
	public Mailer(Properties properties, String propSuffix, Logger logger) throws ConfigurationBatchException
	{	
		// Initialisation des propriétés
		this.setLogger(logger);
		this.setProps(properties);
		Properties sessionProps = new Properties();
		logger.debug("Instanciation du mailer...");
		
		String keyEnableIPV6 = MailerParameters.propPrefix + MailerParameters.IPV6ENABLE + propSuffix;
		String keySMTPHost = MailerParameters.propPrefix + MailerParameters.SMTPHOST + propSuffix;
		String keySMTPPort = MailerParameters.propPrefix + MailerParameters.SMTPPORT + propSuffix;
		String keyAuth = MailerParameters.propPrefix + MailerParameters.AUTH + propSuffix;
		String keyAuthUsername = MailerParameters.propPrefix + MailerParameters.USERNAME + propSuffix;
		String keyAuthPassword = MailerParameters.propPrefix + MailerParameters.PASSWORD + propSuffix;
		String keyAuthMode = MailerParameters.propPrefix + MailerParameters.AUTHMODE + propSuffix;
		
		String defaultSubject = MailerParameters.propPrefix + MailerParameters.MAIL_DEFAULT_SUBJECT + propSuffix;
		String defaultSender = MailerParameters.propPrefix + MailerParameters.MAIL_DEFAULT_SENDER + propSuffix;
		String defaultSendToList = MailerParameters.propPrefix + MailerParameters.MAIL_DEFAULT_SENDTO_LIST + propSuffix;
		String defaultSendToCopyList = MailerParameters.propPrefix + MailerParameters.MAIL_DEFAULT_SENDTO_COPY_LIST + propSuffix;
		String defaultSendToHiddenList = MailerParameters.propPrefix + MailerParameters.MAIL_DEFAULT_SENDTO_HIDDEN_LIST + propSuffix;

		logger.debug("Mailer::Récupération des propriétés");
		try
		{
			if(this.props.getProperty(keyEnableIPV6).equals("false")){
				System.setProperty("java.net.preferIPv4Stack" , "true");
			}
			
			
			// Récupération de l'hôte et du port
			this.host = this.props.getProperty(keySMTPHost);
			this.port = this.props.getProperty(keySMTPPort);
			logger.debug(keySMTPHost + " = " + this.host);
			logger.debug(keySMTPPort + " = " + this.port);
			sessionProps.setProperty(MailerParameters.propPrefix + MailerParameters.SMTPHOST, this.host);
			sessionProps.setProperty(MailerParameters.propPrefix + MailerParameters.SMTPPORT, this.port);
			
			// On crée la session d'envoi de mail
			if (this.props.getProperty(keyAuth).equals("true"))
			{
				logger.debug(keyAuth + " = true");
				this.isAuth = Boolean.TRUE;
				// Si on est en mode authentifié : Récupération du login et mot de passe
				final String username = this.props.getProperty(keyAuthUsername);
				this.username = username;
				sessionProps.setProperty(MailerParameters.propPrefix + MailerParameters.USERNAME, this.username);
				final String password = this.props.getProperty(keyAuthPassword);
				this.password = password;
                sessionProps.setProperty(MailerParameters.propPrefix + MailerParameters.PASSWORD, this.password);
				logger.debug(keyAuthUsername + " = " + this.username);
				logger.debug(keyAuthPassword + " = " + this.password);
				
				// si les infos d'authentification sont présentes
				if(this.username != null && this.password != null && !this.username.equals("") && !this.password.equals(""))
				{
					if(this.props.getProperty(keyAuthMode).equals(MailerParameters.AUTHSSL))
					{
						logger.debug(MailerParameters.AUTHMODE + " = " + MailerParameters.AUTHSSL);
						sessionProps.setProperty(MailerParameters.propPrefix + MailerParameters.AUTHMODE, MailerParameters.AUTHSSL);
						// Mode de connexion SSL
						// Ajout de la classe de connexion SSL
						sessionProps.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
						sessionProps.setProperty("mail.smtp.socketFactory.port",this.props.getProperty(keySMTPPort));
						
						this.setSession(Session.getInstance(sessionProps,
                                    							new javax.mail.Authenticator()
						                                        {
						                                            protected PasswordAuthentication getPasswordAuthentication()
						                                            {
						                                                return new PasswordAuthentication("username", "password");
						                                            }
					                                            }
						                                    ));
					}
					else if(this.props.getProperty(keyAuthMode).equals(MailerParameters.AUTHTLS))
					{
						logger.debug(MailerParameters.AUTHMODE + " = " + MailerParameters.AUTHTLS);
						sessionProps.setProperty(MailerParameters.propPrefix + MailerParameters.AUTHMODE, MailerParameters.AUTHTLS);
						// Mode de connexion TLS
						this.session = Session.getDefaultInstance(sessionProps,
                                        								new javax.mail.Authenticator() {
                                                    							protected PasswordAuthentication getPasswordAuthentication()
                                                    							{
                                                    								return new PasswordAuthentication(username, password);
                                                    							}
                                        						        }
					                                            );
					}
					else
					{
						this.logger.error("Mail : Erreur de création de la session Mail. Le mode d'authentification n'est pas connu (SSL ou TLS acceptés)");
					}
				}
				else
				{
					this.logger.error("MAIL : Impossible de s'authentifier au serveur SMTP, les paramètres mail.smtp.username" +  propSuffix + " et mail.smtp.password ne sont pas renseignés dans mailer.properties");
				}
			}
			else
			{
				this.setSession(Session.getDefaultInstance(sessionProps));
			}
	
	
			// DEBUG : Affichage du paramétrage du mailer
			this.logger.debug("Mail : Paramétrage de session");
			this.logger.debug("Hôte : "+this.host+" : "+this.port);
			if (this.props.getProperty(keyAuth).equals("true")){
				this.logger.debug("Auth : "+this.props.getProperty(keyAuthMode)+" using login: "+this.props.getProperty(keyAuthUsername));
			}else
				this.logger.debug("Auth : Non authentifié");
			if(this.props.getProperty(keyEnableIPV6).equals("false")){
				this.logger.debug("Mail : Mode IPV4");
			}
	
			// Mode no-mail
			if(properties.getProperty(ConfigurationParameters.NOMAIL_KEY).equals("true")){
				this.nomail = Boolean.TRUE;
				this.logger.debug("Mode no-mail : activé (aucun envoi de mail)");
			}else{
				this.nomail = Boolean.FALSE;
                this.logger.debug("Mode no-mail : désactivé (les mails partent)");
			}

			logger.debug("Paramètres par défaut du mail : ");
			//traiter les destinataires par défaut
			this.setSubject(props.getProperty(defaultSubject));
			this.setFrom(props.getProperty(defaultSender));
			this.setTo(props.getProperty(defaultSendToList));
			this.setTo_cc(props.getProperty(defaultSendToCopyList));
			this.setTo_bcc(props.getProperty(defaultSendToHiddenList));
			logger.debug("\tSujet : " + this.getSubject());
			logger.debug("\tExpéditeur par défaut : " + this.getFrom());
			logger.debug("\tDestinataire(s) par défaut : " + this.getTo());
			logger.debug("\tCopies à par défaut : " + this.getTo_cc());
			logger.debug("\tCopies cachées à par défaut : " + this.getTo_bcc());
			
			logger.debug("Instanciation du mailer terminée.");
		}
		catch(Exception e)
		{
			String errMsg = "Une exception inattendue est survenue à l'instanciation du Mailer :\n" + e.getMessage();
			throw new ConfigurationBatchException(errMsg);
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
		this.mainText = "";
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
	private boolean setDestinataire() throws MessagingException
	{
		if(this.getTo() != null)
		{
			try
			{
				InternetAddress[] address = InternetAddress.parse(to, true);
				message.setRecipients(Message.RecipientType.TO, address);
			}
			catch (AddressException ae)
			{
				logger.error("MAIL : Erreur de création de l'adresse du destinataire :\n" + ae.getLocalizedMessage());
				throw new MessagingException(ae.getLocalizedMessage());
			}
			catch (MessagingException me)
			{
				logger.error("MAIL : Erreur d'ajout du destinataire :\n" + me.getLocalizedMessage());
				throw new MessagingException(me.getLocalizedMessage());
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	 /**
     * Méthode permettant de définir les destinataires de copie du mail
     * @throws MessagingException 
     * @throws AddressException 
     */
    private boolean setCopiesA() throws MessagingException
    {
        if(this.getTo_cc() != null)
        {
            try
            {
                InternetAddress[] address = null;
                if(!to_cc.equals(""))
                {
                    address = InternetAddress.parse(to_cc, true);
                }
                message.setRecipients(Message.RecipientType.CC, address);
            }
            catch (AddressException ae)
            {
                logger.error("MAIL : Erreur de création de l'adresse de copie  :\n" + ae.getLocalizedMessage());
                throw new MessagingException(ae.getLocalizedMessage());
            }
            catch (MessagingException me)
            {
                logger.error("MAIL : Erreur d'ajout du destinataire en copie :\n" + me.getLocalizedMessage());
                throw new MessagingException(me.getLocalizedMessage());
            }
            return true;
        }
        else
        {
            return false;
        }
    }
	
    /**
    * Méthode permettant de définir les destinataires de copie du mail
    * @throws MessagingException 
    * @throws AddressException 
    */
   private boolean setCopiesCacheesA() throws MessagingException
   {
       if(this.getTo_bcc() != null)
       {
           
           try
           {
               InternetAddress[] address = null;
               if(!to_bcc.equals(""))
               {
                   address = InternetAddress.parse(to_bcc, true);
               }
               message.setRecipients(Message.RecipientType.BCC, address);
           }
           catch (AddressException ae)
           {
               logger.error("MAIL : Erreur de création de l'adresse de copie  :\n" + ae.getLocalizedMessage());
               throw new MessagingException(ae.getLocalizedMessage());
           }
           catch (MessagingException me)
           {
               logger.error("MAIL : Erreur d'ajout du destinataire en copie :\n" + me.getLocalizedMessage());
               throw new MessagingException(me.getLocalizedMessage());
           }
           return true;
       }
       else
       {
           return false;
       }
   }
    
	/**
	 * Méthode permettant de définir l'emetteur du mail
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	private boolean setExpediteur() throws MessagingException{
		if(this.getFrom() != null)
		{
			try
			{
				message.setFrom(new InternetAddress(from));
			}
			catch (AddressException e)
			{
				logger.error("MAIL : Erreur de création de l'adresse  de l'émetteur :\n" + e.getLocalizedMessage());
				throw new MessagingException(e.getLocalizedMessage());
			}
			catch (MessagingException e)
			{
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
	/**
	 * Méthode permettant d'ajouter une pièce jointe à un mail
	 * @param fileName
	 * @throws CannotJoinAttachementException
	 */
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
		String errMsg = "";
		if(this.message != null)
		{
			try
			{
				if(!this.setDestinataire())
				{
				    errMsg = "Le destinataire du message n'a pas été renseigné. Envoi du mail impossible";
				    logger.error(errMsg);
				    throw new CannotSendMailException(errMsg);
				}
				if(!this.setExpediteur())
				{
				    errMsg = "L'expéditeur du message n'a pas été renseigné. Envoi du mail impossible";
				    logger.error(errMsg);
				    throw new CannotSendMailException(errMsg);
				}
				if(!this.setSujet())
				{
				    errMsg = "Le sujet du message n'a pas été renseigné. Envoi du mail impossible";
				    logger.error(errMsg);
                    throw new CannotSendMailException(errMsg);
				}
				
				//traiter l'ajout éventuel de copies
				this.setCopiesA();
				
				//traiter l'ajout éventuel de copies cachées
				this.setCopiesCacheesA();
				
				// Initialisation du multipart (conteneur général du message)
				this.multipart = new MimeMultipart();

				// créer la partie principale du message
				MimeBodyPart messagePart = new MimeBodyPart();

				logger.debug("Mise en place du texte");
				messagePart.setText(this.mainText);
				this.multipart.addBodyPart(messagePart);

				// Ajout des pièces jointes
                logger.debug("Ajout de pj");
				for (String attachement : this.attachements)
				{
				    logger.debug("et une de plus...");
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(attachement);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(attachement);
					this.multipart.addBodyPart(messageBodyPart);
				}

				// Ajout des pièces jointes Mail
				logger.debug("Ajout de pj mail");
				for (String attachementMail : this.attachementsMail)
				{
				    logger.debug("Ajout de pj mail");
					//on crée une nouvelle partie du message
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					MimeMessage messagetosend;
					try
					{
						messagetosend = this.searchMessage(attachementMail);
					}
					catch (CannotFindMessageException e1)
					{
						throw new CannotSendMailException(e1.getLocalizedMessage());
					}

					logger.debug("Sauvegarde en fichier .eml");
					// On sauvegarde le mail à joindre dans le répertoire temporaire du batch
					String emlfile = (this.props.getProperty(ConfigurationParameters.TEMPDIR)+"/"+attachementMail.replaceAll("<", "").replaceAll(">","")+".eml");
					try
					{
						messagetosend.writeTo(new FileOutputStream(new File(emlfile)));
					}
					catch (FileNotFoundException fnfe)
					{
						logger.error("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+fnfe.getLocalizedMessage());
						throw new CannotJoinAttachementException("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+fnfe.getLocalizedMessage());
					}
					catch (IOException ioe)
					{
						logger.error("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+ioe.getLocalizedMessage());
						throw new CannotJoinAttachementException("MAIL : Impossible de stocker temporairement le mail à mettre en pièce jointe :"+ioe.getLocalizedMessage());
					}

					// On joint le fichier local eml au mail
					DataSource source = new FileDataSource(emlfile);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(messagetosend.getSubject()+".eml");
					this.multipart.addBodyPart(messageBodyPart);
				}
				
				logger.debug("Définition du content");
				this.message.setContent(this.multipart);

				if(this.nomail.equals(Boolean.FALSE))
                {
                    if(this.isAuth.equals(Boolean.TRUE))
					{
					    logger.debug("Connexion pour mode authentifié");	    
				        // Pour le mode connecté on se connect au serveur smtp
					    Transport transport = session.getTransport("smtp");
				        transport.connect(this.host, Integer.parseInt(this.port), this.username, this.password);
				        transport.sendMessage(this.message,this.message.getAllRecipients());
				        this.logger.info("MAIL Envoyé : N°"+this.message.getMessageID());
					}
					else
					{
					    logger.debug("Envoi du message sans authentification");
						Transport.send(this.message);
						this.logger.info("MAIL Envoyé : N°"+this.message.getMessageID());
					}
                }
                else
                {
                    this.logger.info("Mode no-mail : message non envoyé");
                    this.logger.debug("<MESSAGE NON EMIS>");
                    this.logger.debug("\tTo : " + this.getTo());
                    this.logger.debug("\tCC : " + this.getTo_cc());
                    this.logger.debug("\tBCC : " + this.getTo_bcc());
                    this.logger.debug("\tFrom : " + this.getFrom());
                    this.logger.debug("\tSubject : " + this.getSubject());
                    this.logger.debug("\tBody : ");
                    this.logger.debug("\t" + mainText);
                    this.logger.debug("</MESSAGE NON EMIS>");
                }

				// On sauvegarde le mail si nécessaire
				if(keep)
				{
					this.messages.add(this.message);
				}
				id = this.message.getMessageID();
			}
			catch(MessagingException me)
			{
				logger.error("Une erreur s'est manifestée à la fabrication ou la soumission du message :\n" + me.getLocalizedMessage());
				throw new CannotSendMailException(me.getLocalizedMessage());
			}
		}

		// DEBUG : Récapitulatif de l'envoi de mail
		String recap ="";

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
		this.mainText = "";
		this.message = null;
		this.attachements = new ArrayList<String>();
		this.attachementsMail = new ArrayList<String>();

		// On supprime les eml temporaire
//		File folder = new File(this.props.getProperty(ConfigurationParameters.TEMPDIR));
//		String [] listefichiers;
//		int i;
//		listefichiers=folder.list();
//		for(i=0;i<listefichiers.length;i++){
//			if(listefichiers[i].endsWith(".eml")==true){
//				File eml = new File(this.props.getProperty(ConfigurationParameters.TEMPDIR)+"/"+listefichiers[i]);
//				eml.delete();
//			}
//		} 

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
				this.logger.error("Mail : Impossible de rechercher le mail dans la liste des mails sauvegardés : "+e.getLocalizedMessage());
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
				InternetAddress[] address = InternetAddress.parse(to,true);
				msg.setRecipients(Message.RecipientType.TO, address);
				msg.setSubject(titre);
				msg.setSentDate(new Date());
				msg.setText(message);

				// Envoi du message
				Transport.send(msg);
				this.logger.info("MAIL Envoyé");
				this.logger.debug("MAIL Expéditeur : "+this.from);
				this.logger.debug("MAIL Destinataire : "+this.to);
				this.logger.debug("MAIL Objet : "+this.subject);
				this.logger.debug("MAIL Corps : "+this.message);
				
//				if(!this.nocommit){
//					Transport.send(msg);
//					this.logger.info("MAIL Envoyé");
//					this.logger.debug("MAIL Expéditeur : "+this.from);
//					this.logger.debug("MAIL Destinataire : "+this.to);
//					this.logger.debug("MAIL Objet : "+this.subject);
//					this.logger.debug("MAIL Corps : "+this.message);
//				}else{
//					Transport.send(msg);
//					this.logger.info("Mode No-Commit On : Pas d'envoi de mail effectué");
//					this.logger.debug("MAIL Expéditeur : "+this.from);
//					this.logger.debug("MAIL Destinataire : "+this.to);
//					this.logger.debug("MAIL Objet : "+this.subject);
//					this.logger.debug("MAIL Corps : "+this.message);
//				}

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
	public void setFrom(String from)
	{
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

    public String getTo_cc()
    {
        return to_cc;
    }

    public void setTo_cc(String to_cc)
    {
        if(to_cc != null)
            this.to_cc = to_cc;
        else
            this.to_cc = "";
    }


    
    public String getTo_bcc()
    {
        return to_bcc;
    }

    public void setTo_bcc(String to_bcc)
    {
        if(to_bcc != null)
            this.to_bcc = to_bcc;
        else
            this.to_bcc = "";
    }
    
}

