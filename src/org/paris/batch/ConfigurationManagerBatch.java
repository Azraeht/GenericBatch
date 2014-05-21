package org.paris.batch;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.utils.CommandExecutor;

/**
 * Cette classe permet de travailler sur des fichiers de propri�t�s.
 * 
 * @author galloiem
 * 
 */
public class ConfigurationManagerBatch {
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
    public static final String LOG_PATTERN_KEY = "log.pattern";
    /**
     * log : patron du format par d�faut
     */
    public static final String LOG_PATTERN_DFT = "%d %-5p %c - %F:%L - %m%n";
    /**
     * log : chemin
     */
    public static final String LOG_PATH_KEY = "log.path";
    /**
     * log : chemin par d�faut
     */
    public static final String LOG_PATH_DFT = System.getProperty("user.dir")
            + "\\log\\";
    /**
     * log : nom
     */
    public static final String LOG_FILE_KEY = "log.filename";
    /**
     * log : nom par d�faut
     */
    public static final String LOG_FILE_DFT = "generic_batch.log";
    /**
     * log : niveau de log.
     */
    public static final String LOG_LEVEL_KEY = "log.level";
    /**
     * log : niveau par d�faut du log.
     */
    public static final String LOG_LEVEL_DFT = "INFO";
    /**
     * log : �crire sur la sortie standard
     */
    public static final String LOG_STDOUT_KEY = "log.stdout";
    /**
     * Database : Driver JDBC
     */
    public static final String DB_JDBC_DRIVER_KEY = "db.jdbc.driver";
    /**
     * Database : network IP or hostname
     */
    public static final String DB_HOST_KEY = "db.host";
    /**
     * Database : network port
     */
    public static final String DB_PORT_KEY = "db.port";
    /**
     * Database : ID
     */
    public static final String DB_ID_KEY = "db.id";
    // public static final String DB_URL_KEY = "db.url.";
    /**
     * Database : utilisateur
     */
    public static final String DB_USER_KEY = "db.user";
    /**
     * Database : mot de passe
     */
    public static final String DB_PASS_KEY = "db.pass";
    /**
     * Database : Autocommit vrai/faux.
     */
    public static final String DB_AUTOCOMMIT_KEY = "db.autocommit";

    /**
     *     * Database : choix de l'encodage.
     */
    public static final String DB_ENCODAGE_KEY = "db.encodage";

    /**
     * 
     * @see #PROPERTIES_CONFIG_FILENAME
     * @see #PROPERTIES_QUERY_FILENAME
     * 
     * @param properties_type
     *            le type de propri�t� � charger.
     * @return Les propri�t�s contenues dans le fichier
     * @throws ConfigurationBatchException
     */
    public static Properties loadProperties(String properties_type)
            throws ConfigurationBatchException {
        Properties properties = new Properties();
        String env = null;
        String env_var = null;
        String properties_filename;

        // Quel est le type de fichier de configuration ?
        if (properties_type.equals(PROPERTIES_CONFIG_FILENAME)) 
        {
            env_var = ENV_CONFIG_FILENAME;
        } 
        else if (properties_type.equals(PROPERTIES_QUERY_FILENAME)) 
        {
            env_var = ENV_QUERY_FILENAME;
        }

        // L'environnement surcharge les valeurs par d�faut (dossier `config`).
        env = System.getenv(env_var);
        if (env != null && (new File(env)).exists()) 
        {
            properties_filename = env;
        } 
        else 
        {
            properties_filename = System.getProperty("user.dir")
                    + CONFIG_DIRNAME + properties_type;
        }
        try 
        {
            properties.load(new FileInputStream(new File(properties_filename)));
            return properties;
        } 
        catch (Exception e) 
        {
            String msg = "Erreur lors du traitement de chargement de configuration - Fichier concern�: "
                    + properties_filename + "\nException : " + e.getMessage();
            System.err.println(msg);
            throw new ConfigurationBatchException(msg);
        }
    }

    /**
     * 
     * Retourne une instance de type Properties bas�e sur la fusion des
     * instances p1 et p2. Si une cl� existe dans les deux, la valeur de p2
     * remplacera celle de p1.
     * 
     * @param p1
     *            propri�t�s - jeu #1
     * @param p2
     *            propri�t�s - jeu #2
     * @return merged : propri�t�s fusionn�es.
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
     * Permet de retourner un sous-ensemble des propri�t�s filt�es par le
     * param�tre <code>filter</code>.<br>
     * exemple de filtre:
     * 
     * <pre>
     * &quot;.oracleDB&quot;
     * </pre>
     * 
     * @param p
     *            Les propri�t�s � filtrer
     * @param filter
     *            le filtre � rechercher
     * @param removeFilter
     *            si d�fini � <code>true</code> alors le filtre sera supprim� de
     *            la cl�, sinon la cl� reste tel quel.
     * @return Les propri�t�s filtr�es
     * 
     */
    public static Properties filterProperties(Properties p, String filter,
            boolean removeFilter) 
    {
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
