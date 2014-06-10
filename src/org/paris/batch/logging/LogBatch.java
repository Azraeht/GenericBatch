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
            throws ConfigurationBatchException {
        logBatch = Logger.getRootLogger();
        String log_pattern = properties.getProperty(
        		ConfigurationParameters.LOG_PATTERN_KEY,
        		ConfigurationParameters.LOG_PATTERN_DFT);
        String log_path = properties.getProperty(
        		ConfigurationParameters.LOG_PATH_KEY,
        		ConfigurationParameters.LOG_PATH_DFT);
        // vérification du dossier, sinon création de celui-ci.
        File directory = new File(log_path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // fichier de log.
        String log_filename = properties.getProperty(
        		ConfigurationParameters.LOG_FILE_KEY,
        		ConfigurationParameters.LOG_FILE_DFT);

        Level level = Level.toLevel(properties.getProperty(
        		ConfigurationParameters.LOG_LEVEL_KEY,
        		ConfigurationParameters.LOG_LEVEL_DFT));

        boolean logstdout = Boolean.parseBoolean(properties.getProperty(
        		ConfigurationParameters.LOG_STDOUT_KEY, "false"));

        try {
            PatternLayout layout2 = new PatternLayout(log_pattern);
            FileAppender appender = new FileAppender(layout2, log_path
                    + log_filename, false);
            logBatch.setLevel(level);
            logBatch.addAppender(appender);
            if (logstdout) {
                ConsoleAppender stdout = new ConsoleAppender(layout2);
                logBatch.addAppender(stdout);
            }

        } catch (Throwable t) {
            String msg = "Erreur lors de l'instanciation du logger: "
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
