package org.paris.batch.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.paris.batch.GenericBatch;
import org.paris.batch.exception.ConfigurationBatchException;


/**
 * Cette classe permet de travailler sur des fichiers de propriétés.
 * 
 * @author galloiem
 * 
 */
public class ConfigurationManagerBatch {
	
	/**
	 * Charge le fichier de propriétés indiqué en paramètre dans le jeu de propriétés fourni en entrée.
	 * Si une variable d'environnement existe permettant de surcharger le fichier à utiliser, c'est le fichier visé par la variable d'environnement qui sera chargé.
	 * @param propertiesFileName nom du fichier de propriétés à charger
	 * @param props jeu de propriétés existant
	 * @return le jeu de propriétés existant, complété des propriétés chargées
	 * @throws ConfigurationBatchException en cas de fichier absent ou incorrect
	 */
	public static Properties loadAdditionalProperties(String propertiesFileName, Properties props) throws ConfigurationBatchException
	{
		String env = null;
		String env_var = null;

		// Quel est le type de fichier de configuration ?
		if (propertiesFileName.equals(ConfigurationParameters.PROPERTIES_CONFIG_FILENAME)) {
		    //tenter de récupérer dans les variables d'environnement le nom du fichier de conf à charger
			env_var = ConfigurationParameters.ENV_CONFIG_FILENAME;
			env = System.getenv(env_var);
		} 
		else if (propertiesFileName.equals(ConfigurationParameters.PROPERTIES_QUERY_FILENAME)) {
            //tenter de récupérer dans les variables d'environnement le nom du fichier de conf à charger
		    env_var = ConfigurationParameters.ENV_QUERY_FILENAME;
			env = System.getenv(env_var);
		}

		//la chaîne env vaut null si aucune variable d'env n'a été trouvée.
		// Si elle a été trouvée, l'environnement surcharge les valeurs par défaut (dossier `config`).
		if (env != null && (new File(env)).exists()) {
			propertiesFileName = env;
		} else {
		    //construire le chemin d'accès au fichier de propriétés à charger
			propertiesFileName = System.getProperty("user.dir")
					+ ConfigurationParameters.CONFIG_DIRNAME + propertiesFileName;
		}
		
		//on tente le chargement du fichier demandé
		try
		{
			props.load(new FileInputStream(new File(propertiesFileName)));
			props = overrideWithEnv(props);
			return props;
		}
		catch (Exception e)
		{
			String msg = "Erreur lors du traitement de chargement de configuration - Fichier concerné: "
					+ propertiesFileName + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new ConfigurationBatchException(msg);
		}
	}
	
	/**
	 * 
	 * Charge le fichier de propriétés spécifié en paramètre, sauf si celui-ci est redéfini localement par une variable d'environnement
	 *  (dans ce cas, on charge le fichier pointé par la variable d'environnement à la place)
	 * 
	 * @param properties_type
	 *            le type de propriété à charger.
	 * @return Les propriétés contenues dans le fichier
	 * @throws ConfigurationBatchException
	 */
	public static Properties loadProperties(String properties_type)
			throws ConfigurationBatchException {
		Properties properties = new Properties();
		return loadAdditionalProperties(properties_type, properties);
	}

	/**
	 * Cette méthode permet de surcharger les valeurs du fichier de propriétés par la valeur
	 * correspondante éventuellement présente dans l'environnement
	 * @param props Jeu de propriétés issue des fichiers du batch
	 * @return Jeu de propriétés mis à jour avec les données issues de l'environnement
	 * @throws ConfigurationBatchException
	 */
	public static Properties overrideWithEnv(Properties props)
	{
	    String env = null;
	    //passer par toutes les propriétés du jeu fourni, et remplacer au besoin
	    //les propriétés du fichier par celles définies dans l'environnement (si définies !)
	    for (Object key : props.keySet()) 
	    {
	        env = System.getenv((String)key);
	        if(env != null)
	            props.setProperty((String)key, env);
	    }
	    return props;
	}
	
	/**
	 * Méthode retournant l'ensemble des properties à partir de la liste de fichiers de properties contenu dans la propertie
	 * config.configfiles de config.properties(fichier de conf par défaut et obligatoire)
	 * */
	public static Properties initProperties() throws ConfigurationBatchException{
	    boolean DEBUG = false;
	    
        //debug defini dans l'environnement ?
        if (System.getenv(GenericBatch.ENV_DEBUG) != null) {
            DEBUG = true;
        }
        
        /*---------------------------Vérification des properties----------------------------*/
        
        /*----------------------------------------------------------------------------------*/
        
		Properties basicsProperties = new Properties();
		Properties finalProperties = new Properties();
		try{
		    if(DEBUG) System.out.println("Chargement des propriétés élémentaires (fichier " + ConfigurationParameters.PROPERTIES_CONFIG_FILENAME + ")");
			// Initialisation à partir des fichiers par défaut config.properties
			basicsProperties = ConfigurationManagerBatch.loadProperties(ConfigurationParameters.PROPERTIES_CONFIG_FILENAME);
			if(DEBUG) System.out.println("Propriétés élémentaires chargées.");
			
	        /*---------------------------Vérification des properties----------------------------*/
			// Vérification du nom du batch
	        String propertie = basicsProperties.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.NOM_BATCH);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.NOM_BATCH, "Batch généré à partir du GenericBatch");
	        }
	        // Vérification de la version du batch
	        propertie = basicsProperties.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.VERSION);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.VERSION, "Version non définie");
	        }
	        // Vérification du répertoire temporaire
	        propertie = basicsProperties.getProperty(ConfigurationParameters.TEMPDIR);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.TEMPDIR, "autotempdir");
	        	 File dir = new File("autotempdir");
	        	 if(!dir.exists())
	        		 dir.mkdir();
	        }
	        // Vérification du mode no-commit
	        propertie = basicsProperties.getProperty(ConfigurationParameters.NOCOMMIT_KEY);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.NOCOMMIT_KEY, "false");
	        }
	        propertie = basicsProperties.getProperty(ConfigurationParameters.NOCOMMIT_KEY);
	        // Vérification du paramétrage du log : Nom du fichier, emplacement
	        propertie = basicsProperties.getProperty(ConfigurationParameters.LOG_PATTERN_KEY);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.LOG_PATTERN_KEY, ConfigurationParameters.LOG_PATTERN_DFT);
	        }
	        propertie = basicsProperties.getProperty(ConfigurationParameters.LOG_FILE_KEY);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.LOG_FILE_KEY, ConfigurationParameters.LOG_FILE_DFT);
	        }
	        propertie = basicsProperties.getProperty(ConfigurationParameters.LOG_LEVEL_KEY);
	        if(propertie == null || propertie.equals("")){
	        	basicsProperties.put(ConfigurationParameters.LOG_LEVEL_KEY, ConfigurationParameters.LOG_LEVEL_KEY);
	        }
	        
	  
			
	        /*----------------------------------------------------------------------------------*/
			
			// Récupération de la liste des fichiers de config de modules présents dans le répertoire 'config'
			if(DEBUG) System.out.println("Chargement des propriétés supplémentaires (définies dans le fichier " + ConfigurationParameters.PROPERTIES_CONFIG_FILENAME + ", clé : " + ConfigurationParameters.CONFIG_PREFIX + "." + ConfigurationParameters.CONFIG_MODULES + ")");
			String configFiles = basicsProperties.getProperty(ConfigurationParameters.CONFIG_PREFIX+"."+ConfigurationParameters.CONFIG_MODULES);
			//déverser les propriétés élémentaires dans le paquet de propriétés final
			finalProperties.putAll(basicsProperties);
			
			//si la liste est renseignée (pas null, pas vide)
			if(configFiles != null && !configFiles.trim().equals(""))
			{
			    //créer un tableau de String à partir de la liste 
				String[] listConfigFiles = configFiles.split(",");
				
				

				// Chargement de chaque fichier de properties indiqué dans la liste
				for (String configfile : listConfigFiles)
				{
				    //pour le fichier considéré, intégrer les propriétés chargées au paquet de propriétés final
					finalProperties.putAll(ConfigurationManagerBatch.loadProperties(configfile+".properties"));
				}
			}
		}
		catch(Exception e)
		{
			String msg = "Erreur lors de l'initialisation de la configuration\nException : " + e.getMessage();
			System.err.println(msg);
			throw new ConfigurationBatchException(msg);
		}

		//chargement fini : on renvoie le paquet de propriétés final, et zou !
		return finalProperties;
	}
	/**
	 * 
	 * Retourne une instance de type Properties basée sur la fusion des
	 * instances p1 et p2. Si une clé existe dans les deux, la valeur de p2
	 * remplacera celle de p1.
	 * 
	 * @param p1
	 *            propriétés - jeu #1
	 * @param p2
	 *            propriétés - jeu #2
	 * @return merged : propriétés fusionnées.
	 */
	public static Properties mergeProperties(Properties p1, Properties p2) {
		Properties merged = new Properties();
		merged = p1;
		for (Object k : p2.keySet()) {
			merged.setProperty((String) k, (String) p2.get(k));
		}

		return merged;
	}

	/**
	 * Permet de retourner un sous-ensemble des propriétés filtées par le
	 * paramétre <code>filter</code>.<br>
	 * exemple de filtre:
	 * 
	 * <pre>
	 * &quot;.oracleDB&quot;
	 * </pre>
	 * 
	 * @param p
	 *            Les propriétés à filtrer
	 * @param filter
	 *            le filtre à rechercher
	 * @param removeFilter
	 *            si défini à <code>true</code> alors le filtre sera supprimé de
	 *            la clé, sinon la clé reste tel quel.
	 * @return Les propriétés filtrées
	 * 
	 */
	public static Properties filterProperties(Properties p, String filter,
			boolean removeFilter) {
		Properties props = new Properties();
		for (Object key : p.keySet()) {
			String newkey = (String) key;
			if (removeFilter) {
				// System.out.println("b:"+newkey);
				newkey = newkey.replaceAll(filter, "");
				// System.out.println("a:"+newkey);
			}
			if (((String) key).contains(filter)) {
				// System.out.println("->"+newkey);
				props.setProperty(newkey, (String) p.get(key));
			}
		}

		return props;
	}

}
