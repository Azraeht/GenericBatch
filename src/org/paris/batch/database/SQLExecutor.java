package org.paris.batch.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.SQLExecutorException;

/**
 * @author galloiem
 * 
 */
public class SQLExecutor {

    private Connection connection;
    private Logger logger;
    private QueryRunner runner;

    /**
     * @param properties
     * @param logger
     * @throws DatabaseDriverNotFoundException
     * @throws ConfigurationBatchException
     */
    public SQLExecutor(Properties properties, Logger logger)
            throws DatabaseDriverNotFoundException, ConfigurationBatchException {
        super();
        this.connection = DBConnection.getConnection(properties);
        this.logger = logger;
        runner = new QueryRunner();
        logger.debug("SQLExecutor configured.");
    }

    /**
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
     * Méthode pour effectuer une requête "DELETE","UPDATE" ou "INSERT" dans une
     * base de données sans paramètres.
     * 
     * @param query
     *            requete SQL à executer.
     * @return 1 si la requête s'est executée ou 0 si elle ne s'est pas
     *         correctement exécutée.
     */
    public int executeUpdate(String query) throws SQLExecutorException {
        int result = 0;
        try {
            logger.info("Requête SQL : " + query);
            result = runner.update(connection, query);
            logger.info("Requête exécutée. Résultat retourné : " + result);
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);
            
            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * Méthode pour effectuer une requête "DELETE","UPDATE" ou "INSERT" dans une
     * base de données avec paramètres.
     * 
     * @param query
     *            requete SQL à executer.
     * @param params
     *            The query replacement parameters.
     * @return 1 si la requête s'est executée ou 0 si elle ne s'est pas
     *         correctement exécutée.
     */
    public int executeUpdate(String query, Object... params)
            throws SQLExecutorException {
        int result = 0;
        try {
            logger.info("Requête SQL : " + query);
            result = runner.update(connection, query, params);
            logger.info("Requête exécutée. Résultat retourné : " + result);
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
     * @return
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requête SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, handler);
            logger.info("Requête exécutée. Eléments retournés : "
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
     * @return
     */
    public List<?> executeSelect(String query, Object... params)
            throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requête SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, new MapListHandler(),
                    params);
            logger.info("Requête exécutée. Eléments retournés : "
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
     * Wrapper pour la méthode
     * <code>executeSelectWithRS(String query, Object... params)</code>
     * 
     * @param query
     * @return
     */
    public ResultSet executeSelectWithRS(String query)
            throws SQLExecutorException {
        return executeSelectWithRS(query, (Object[]) null);
    }

    /**
     * @param query
     * @return
     */
    public ResultSet executeSelectWithRS(String query, Object... params)
            throws SQLExecutorException {
        ResultSet result = null;
        try {
            logger.info("Requête SQL : " + query);
            PreparedStatement ps = connection.prepareStatement(query);
            if (params != null)
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            result = ps.executeQuery();
            logger.info("Requête exécutée.");
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
     * @return
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query, Object... params) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requête SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, handler, params);
            logger.info("Requête exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);
            
            throw new SQLExecutorException(msg);
        }
        return result;
    }

}
