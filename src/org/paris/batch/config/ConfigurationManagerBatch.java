package org.paris.batch.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.paris.batch.exception.ConfigurationBatchException;

/**
 * Cette classe permet de travailler sur des fichiers de propri�t�s.
 * 
 * @author galloiem
 * 
 */
public class ConfigurationManagerBatch {
    /**
    

    /**
     * 
     * Charge le fichier de propri�t�s sp�cifi�
     * 
     * @see #PROPERTIES_CONFIG_FILENAME
     * @see #PROPERTIES_QUERY_FILENAME
     * 
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
        if (properties_type.equals(ConfigurationParameters.PROPERTIES_CONFIG_FILENAME)) {
            env_var = ConfigurationParameters.ENV_CONFIG_FILENAME;
            env = System.getenv(env_var);
        } 
        else if (properties_type.equals(ConfigurationParameters.PROPERTIES_QUERY_FILENAME)) {
            env_var = ConfigurationParameters.ENV_QUERY_FILENAME;
            env = System.getenv(env_var);
        }
        

        // L'environnement surcharge les valeurs par d�faut (dossier `config`).
        if (env != null && (new File(env)).exists()) {
            properties_filename = env;
        } else {
            properties_filename = System.getProperty("user.dir")
                    + ConfigurationParameters.CONFIG_DIRNAME + properties_type;
        }
        try {
            properties.load(new FileInputStream(new File(properties_filename)));
            return properties;
        } catch (Exception e) {
            String msg = "Erreur lors du traitement de chargement de configuration - Fichier concern�: "
                    + properties_filename + "\nException : " + e.getMessage();
            System.err.println(msg);
            throw new ConfigurationBatchException(msg);
        }
    }

    /**
     * M�thode retournant l'ensemble des properties � partir de la liste de fichiers de properties contenu dans la propertie
     * config.configfiles de config.properties(fichier de conf par d�faut et obligatoire)
     * */
    public static Properties initProperties() throws ConfigurationBatchException{
    	
    	Properties basicsProperties = new Properties();
    	Properties finalProperties = new Properties();
    	
    	// Initialisation � partir des fichiers par d�faut config.properties
    	basicsProperties = ConfigurationManagerBatch.loadProperties(ConfigurationParameters.PROPERTIES_CONFIG_FILENAME);
    	
    	// R�cup�ration de la liste des fichiers de config de modules pr�sents dans le r�pertoire 'config'
    	String ConfigFiles = basicsProperties.getProperty("config.configfiles");
    	String[] listConfigFiles = ConfigFiles.split(",");
    	
    	finalProperties.putAll(basicsProperties);
    	// Chargement de chaque fichier de properties
    	for (String configfile : listConfigFiles) {
			finalProperties.putAll(ConfigurationManagerBatch.loadProperties(configfile+".properties"));
		}
    	
    	return finalProperties;
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
