package org.paris.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;
import org.paris.batch.exception.StatAnalyzerException;
import org.paris.batch.logging.LogBatch;
import org.paris.batch.mailer.Mailer;
import org.paris.batch.utils.CommandExecutor;
import org.paris.batch.utils.FileWriter;
import org.paris.batch.database.SQLExecutor;
import org.paris.batch.datafile.DataFile;

/**

 * Classe abstraite offrant plusieurs services communs à tous les batchs Ville
 * de Paris.
 * 
 * <br>

 * Le contrat de la classe stipule qu'il faut implémenter ces trois méthodes:<br>
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
 * @author Christophe RIVIERE
 * 
 * Exemple : 

      //getting username using System.getProperty in Java
       String user = System.getProperty("user.name") ;
       System.out.println("Username using system property: "  + user);

     //getting username as environment variable in java, only works in windows
       String userWindows = System.getenv("USERNAME");
       System.out.println("Username using environment variable in windows : "  + userWindows);

     //name and value of all environment variable in Java  program
      Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }

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
	 * réçoit les informations spécifiées dans le fichier de configuration
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


	protected SQLExecutor sqlexecutor;

	/**
	 * ArrayList permettant de stocker les datafiles
	 */
	protected ArrayList<DataFile> dataFileList;

	protected Mailer mailer = null;
	
	private boolean noMailMode = false;


	/**
	 * Méthode pour initialiser les ressources locales.
	 * 
	 * @throws ConfigurationBatchException
	 * @throws DatabaseDriverNotFoundException
	 */
	public abstract void setup() throws ConfigurationBatchException,
	DatabaseDriverNotFoundException, StatAnalyzerException;


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
			System.out.println("### MODE DEBUG ACTIVE ###");
			System.out.println("# pour désactiver le mode debug, supprimer la variable " + ENV_DEBUG + " de l'environnement d'exécution #");
			System.out.println("Constructeur de la classe GenericBatch");
			System.out.println("\tLecture du fichier de configuration principal");
		}

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
			System.out.println("\tRécupération du numéro de version du framework");
		}
		// -------------------- Versionning Batch ---------------------------------------
		String versionGenericBatch = "non définie";
		if (!DEBUG) {
			// Récupération de la version du GénéricBatch
			String classSimpleName = GenericBatch.class.getSimpleName() + ".class";
			// 2 - Récupérer le chemin physique de la classe
			String pathToClass = GenericBatch.class.getResource(classSimpleName).toString();

			// 3 - Récupérer le chemin de la classe à partir de la racine du classpath
			String classFullName = GenericBatch.class.getName().replace('.', '/') + ".class";
			// 4 - Récupérer le chemin complet vers MANIFEST.MF
			String pathToJar = pathToClass.substring( 0, pathToClass.length() - (classFullName.length()+2)).replace("jar:file:", "");

			try {
				JarFile jar = new JarFile(pathToJar);
				Manifest manifest = jar.getManifest();
				// Lire la propriété "Implementation-Version" du Manifest
				versionGenericBatch = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
				jar.close();
				if(DEBUG) System.out.println("Version du framework récupérée dans le fichier manifest : " + versionGenericBatch);
			}
			catch (IOException ioe)
			{
				String msg = "IOEXception à l'accès au Manifest de GenericBatch :\nException : " + ioe.getMessage();
				System.err.println(msg);
				throw new ConfigurationBatchException(msg);
			}
			catch (Exception e)
			{
				String msg = "Erreur inattendue lors de l'accès au Manifest du framework.\nException :" + e.getMessage();
				throw new ConfigurationBatchException(msg);
			}
		}
		// --------------------Initialisation des modules---------------------------------------
		if (DEBUG) System.out.println("\tCréation du logger");

		// Initialisation du logger
		this.logger = LogBatch.getLogBatch(props);
		this.logger.info("---------- Initialisation du batch -----------");
		this.logger.info("Batch : "+props.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.NOM_BATCH));
		this.logger.info("Version : "+props.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.VERSION));
		this.logger.info("Version GenericBatch: "+versionGenericBatch);
		this.logger.info("----------- Chargement des modules -----------");
		if (DEBUG) System.out.println("Instanciation de GenericBatch::logger instancié");

		// Initialisation du writer et de la Datafilelist
		if (DEBUG) System.out.println("Instanciation de GenericBatch::Création du FileWriter");
		this.writer = new FileWriter(this.logger);
		this.logger.info("Module chargé : FileWriter");
		this.dataFileList = new ArrayList<DataFile>();
		if (DEBUG) System.out.println("Instanciation de GenericBatch::FileWriter instancié");

		// Initialisation de l'executeur de commande
		if (DEBUG) System.out.println("Instanciation de GenericBatch::Création du CommandExecutor");
		this.command = new CommandExecutor(this.logger);
		this.logger.info("Module chargé : CommandExecutor");
		if (DEBUG) System.out.println("Instanciation de GenericBatch::CommandExecutor instancié");

		// Initialisation des fichiers de config
		String ConfigFiles = props.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.CONFIG_MODULES);
		
		// On liste dans le log la liste des modules chargés : Mail, Datafile etc...
		if(ConfigFiles != null){
			if(!ConfigFiles.equals(""))
			{
				String[] listConfigFiles = ConfigFiles.split(",");
				for (String configfile : listConfigFiles)
				{
					//Module Mailer
					//if(configfile.equals("mail")){
					//	this.mailer = new Mailer(props, this.logger);
					//}
					props = ConfigurationManagerBatch.loadAdditionalProperties(configfile + ConfigurationParameters.PROPERTIES_FILE_EXTENSION, props);
					this.logger.info("Module complémentaire chargé : "+configfile);
				}
			}
		}
		
		if (DEBUG) System.out.println("Instanciation de GenericBatch::Fichiers de config complémentaire ");
		// ---------------------------- Mode No-Commit ----------------------------------------
		if(this.props.getProperty(ConfigurationParameters.NOCOMMIT_KEY).equals("true")){
			Properties connexions = ConfigurationManagerBatch.filterProperties(this.props, ConfigurationParameters.DB_HOST_KEY, true);

			// Pour chaque connexion présente dans le fichier de conf on passe celle-ci en mode No-Commit
			Enumeration e = connexions.propertyNames();
			for (; e.hasMoreElements();) {
				String con = e.nextElement().toString();
				this.props.setProperty(ConfigurationParameters.DB_NOCOMMIT_KEY+con, "true");
			}
			this.logger.info("Mode No-Commit : ON - Pas de modifications SQL");
		}
		
		if (DEBUG) System.out.println("Test de présence de la propriété nomail");
		try
		{
			// on fournit une valeur par défaut si la propriété n'est pas trouvée
			if(this.props.getProperty(ConfigurationParameters.NOMAIL_KEY, "true").equals("true")){
			    noMailMode = true;
			}
		} catch(Exception e)
		{
			String msg = "Erreur inattendue lors du test de présence de la propriété nomail.\nException :" + e.getMessage();
			throw new ConfigurationBatchException(msg);
		}
			
		// --------------------------- Fin de l'initialisation ---------------------------------
		this.logger.info("Initialisation terminée.");
		if (DEBUG) System.out.println("Instanciation de GenericBatch::Fin de l'instanciation, prêt à taffer");
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
