package org.paris.batch.mailer;

import java.io.IOException;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.apache.log4j.Logger;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.logging.LogBatch;
import org.paris.batch.exception.CannotSendMailException;
import org.paris.batch.exception.CannotWriteTextToMessageException;
import org.paris.batch.exception.GeneralMailingException;
import org.paris.batch.exception.MailSendFailureException;

/**
 * Classe permettant l'envoi de mails
 * 
 * @author tatons
 *
 */
public class Mailer {
	
	/**
	 * Propriété contenant l'adresse utilisée
	 */
	protected String from;
	
	/**
	 *Propriété contenant l'adresse de destination
	 */
    protected String to;
	
	/**
	 * Propriété contenant l'adresse hote
	 */
	protected String host;
		
	/**
	 * journal d'evenements
	 */
	protected Logger logger;
	
	/**
     * reçoit les informations spécifiées dans le fichier de configuration
     */
    protected Properties props;
    
    /**
     * contient le mail
     */
	protected MimeMessage message;
	
	/**
	 * permet de verifer si le mail contient des pièces jointes
	 */
	protected boolean haveAttachment;
	
	/**
	 * proprieté récupérant toutes les pièces jointes du mail
	 */
	protected Multipart multipart;
	
	protected Session session ;
	
	protected String mainText ;
	
	
	public Mailer(Properties properties, Logger logger)
	{	
		 
	    // Initialisation des propriétés
		this.logger = logger;
		this.props = properties;
	    String smtp=this.props.getProperty("mail.smtp.host");
	    props.setProperty(smtp, host);
	    
	    // Get the default Session object.
	    session = Session.getDefaultInstance(props);
	    
	    // Create a default MimeMessage object.
		//multipart = new MimeMultipart();
		
		 
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
	
	public void addMainText(String text){
		mainText+=text;
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
			logger.debug("Nouveau message créé (ID = " + message.getMessageID() + ")");
		}
		catch(MessagingException me)
		{
			logger.error("Ouhlà, j'ai créé un nouveau message et ce gros chacal veut pas me donner son ID... ah l'bâtard !\n" + me.getLocalizedMessage());			
		}
	}
	
	/**
	 * Méthode permettant d'ajouter du texte dans le mail à envoyer
	 * @param text
	 * @throws MessagingException 
	 */
	public void setText(String text) throws CannotWriteTextToMessageException
	{
		try
		{
			((MimeMessage) message).setText(text);
		}
		catch(MessagingException me)
		{
			logger.error("Erreur à l'écriture du texte du message :\n" + me.getLocalizedMessage());
			throw new CannotWriteTextToMessageException(me.getLocalizedMessage());
		}
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
		mainText += textToAppend;
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
	 * méthode permettant de définir le destinataire du mail
	 * @param mail
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public void setDestinataire(String mail){
		to=mail;
		try {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Méthode permettant de définir le sujet du mail
	 * @param subject
	 * @throws MessagingException
	 */
	public void setSujet(String subject) throws CannotWriteTextToMessageException{
		try
		{
			((MimeMessage) this.message).setSubject(subject);
		}
		catch(MessagingException me)
		{
			logger.error("Erreur à l'écriture du texte du message :\n" + me.getLocalizedMessage());
			throw new CannotWriteTextToMessageException(me.getLocalizedMessage());
		}
	}
	
	/**
	 * Méthode permettant d'ajouter un mail en tant que pièce jointe
	 * @param attachment
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public void addMailAsAttachment(MimeMessage attachment,int numMail) throws MessagingException, IOException{
			//on crée une nouvelle partie du message
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(new MimeMessage(attachment),"mail n°"+numMail);
			multipart.addBodyPart(messageBodyPart);
			//le boolean est maintenant true car le mail contient une pièce jointe
			if(haveAttachment==false){
				haveAttachment=true;
			}	
	}
	
	/**
	 * Méthode permettant d'ajouter le contenu du multipart dans le mail
	 * @throws MessagingException
	 */
	public void addMultipartInMail() throws MessagingException{
		message.setContent(multipart);
	}
	
	/**
	 * Méthode permettant l'envoi d'un mail
	 * @throws CannotSendMailException signale que l'envoi du mail a échoué
	 */
	public void send() throws CannotSendMailException
	{
		try
		{
			Transport.send(message);
		}
		catch(MessagingException me)
		{
			logger.error("Weuhlà, mon mail y part pô ! Méga glandasses !\n" + me.getLocalizedMessage());
			throw new CannotSendMailException(me.getLocalizedMessage());
		}
	}
}

