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
	 * Propri�t� contenant l'adresse utilis�e
	 */
	protected String from;
	
	/**
	 *Propri�t� contenant l'adresse de destination
	 */
    protected String to;
	
	/**
	 * Propri�t� contenant l'adresse hote
	 */
	protected String host;
		
	/**
	 * journal d'evenements
	 */
	protected Logger logger;
	
	/**
     * re�oit les informations sp�cifi�es dans le fichier de configuration
     */
    protected Properties props;
    
    /**
     * contient le mail
     */
	protected MimeMessage message;
	
	/**
	 * permet de verifer si le mail contient des pi�ces jointes
	 */
	protected boolean haveAttachment;
	
	/**
	 * propriet� r�cup�rant toutes les pi�ces jointes du mail
	 */
	protected Multipart multipart;
	
	protected Session session ;
	
	protected String mainText ;
	
	
	public Mailer(Properties properties, Logger logger)
	{	
		 
	    // Initialisation des propri�t�s
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
	 * Initialise un nouveau message (� ce stade, le message ne contient rien du tout).
	 * On suppose par d�faut qu'il n'y aura pas de PJ (mais �a peut se changer apr�s)
	 */
	public void newMessage()
	{
		message = new MimeMessage(session);
		haveAttachment=false;
		try
		{
			logger.debug("Nouveau message cr�� (ID = " + message.getMessageID() + ")");
		}
		catch(MessagingException me)
		{
			logger.error("Ouhl�, j'ai cr�� un nouveau message et ce gros chacal veut pas me donner son ID... ah l'b�tard !\n" + me.getLocalizedMessage());			
		}
	}
	
	/**
	 * M�thode permettant d'ajouter du texte dans le mail � envoyer
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
			logger.error("Erreur � l'�criture du texte du message :\n" + me.getLocalizedMessage());
			throw new CannotWriteTextToMessageException(me.getLocalizedMessage());
		}
	}
	
	/**
	 * Ajoute au contenu du corps de texte la cha�ne pass�e en param�tre.
	 * NB : tant que le corps de texte n'a pas �t� explicitement inclus dans le message
	 * cette m�thode n'a pas d'effet sur le message
	 * @param textToAppend la cha�ne � ajouter en fin de corps de texte
	 * @param newLine bool�en indiquant s'il faut ou non ins�rer un retour chariot au moment de l'ajout
	 */
	private void appendToMainText(String textToAppend, boolean newLine)
	{
		mainText += textToAppend;
		if(newLine==true){
			mainText+="\n";
		}
	}
	
	/**
	 * Ajoute au contenu du corps de texte la cha�ne pass�e en param�tre, sans ins�rer de retour chariot
	 * @param textToAppend la cha�ne � ajouter en fin de corps de texte
	 */
	public void appendToMainText(String textToAppend)
	{
		appendToMainText(textToAppend, false);
	}

	/**
	 * Ajoute au contenu du corps de texte la cha�ne pass�e en param�tre, avec insertion de retour chariot
	 * @param textToAppend la cha�ne � ajouter en fin de corps de texte
	 */
	public void appendToMainTextWithNewLine(String textToAppend)
	{
		appendToMainText(textToAppend, true);
	}
	
	/**
	 * m�thode permettant de d�finir le destinataire du mail
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
	 * M�thode permettant de d�finir le sujet du mail
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
			logger.error("Erreur � l'�criture du texte du message :\n" + me.getLocalizedMessage());
			throw new CannotWriteTextToMessageException(me.getLocalizedMessage());
		}
	}
	
	/**
	 * M�thode permettant d'ajouter un mail en tant que pi�ce jointe
	 * @param attachment
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public void addMailAsAttachment(MimeMessage attachment,int numMail) throws MessagingException, IOException{
			//on cr�e une nouvelle partie du message
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(new MimeMessage(attachment),"mail n�"+numMail);
			multipart.addBodyPart(messageBodyPart);
			//le boolean est maintenant true car le mail contient une pi�ce jointe
			if(haveAttachment==false){
				haveAttachment=true;
			}	
	}
	
	/**
	 * M�thode permettant d'ajouter le contenu du multipart dans le mail
	 * @throws MessagingException
	 */
	public void addMultipartInMail() throws MessagingException{
		message.setContent(multipart);
	}
	
	/**
	 * M�thode permettant l'envoi d'un mail
	 * @throws CannotSendMailException signale que l'envoi du mail a �chou�
	 */
	public void send() throws CannotSendMailException
	{
		try
		{
			Transport.send(message);
		}
		catch(MessagingException me)
		{
			logger.error("Weuhl�, mon mail y part p� ! M�ga glandasses !\n" + me.getLocalizedMessage());
			throw new CannotSendMailException(me.getLocalizedMessage());
		}
	}
}

