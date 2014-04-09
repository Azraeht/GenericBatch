/**
 * 
 */
package org.paris.batch;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.NoPropertiesFoundException;

/**
 * @author galloiem
 * 
 * 
 *         Elements du fichier <code>config.properties</code><br>
 *         - <br>
 *         - <code>log.pattern</code> : format des logs.<br>
 *         - <code>log.name</code> : nom du fichier de log.<br>
 *         - <code>log.path</code> : chemin vers le fichier de log.<br>
 * 
 * 
 * 
 * 
 */
public class ConfigurationManagerBatch {
    /**
     * Constants : clés et valeurs par défaut.
     * 
     */
    public final static String CONFIG_DIRNAME = "\\config\\";
    public final static String PROPERTIES_CONFIG_FILENAME = "config.properties";
    public final static String PROPERTIES_QUERY_FILENAME = "query.properties";
    public final static String ENV_CONFIG_FILENAME = "MDP_BATCH_CONFIG_FILENAME";
    public final static String ENV_QUERY_FILENAME = "MDP_BATCH_QUERY_FILENAME";
    public final static String ENV_DEBUG = "MDP_BATCH_DEBUG";
    public static final String LOG_PATTERN_KEY = "log.pattern";
    public static final String LOG_PATTERN_DFT = "%d %-5p %c - %F:%L - %m%n";
    public static final String LOG_PATH_KEY = "log.path";
    public static final String LOG_PATH_DFT = System.getProperty("user.dir")
            + "\\log\\";
    public static final String LOG_FILE_KEY = "log.filename";
    public static final String LOG_FILE_DFT = "generic_batch.log";
    public static final String LOG_LEVEL_KEY = "log.level";
    public static final String LOG_LEVEL_DFT = "INFO";
    public static final String LOG_STDOUT_KEY = "log.stdout";

    public static final String DB_JDBC_DRIVER_KEY = "db.jdbc.driver";
    public static final String DB_HOST_KEY = "db.host";
    public static final String DB_PORT_KEY = "db.port";
    public static final String DB_ID_KEY = "db.id";
    // public static final String DB_URL_KEY = "db.url.";
    public static final String DB_USER_KEY = "db.user";
    public static final String DB_PASS_KEY = "db.pass";
    public static final String DB_AUTOCOMMIT_KEY = "db.autocommit";

    /**
     * 
     * Charge le fichier de propriétés spécifié
     * 
     * 
     * @param properties_type
     * @return
     * @throws NoPropertiesFoundException
     */
    public static Properties loadProperties(String properties_type)
            throws ConfigurationBatchException {
        Properties properties = new Properties();
        String env = null;
        String env_var = null;
        String properties_filename;

        // Quel est le type de fichier de configuration ?
        if (properties_type.equals(PROPERTIES_CONFIG_FILENAME)) {
            env_var = ENV_CONFIG_FILENAME;
        } else if (properties_type.equals(PROPERTIES_QUERY_FILENAME)) {
            env_var = ENV_QUERY_FILENAME;
        }

        // L'environnement surcharge les valeurs par défaut (dossier `config`).
        env = System.getenv(env_var);
        if (env != null && (new File(env)).exists()) {
            properties_filename = env;
        } else {
            properties_filename = System.getProperty("user.dir")
                    + CONFIG_DIRNAME + properties_type;
        }
        try {
            properties.load(new FileInputStream(new File(properties_filename)));
            return properties;
        } catch (Exception e) {
            String msg = "Erreur lors du traitement de chargement de configuration - Fichier concerné: "
                    + properties_filename + "\nException : " + e.getMessage();
            System.err.println(msg);
            throw new ConfigurationBatchException(msg);
        }
    }

    /**
     * 
     * Retourne une instance de type Properties basée sur les instances p1 et
     * p2.
     * 
     * @param p1
     * @param p2
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
     * paramètre <code>filter</code>.<br>
     * exemple de filtre:
     * 
     * <pre>
     * &quot;oracle&quot;
     * </pre>
     * 
     * @param p
     *            properties to filter
     * @param filter
     *            the filter
     * @param removeFilter
     *            if true then removes the filter from key, else leaves key as
     *            it is.
     * @return the properties filtered
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
