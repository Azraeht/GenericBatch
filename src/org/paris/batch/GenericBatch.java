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
 * Classe générique offrant plusieurs services communs à tous les batchs Ville
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
     * debogguer to stderr & stdout: Util lors des initialisations de démarrage
     * (config, logger,...)
     * 
     */
    public static boolean DEBUG = false;
    /**
     * Variable d'environnement pour définir la variable <code>DEBUG</code><br>
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
     * Objet Properties - reçoit les informations spécifiées dans le fichier de
     * configuration adjoint au batch
     */
    protected Properties props;
    /**
     * Objet Logger - prend en charge la traçabilité des opérations effectuées
     * par le batch
     */
    protected Logger logger;
    /**
     * Objet FileWriter - permet d'écrire du contenu dans des fichiers.
     */
    protected FileWriter writer;
    /**
     * Objet CommandExecutor - permet d'exécuter des commandes système.
     */
    protected CommandExecutor command;

    /**
     * Méthode pour initialiser les variables locales.
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
     * Méthode pour nettoyer les ressources utilisées. Cette méthode est appelée
     * automatiquement lors de l'appel à l'une des méthodes de sortie (
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
                    .println("Instanciation de GenericBatch::Création du logger");
        }
        this.logger = LogBatch.getLogBatch(props);
        this.logger
                .info("Initialisation des objets FileWriter, CommandExecutor.");
        this.writer = new FileWriter(this.logger);
        this.command = new CommandExecutor(this.logger);
        this.logger.info("Initialisation terminée.");
    }

    /**
     * Méthode pour gérer l'arrêt du batch après une erreur irrécupérable.
     */
    public void exitFailure() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);
        } finally {
            logger.info("Erreur irrécupérable. Fin d'exécution du batch.");
            System.exit(EXIT_ERROR);
        }
    }

    /**
     * Méthode pour gérer l'arrêt du batch quand tout va bien (hé ouais, c'est
     * possible).
     */
    public void exitSuccess() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);
        } finally {
            logger.info("Fin d'exécution du batch (succès).");
            System.exit(EXIT_OK);
        }
    }

    /**
     * Méthode pour gérer l'arrêt du batch après un avertissement.
     */
    public void exitWarning() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);

        } finally {
            logger.info("Fin d'exécution du batch (avertissements).");
            System.exit(EXIT_WARNING);
        }
    }
}
