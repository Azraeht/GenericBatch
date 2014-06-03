/**
 * 
 */
package org.paris.batch.config;

/**
 * @author santusbr
 *
 */
public class ConfigurationParameters {
	/**
	 * configuration : Dossier des fichiers de configuration
     */
    public final static String CONFIG_DIRNAME = "\\config\\";

    /**
     * configuration : fichier principal
     */
    public final static String PROPERTIES_CONFIG_FILENAME = "config.properties";
    /**
     * configuration : fichier des requetes
     */
    public final static String PROPERTIES_QUERY_FILENAME = "query.properties";
    /**
     * Variable d'environnement : surchage le fichier de configuration
     */
    public final static String ENV_CONFIG_FILENAME = "MDP_BATCH_CONFIG_FILENAME";
    /**
     * Variable d'environnement : surchage le fichier de requetes
     */
    public final static String ENV_QUERY_FILENAME = "MDP_BATCH_QUERY_FILENAME";
    /**
     * Variable d'environnement : pour tracer le d�marage initial de la classe.
     */
    public final static String ENV_DEBUG = "MDP_BATCH_DEBUG";
    
    
    
    /**
     * log : patron du format
     */
    public static final String LOG_PATTERN_KEY = "config.log.pattern";
    /**
     * log : patron du format par d�faut
     */
    public static final String LOG_PATTERN_DFT = "%d %-5p %c - %F:%L - %m%n";
    /**
     * log : chemin
     */
    public static final String LOG_PATH_KEY = "config.log.path";
    /**
     * log : chemin par d�faut
     */
    public static final String LOG_PATH_DFT = System.getProperty("user.dir")
            + "\\log\\";
    /**
     * log : nom
     */
    public static final String LOG_FILE_KEY = "config.log.filename";
    /**
     * log : nom par d�faut
     */
    public static final String LOG_FILE_DFT = "generic_batch.log";
    /**
     * log : niveau de log.
     */
    public static final String LOG_LEVEL_KEY = "config.log.level";
    /**
     * log : niveau par d�faut du log.
     */
    public static final String LOG_LEVEL_DFT = "INFO";
    /**
     * log : �crire sur la sortie standard
     */
    public static final String LOG_STDOUT_KEY = "config.log.stdout";
    /**
     * Database : Driver JDBC
     */
    public static final String DB_JDBC_DRIVER_KEY = "config.db.jdbc.driver";
    /**
     * Database : network IP or hostname
     */
    public static final String DB_HOST_KEY = "config.db.host";
    /**
     * Database : network port
     */
    public static final String DB_PORT_KEY = "config.db.port";
    /**
     * Database : ID
     */
    public static final String DB_ID_KEY = "config.db.id";
    // public static final String DB_URL_KEY = "db.url.";
    /**
     * Database : utilisateur
     */
    public static final String DB_USER_KEY = "config.db.user";
    /**
     * Database : mot de passe
     */
    public static final String DB_PASS_KEY = "config.db.pass";
    /**
     * Database : Autocommit
     */
    public static final String DB_AUTOCOMMIT_KEY = "config.db.autocommit";
}
