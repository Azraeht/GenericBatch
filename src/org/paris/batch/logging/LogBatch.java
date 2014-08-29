package org.paris.batch.logging;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.ConfigurationBatchException;

/**

 * Implémentation spécifique au framework d'un logger Log4J v1
 * 
 * @author lannoog
 * @author galloiem
 * 
 */
public class LogBatch {
    /**
     * Objet Logger - singleton
     */
    public static Logger logBatch = null;

    /**
     * Création et configuration du logger
     * 
     * @param properties
     *            propriétés de configuration du journal
     * 
     * 
     * @see ConfigurationManagerBatch
     * 
     * @return instance courante du Logger
     * @throws ConfigurationBatchException
     */
    public static Logger getLogBatch(Properties properties)
            throws ConfigurationBatchException
    {
        
        //on commence par initialiser avec un logger par défaut fourni par Log4J
        logBatch = Logger.getRootLogger();
        
        //récupérer dans les propriétés du GenericBatch les paramètres de configuration du log :
        //- le motif d'affichage
        String log_pattern = properties.getProperty(
        		ConfigurationParameters.LOG_PATTERN_KEY,
        		ConfigurationParameters.LOG_PATTERN_DFT);
        
        //- le chemin d'enregistrement
        String log_path = properties.getProperty(
        		ConfigurationParameters.LOG_PATH_KEY,
        		ConfigurationParameters.LOG_PATH_DFT);
        
        // vérification de l'existence du dossier où stocker les logs, sinon création de celui-ci.
        File directory = new File(log_path);
        boolean dirExists = false;
        if (!directory.exists())
        {
            dirExists = directory.mkdirs();
        }
        else
            dirExists = true;
        
        if(!dirExists)
        {
            //traiter le cas où le répertoire n'existe pas et est introuvable
            String noDirMsg = "LogBatch - Erreur : le répertoire de sauvegarde des traces d'exécution (" + log_path + ") n'existe pas et n'a pu être créé.";
            System.err.println(noDirMsg);
            throw new ConfigurationBatchException(noDirMsg);
        }
        
        // récupérer dans les propriétés du GenericBatch les informations de configuration de la trace (suite) :
        // - nom du fichier
        String log_filename = properties.getProperty(
        		ConfigurationParameters.LOG_FILE_KEY,
        		ConfigurationParameters.LOG_FILE_DFT);
        // - profondeur de la trace
        Level level = Level.toLevel(properties.getProperty(
        		ConfigurationParameters.LOG_LEVEL_KEY,
        		ConfigurationParameters.LOG_LEVEL_DFT));
        
        // - report de la trace sur la sortie standard
        boolean logstdout = Boolean.parseBoolean(properties.getProperty(
        		ConfigurationParameters.LOG_STDOUT_KEY, "false"));

        //application de ces paramètres au logger instancié en début de méthode
        try
        {
            //définir le motif de trace
            PatternLayout layout2 = new PatternLayout(log_pattern);
            //définir le mode d'alimentation du fichier
            FileAppender appender = new FileAppender(layout2, log_path
                    + log_filename, false);
            //positionner la profondeur de trace
            logBatch.setLevel(level);
            //utiliser le FileAppender défini précédemment
            logBatch.addAppender(appender);
            
            if (logstdout)
            {
                //ajouter au besoin un renvoi des logs également sur sortie standard
                ConsoleAppender stdout = new ConsoleAppender(layout2);
                logBatch.addAppender(stdout);
            }
        }
        catch (Throwable t)
        {
            String msg = "Erreur lors du paramétrage du logger : "
                    + t.getMessage();
            System.err.println(msg);
            throw new ConfigurationBatchException(msg);
        }

        return logBatch;
    }

    /**
     * @return the logger instance
     * @throws ConfigurationBatchException
     */
    public static Logger getLogBatch() throws ConfigurationBatchException {
        if (logBatch == null) {
            String msg = "Tentative d'accés à un singleton non initialisé.";
            System.err.println(msg);
            throw new ConfigurationBatchException(msg);
        }

        return logBatch;
    }
}
