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
            NoPropertiesFoundException 
            {
        // debug defini dans l'environnement ?
        if (System.getenv(ENV_DEBUG) != null)
        {
            DEBUG = true;
        }
        //
        if (DEBUG) 
        {
            System.out.println("Instanciation de GenericBatch::Lecture des fichiers de configuration");
        }
        props = ConfigurationManagerBatch.mergeProperties(
                        ConfigurationManagerBatch.loadProperties(ConfigurationManagerBatch.PROPERTIES_CONFIG_FILENAME),
                        ConfigurationManagerBatch.loadProperties(ConfigurationManagerBatch.PROPERTIES_QUERY_FILENAME));
        if (DEBUG) 
        {
            System.out.println("Instanciation de GenericBatch::Création du logger");
        }
        this.logger = LogBatch.getLogBatch(props);
        this.logger.info("Initialisation des objets FileWriter, CommandExecutor.");
        this.writer = new FileWriter(this.logger);
        this.command = new CommandExecutor(this.logger);
        this.logger.info("Initialisation terminée.");
    }

    /**
     * Méthode pour gérer l'arrêt du batch après une erreur irrécupérable.
     */
    public void exitFailure() 
    {
        try 
        {
            finished();
        } 
        catch (SQLExecutorException e) {
            logger.error(e);
        } finally 
        {
            logger.info("Erreur irrécupérable. Fin d'exécution du batch.");
            System.exit(EXIT_ERROR);
        }
    }

    /**
     * Méthode pour gérer l'arrêt du batch quand l'exécution s'est bien déroulée
     */
    public void exitSuccess() 
    {
        try 
        {
            finished();
        } 
        catch (SQLExecutorException e) 
        {
            logger.error(e);
        } finally 
        {
            logger.info("Fin d'exécution du batch (succès).");
            System.exit(EXIT_OK);
        }
    }

    /**
     * Méthode pour gérer l'arrêt du batch après un avertissement.
     */
    public void exitWarning() 
    {
        try 
        {
            finished();
        } 
        catch (SQLExecutorException e) 
        {
            logger.error(e);
        } 
        finally 
        {
            logger.info("Fin d'exécution du batch (avertissements).");
            System.exit(EXIT_WARNING);
        }
    }
    
    /**
     *  Méthode de récupération des variables d'environnement.
     * @param Valeur
     */
    public String getEnvVar(String Valeur) 
    {
        String MaValeur = "";
        try 
        {
            MaValeur = System.getenv(Valeur) ;
            System.out.println("Valeur en cours : "  + MaValeur);
        } 
        catch (Exception e) 
        {
            logger.info("Erreur lors de la récupération des variables OS.");
            logger.error(e);
        } 
        return MaValeur;
    }
    
    /**
     *  Méthode de récupération des nom et lieu de fichier à générer après avoir testé si la 
     *  variable d'environnement du fichier SalsaConfig.bat est saisie, sinon il faut prendre  
     *  la valeur de la propriété du même nom saisie dans le fichier Config.Properties. Pffff......
     *  @param nomProp nom de la propriété à trouver
     */
    public String recupValeurProp(String nomProp) 
    {
        String valeur = "";
        try
        {
            //on tente la récupération de la valeur de la propriété dans les variables d'environnement
            valeur = getEnvVar(nomProp); 
            if (valeur == null)  
            {
                //propriété introuvable dans l'environnement ! On prend la valeur par défaut présente
                //dans les fichiers de propriétés
                valeur = System.getProperty(nomProp);
            }
        }
        catch (Exception e) 
        {
            logger.info("Erreur lors de la récupération de la propriété : "+ nomProp);
            logger.error(e);
        }
        return valeur;    
    }
    
    /**
     *  Méthode d'initialisation des variables d'environnement.
     * 
     * @param Nom
     * @param Contenu
     */
    /*public void SetEnvVar(String Nom, String Contenu) 
    {
        try 
        {
            System.setProperty(Nom, Contenu) ;
            System.out.println("Valeur de substitution : "+Nom+' '+Contenu);
        } 
        catch (Exception e) 
        {
            logger.info("Initialisation erronnée des variables OS.");
            logger.error(e);
        } 
    }
    */
}
