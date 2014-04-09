package org.paris.batch.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
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
            logger.info("Fermeture de la connection en cours...");
            this.connection.close();
            logger.info("Fermeture de la connection effectuée.");

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
    public int executeUpdate(String query) {
        int result = 0;
        try {
            logger.info("Requête SQL : " + query);
            result = runner.update(connection, query);
            logger.info("Requête exécutée. Résultat retourné : " + result);
        } catch (SQLException sqle) {
            logger.error("SQLException à l'exécution de la requête :\n\t"
                    + query + "\n" + sqle.getMessage());
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
    public int executeUpdate(String query, Object... params) {
        int result = 0;
        try {
            logger.info("Requête SQL : " + query);
            result = runner.update(connection, query, params);
            logger.info("Requête exécutée. Résultat retourné : " + result);
        } catch (SQLException sqle) {
            logger.error("SQLException à l'exécution de la requête :\n\t"
                    + query + "\n" + sqle.getMessage());
        }
        return result;
    }

    /**
     * @param query
     * @return
     */
    public List<?> executeSelect(String query) {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requête SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, new MapListHandler());
            logger.info("Requête exécutée. Eléments retournés : "
                    + result.size());
        } catch (SQLException sqle) {
            logger.error("SQLException à l'exécution de la requête :\n\t"
                    + query + "\n" + sqle.getMessage());
        }
        return result;
    }

    /**
     * @param query
     * @return
     */
    public List<?> executeSelect(String query, Object... params) {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requête SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, new MapListHandler(),
                    params);
            logger.info("Requête exécutée. Eléments retournés : "
                    + result.size());
        } catch (SQLException sqle) {
            logger.error("SQLException à l'exécution de la requête :\n\t"
                    + query + "\n" + sqle.getMessage());
        }
        return result;
    }

}
