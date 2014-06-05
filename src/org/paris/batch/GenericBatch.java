package org.paris.batch;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;
import org.paris.batch.logging.LogBatch;
import org.paris.batch.utils.CommandExecutor;
import org.paris.batch.utils.FileWriter;
import org.paris.batch.datafile.DataFile;

/**
 * Classe abstraite offrant plusieurs services communs à tous les batchs Ville
 * de Paris.
 * 
 * <br>
 * La contrat de la classe stipule qu'il faut implémenter ces trois méthodes:<br>
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
     * debogguer to stderr & stdout: Utile lors des initialisations de démarrage
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
     * reéoit les informations spécifiées dans le fichier de configuration
     * adjoint au batch
     */
    protected Properties props;
    /**
     * properties dédiés aux formats des datafiles issus du fichier format.properties
     */
    protected Properties formats;
    /**
     * prend en charge la traçabilité des opérations effectées par le batch
     * 
     * @see LogBatch
     */
    protected Logger logger;
    /**
     * permet d'écrire du contenu dans des fichiers.
     * 
     * @see FileWriter
     */
    protected FileWriter writer;
    /**
     * permet d'exécuter des commandes système.
     * 
     * @see CommandExecutor
     */
    protected CommandExecutor command;
    
    /**
     * ArrayList permettant de stocker les datafiles
     */
    protected ArrayList<DataFile> dataFileList;

    /**
     * Méthode pour initialiser les ressources locales.
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
     * Méthode pour nettoyer les ressources utilisées. Cette méthode est appelée
     * automatiquement lors de l'appel à l'une des méthodes de sortie (
     * <code>exit*</code>).
     * 
     * @throws SQLExecutorException
     */
    public abstract void finished() throws SQLExecutorException;

    /**
     * Constructeur de GenericBatch, permet d'instancier le Batch, le journal
     * d'événements, le gestionnaire de commande, la gestion des fichiers et les
     * propriétés de configuration.
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
        /*
        props = ConfigurationManagerBatch
                .mergeProperties(
                        ConfigurationManagerBatch
                                .loadProperties(ConfigurationManagerBatch.PROPERTIES_CONFIG_FILENAME),
                        ConfigurationManagerBatch
                                .loadProperties(ConfigurationManagerBatch.PROPERTIES_QUERY_FILENAME));
        */
        // Initialisation des properties
     	props = ConfigurationManagerBatch.initProperties();
     	
     	// Initialisation des properties de format
     	Enumeration enuKeys = props.keys();
     	this.formats = new Properties();
		while (enuKeys.hasMoreElements()) {
			String key = enuKeys.nextElement().toString();
			String value = props.getProperty(key);
			// Si la propertie est un format elle est ajouté aux properties de format 
			if(key.contains(ConfigurationParameters.FORMAT_PREFIX)){
				this.formats.put(key, value);
			}
		}
        
        if (DEBUG) {
            System.out
                    .println("Instanciation de GenericBatch::Création du logger");
        }
        // --------------------Initialisation des modules---------------------------------------
        
        // Initialisation du logger
        this.logger = LogBatch.getLogBatch(props);
        this.logger.info("Initialisation du batch.");
        
        
        // Initialisation du writer et de la Datafilelist
        this.writer = new FileWriter(this.logger);
        this.logger.info("Module chargé : FileWriter");
        this.dataFileList = new ArrayList<DataFile>();
        
        // Initialisation de l'executeur de commande
        this.command = new CommandExecutor(this.logger);
        this.logger.info("Module chargé : CommandExecutor");
        
        String ConfigFiles = props.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.CONFIG_MODULES);
		
		if(ConfigFiles != null){
			String[] listConfigFiles = ConfigFiles.split(",");
			for (String configfile : listConfigFiles) {
				this.logger.info("Module complémentaire chargé : "+configfile);
			}
		}
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
     * Méthode pour gérer l'arrét du batch quand l'exécution s'est bien déroulée
     */
    public void exitSuccess() {
        try {
            finished();
        } catch (SQLExecutorException e) {
            logger.error(e);
        } finally {
            logger.info("Fin d'exécution du batch (succés).");
            System.exit(EXIT_OK);
        }
    }

    /**
     * Méthode pour gérer l'arrét du batch aprés un avertissement.
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
