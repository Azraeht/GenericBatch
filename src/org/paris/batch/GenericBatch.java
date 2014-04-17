package org.paris.batch;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;
import org.paris.batch.logging.LogBatch;
import org.paris.batch.utils.CommandExecutor;
import org.paris.batch.utils.FileWriter;

/**
 * Classe abstraite offrant plusieurs services communs � tous les batchs Ville
 * de Paris.
 * 
 * <br>
 * La contrat de la classe stipule qu'il faut impl�menter ces trois m�thodes:<br>
 * <ul>
 * <li><code>setup()</code></li>
 * <li><code>run()</code></li>
 * <li><code>finished()</code></li>
 * </ul>
 * 
 * @see GenericBatch#setup()
 * @see #run()
 * @see #finished()
 * 
 * 
 * 
 * @author Guillaume Weber
 * @author Brice Santus
 * @author Emmanuel GALLOIS
 */
public abstract class GenericBatch {
    /**
     * debogguer to stderr & stdout: Utile lors des initialisations de d�marrage
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
     * Code retour - OK
     */
    public static final int EXIT_OK = 0;
    /**
     * Code retour - Avertissement
     */
    public static final int EXIT_WARNING = 5;
    /**
     * Code retour - Erreur
     */
    public static final int EXIT_ERROR = 10;

    /**
     * re�oit les informations sp�cifi�es dans le fichier de configuration
     * adjoint au batch
     */
    protected Properties props;
    /**
     * prend en charge la tra�abilit� des op�rations effectu�es par le batch
     * 
     * @see LogBatch
     */
    protected Logger logger;
    /**
     * permet d'�crire du contenu dans des fichiers.
     * 
     * @see FileWriter
     */
    protected FileWriter writer;
    /**
     * permet d'ex�cuter des commandes syst�me.
     * 
     * @see CommandExecutor
     */
    protected CommandExecutor command;

    /**
     * M�thode pour initialiser les ressources locales.
     * 
     * @throws ConfigurationBatchException
     * @throws DatabaseDriverNotFoundException
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
     * 
     * @throws SQLExecutorException
     */
    public abstract void finished() throws SQLExecutorException;

    /**
     * Constructeur de GenericBatch, permet d'instancier le Batch, le journal
     * d'�v�nements, le gestionnaire de commande, la gestion des fichiers et les
     * propri�t�s de configuration.
     * 
     * @throws ConfigurationBatchException
     * @throws NoPropertiesFoundException
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
     * M�thode pour g�rer l'arr�t du batch quand l'ex�cution s'est bien d�roul�e
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
