package org.paris.batch;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;
import org.paris.batch.logging.LogBatch;
import org.paris.batch.utils.FileWriter;
import org.paris.batch.utils.CommandExecutor;

/**
 * Classe g�n�rique offrant plusieurs services communs � tous les batchs Ville
 * de Paris.
 * 
 * @author Guillaume Weber
 * @author Brice Santus
 * @author Emmanuel GALLOIS
 */

/**
 * @author galloiem
 * 
 */
public abstract class GenericBatch {
    /**
     * debogguer to stderr & stdout: Util lors des initialisations de d�marrage
     * (config, logger,...)
     * 
     */
    public static boolean DEBUG = false;
    /**
     * Variable d'environnement pour d�finir la variable <code>DEBUG</code><br>
     * Exemples:<br>
     * <code>set MDP_BATCH_DEBUG=DEBUG</code><br>
     * <code>set MDP_BATCH_DEBUG=1</code><br>
     * <code>set MDP_BATCH_DEBUG=TATOR</code><br>
     */
    public static final String ENV_DEBUG = "MDP_BATCH_DEBUG";
    /**
     * Code retours
     */
    public static int EXIT_OK = 0;
    public static int EXIT_WARNING = 5;
    public static int EXIT_ERROR = 10;

    /**
     * Objet Properties - re�oit les informations sp�cifi�es dans le fichier de
     * configuration adjoint au batch
     */
    protected Properties props;
    /**
     * Objet Logger - prend en charge la tra�abilit� des op�rations effectu�es
     * par le batch
     */
    protected Logger logger;
    /**
     * Objet FileWriter - permet d'�crire du contenu dans des fichiers.
     */
    protected FileWriter writer;
    /**
     * Objet CommandExecutor - permet d'ex�cuter des commandes syst�me.
     */
    protected CommandExecutor command;

    /**
     * M�thode pour initialiser les variables locales.
     */
    public abstract void setup() throws ConfigurationBatchException,
            DatabaseDriverNotFoundException;

    /**
     * Traitement principal du Batch.
     * 
     * @return le code retour. voir <code>EXIT_*</code>
     */
    public abstract int run();

    /**
     * M�thode pour nettoyer les ressources utilis�es. Cette m�thode est appel�e
     * automatiquement lors de l'appel � l'une des m�thodes de sortie (
     * <code>exit*</code>).
     */
    public abstract void finished() throws SQLExecutorException;

    /**
     * Constructeur de GenericBatch, permet d'instancier le Batch, son logger et
     * ses properties
     * 
     * @throws NoPropertiesFoundException
     * @throws IOException
     */
    public GenericBatch() throws ConfigurationBatchException,
            NoPropertiesFoundException {
        // debug defini dans l'environnement ?
        if (System.getenv(ENV_DEBUG) != null) {
            DEBUG = true;
        }
        //
        if (DEBUG) {
            System.out
                    .println("Instanciation de GenericBatch::Lecture des fichiers de configuration");
        }
        props = ConfigurationManagerBatch
                .mergeProperties(
                        ConfigurationManagerBatch
                                .loadProperties(ConfigurationManagerBatch.PROPERTIES_CONFIG_FILENAME),
                        ConfigurationManagerBatch
                                .loadProperties(ConfigurationManagerBatch.PROPERTIES_QUERY_FILENAME));
        if (DEBUG) {
            System.out
                    .println("Instanciation de GenericBatch::Cr�ation du logger");
        }
        this.logger = LogBatch.getLogBatch(props);
        this.logger
                .info("Initialisation des objets FileWriter, CommandExecutor.");
        this.writer = new FileWriter(this.logger);
        this.command = new CommandExecutor(this.logger);
        this.logger.info("Initialisation termin�e.");
    }

    /**
     * M�thode pour g�rer l'arr�t du batch apr�s une erreur irr�cup�rable.
     */
    public void exitFailure() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);
        } finally {
            logger.info("Erreur irr�cup�rable. Fin d'ex�cution du batch.");
            System.exit(EXIT_ERROR);
        }
    }

    /**
     * M�thode pour g�rer l'arr�t du batch quand tout va bien (h� ouais, c'est
     * possible).
     */
    public void exitSuccess() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);
        } finally {
            logger.info("Fin d'ex�cution du batch (succ�s).");
            System.exit(EXIT_OK);
        }
    }

    /**
     * M�thode pour g�rer l'arr�t du batch apr�s un avertissement.
     */
    public void exitWarning() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);

        } finally {
            logger.info("Fin d'ex�cution du batch (avertissements).");
            System.exit(EXIT_WARNING);
        }
    }
}
