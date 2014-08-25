
package org.paris.batch.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.SQLExecutorException;

/**
 * Services de base de données.
 * 
 * @author galloiem
 */
public class SQLExecutor {

    private Connection connection;
    private Logger logger;
    private QueryRunner runner;
    private Boolean nocommit;

    /**
     * Constructeur
     * 
     * @param properties
     *            les informations nécessaires pour la création de la connection
     * @param logger
     *            journal d'événements
     * @throws DatabaseDriverNotFoundException
     * @throws ConfigurationBatchException
     */
    public SQLExecutor(Properties properties, Logger logger)
            throws DatabaseDriverNotFoundException, ConfigurationBatchException {
        super();
        // Création de la connexion
        this.logger.debug("Ouverture de la connection en cours...");
        this.connection = DBConnection.getConnection(properties);
        this.logger.debug("Connection ouverte.");
        // Attribution du logger
        this.logger = logger;
        // Préparation du runner
        runner = new QueryRunner();
        
        // Mode no-commit
        if(properties.getProperty(ConfigurationParameters.DB_NOCOMMIT_KEY).equals("true")){
        	this.nocommit = true;
        }else{
        	this.nocommit = false;
        }
        this.logger.debug("SQLExecutor : Paramétrage");
        this.logger.debug("Connexion : "+properties.getProperty(ConfigurationParameters.DB_USER_KEY)+"/"+properties.getProperty(ConfigurationParameters.DB_PASS_KEY)+"@"+properties.getProperty(ConfigurationParameters.DB_HOST_KEY)+":"+properties.getProperty(ConfigurationParameters.DB_PORT_KEY));
        this.logger.debug("Mode Auto-Commit :"+properties.getProperty(ConfigurationParameters.DB_AUTOCOMMIT_KEY));
        
    }

    /**
     * Ferme la connection ouverte par {@link #SQLExecutor(Properties, Logger)}
     * 
     * @throws SQLExecutorException
     */
    public void close() throws SQLExecutorException {
        try {
            logger.debug("Fermeture de la connection en cours...");
            this.connection.close();
            logger.debug("Fermeture de la connection effectuée.");

        } catch (Throwable t) {
            String msg = "Erreur à la fermeture de la connection : "
                    + t.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
    }

    /**
     * Méthode pour effectuer une Requète "DELETE","UPDATE" ou "INSERT" dans une
     * base de données sans paramétres.
     * 
     * @param query
     *            requete SQL à executer.
     * @return 1 si la Requète s'est executée ou 0 si elle ne s'est pas
     *         correctement exécutée.
     * @throws SQLExecutorException
     */
    public int executeUpdate(String query) throws SQLExecutorException {
        int result = 0;
        try {
        	// Exécution de la requête
            logger.debug("Requète SQL : " + query);
            result = runner.update(connection, query);
            logger.debug("Requète exécutée. Résultat retourné : " + result);
            
            // Rollback si mode no-commit
            if(nocommit){
            	this.rollback();
            	this.logger.info("Mode No-Commit On : Rollback effectué");
            }
            
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * Méthode pour effectuer une Requète "DELETE","UPDATE" ou "INSERT" dans une
     * base de données avec paramétres.
     * 
     * @param query
     *            la Requète SQL
     * @param params
     *            The query replacement parameters.
     * @return 1 si la Requète s'est executée ou 0 si elle ne s'est pas
     *         correctement exécutée.
     * @throws SQLExecutorException
     */
    public int executeUpdate(String query, Object... params)
            throws SQLExecutorException {
        int result = 0;
        try {
        	// Exécution de la requête
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.info("Param : "+arg.toString());
             }            
            result = runner.update(connection, query, params);
            logger.debug("Requète exécutée. Résultat retourné : " + result);
            
            // Rollback si mode no-commit
            if(nocommit){
            	this.rollback();
            	this.logger.info("Mode No-Commit On : Rollback effectué");
            }
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @param handler
     * @param query
     *            la Requète SQL
     * @return une liste contenant les résultats
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.debug("Requète SQL : " + query);
            result = runner.query(connection, query, handler);
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @param query
     *            la Requète SQL
     * @param params
     * @return <code>List<Map<String, String>></code>
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(String query, Object... params)
            throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.debug("Param : "+arg.toString());
             }        
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, new MapListHandler(),
                    params);
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @deprecated Wrapper pour la méthode
     *             <code>executeSelectWithRS(String query, Object... params)</code>
     * 
     * @param query
     *            la Requète SQL
     * @return <code>java.sql.ResultSet</code>.
     */
    @SuppressWarnings("javadoc")
    public ResultSet executeSelectWithRS(String query)
            throws SQLExecutorException {
        return executeSelectWithRS(query, (Object[]) null);
    }

    /**
     * @deprecated Attention, cette méthode a été écrite pour la transition du
     *             job MDPH0001. Il convient d'utiliser les autres méthodes qui
     *             ne retournent pas de <code>java.sql.ResultSet</code>.
     * 
     * @param query
     *            la Requète SQL
     * @return <code>java.sql.ResultSet</code>.
     */
    @SuppressWarnings("javadoc")
    public ResultSet executeSelectWithRS(String query, Object... params)
            throws SQLExecutorException {
        ResultSet result = null;
        try {
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.debug("Param : "+arg.toString());
             }         
            PreparedStatement ps = connection.prepareStatement(query);
            if (params != null)
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            result = ps.executeQuery();
            logger.debug("Requète exécutée.");
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @param handler
     *            comment traiter le ResultSet et alimenter le resultat.
     * @param query
     *            la Requète SQL
     * @param params
     *            liaison des paramétres pour la Requète SQL
     * @return une liste du type définie par le paramétre handler
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query, Object... params) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.debug("Param : "+arg.toString());
             }        
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, handler, params);
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }
    /**
     * Méthode permettant d'envoyer un ordre de commit à la base
     */
    public void commit(){
    	try {
    		// Commit des changement
			this.connection.commit();
			this.logger.debug("Commit effectué");
		} catch (SQLException sqle) {
			String msg = "Exception lors du rollback : "+ sqle.getMessage();
            logger.error(msg);
		}
    }
    
    /**
     * Méthode permettant d'envoyer un ordre de rollback à la base
     */
    public void rollback(){
    	try {
    		// Rollback des changement
			this.connection.rollback();
			this.logger.debug("RollBack effectué");
		} catch (SQLException sqle) {
			String msg = "Exception lors du rollback : "+ sqle.getMessage();
            logger.error(msg);
		}
    }
    
    

}
