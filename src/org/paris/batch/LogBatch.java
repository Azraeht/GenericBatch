package org.paris.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.paris.batch.CodeRetourBatch;
import org.paris.batch.exception.NoPropertiesFoundException;

/**
 * Implémentation spécifique au framework d'un logger Log4J v1
 * @author lannoog
 *
 */
public class LogBatch {
	/**
	 * Objet Logger - singleton
	 */
	public static Logger logBatch;
	
	/**
	 * Objet Properties
	 */
	static Properties properties;
	
	/**
	 * Accesseur simple
	 * @return instance courante du Logger
	 */
	public Logger getLogBatch() {
		return logBatch;
	}

	/**
	 * Affectation simple
	 * @param logBatch
	 */
	public static void setLogBatch(Logger logBatch) {
		LogBatch.logBatch = logBatch;
	}

	/**
	 * Méthode permettant d'enregistrer un log dans un dossier log, dans le répertoire courant de l'application.
	 * Le nom du batch sera par défaut GenericBatchLog.txt
	 * @throws IOException
	 */
	public void configurationAutoLog() throws IOException {
		//On configure le log dans le dossier log.
		//affecter au singleton une nouvelle instance de logger
		setLogBatch(Logger.getRootLogger());
		
		//configurer le mode de sauvegarde de la trace d'exécution
		PatternLayout layout2 = new PatternLayout("%d %-5p %c - %F:%L - %m%n");
		
		//TODO : virer la trace ci-dessous
		System.out.println("Les fichiers de log sont créés ici :\n" + System.getProperty("user.dir") +"\\log\\GenericBatchLog.txt");
		
		//créer le FileAppender et son emplacement de sortie en fonction du mode de sauvegarde
		FileAppender appender = new FileAppender(layout2, System.getProperty("user.dir") +"\\log\\GenericBatchLog.txt",false);
		
		ConsoleAppender stdout = new ConsoleAppender(layout2);		
		getLogBatch().addAppender(appender);
		getLogBatch().addAppender(stdout);
		
		try {
			logBatch.info("Configuration du log.");
		}
		catch(Throwable t){
			System.err.println(t.getMessage());
			logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
			System.exit(CodeRetourBatch.EXIT_ERROR);
		}
	}
	
	/**
	 * Méthode permettant de configurer le fichier de log.
	 * @param cheminLog le chemin de l'enregistrement du fichier log
	 * @param nomLog le nom du log
	 * @throws IOException
	 * @throws NoPropertiesFoundException 
	 */
	public void configurationLog() throws IOException, NoPropertiesFoundException {
		properties = new Properties();
		String cheminDAccesProperties = "config.properties";
		String workingDir = System.getProperty("user.dir") + "\\config\\";
		File temp = new File(workingDir, "config.properties");
		
		try {
			//props.load(classLoader.getResourceAsStream("\\query.properties"));
			InputStream resourceAsStream =  new FileInputStream(temp);
		    if (resourceAsStream != null) {
		        properties.load(resourceAsStream);
		    }
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException au chargement du fichier de propriétés :\n\t" + cheminDAccesProperties + "\n" + npe.toString());
			throw new NoPropertiesFoundException(npe.getMessage());
		} catch (IOException ioe) {
			System.out.println("IOException au chargement du fichier de propriétés :\n\t" + cheminDAccesProperties + "\n" + ioe.getMessage());
			throw new NoPropertiesFoundException(ioe.getMessage());
		}
		
		//On configure le log dans le dossier log.
		setLogBatch(Logger.getRootLogger());
		PatternLayout layout2 = new PatternLayout("%d %-5p %c - %F:%L - %m%n");
		FileAppender appender = new FileAppender(layout2, properties.getProperty("cheminLog")+"\\"+ properties.getProperty("nomLog") +".txt",false);
		ConsoleAppender stdout = new ConsoleAppender(layout2);		
		getLogBatch().addAppender(appender);
		getLogBatch().addAppender(stdout);
		
		try {
			logBatch.info("Configuration du log.");
		}
		catch(Throwable t){
			System.err.println(t.getMessage());
			logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
			System.exit(CodeRetourBatch.EXIT_ERROR);
		}
	}
}
