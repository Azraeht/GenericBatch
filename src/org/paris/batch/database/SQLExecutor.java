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
            logger.info("Fermeture de la connection effectu�e.");

        } catch (Throwable t) {
            String msg = "Erreur � la fermeture de la connection : "
                    + t.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
    }

    /**
     * M�thode pour effectuer une requ�te "DELETE","UPDATE" ou "INSERT" dans une
     * base de donn�es sans param�tres.
     * 
     * @param query
     *            requete SQL � executer.
     * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas
     *         correctement ex�cut�e.
     */
    public int executeUpdate(String query) {
        int result = 0;
        try {
            logger.info("Requ�te SQL : " + query);
            result = runner.update(connection, query);
            logger.info("Requ�te ex�cut�e. R�sultat retourn� : " + result);
        } catch (SQLException sqle) {
            logger.error("SQLException � l'ex�cution de la requ�te :\n\t"
                    + query + "\n" + sqle.getMessage());
        }
        return result;
    }

    /**
     * M�thode pour effectuer une requ�te "DELETE","UPDATE" ou "INSERT" dans une
     * base de donn�es avec param�tres.
     * 
     * @param query
     *            requete SQL � executer.
     * @param params
     *            The query replacement parameters.
     * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas
     *         correctement ex�cut�e.
     */
    public int executeUpdate(String query, Object... params) {
        int result = 0;
        try {
            logger.info("Requ�te SQL : " + query);
            result = runner.update(connection, query, params);
            logger.info("Requ�te ex�cut�e. R�sultat retourn� : " + result);
        } catch (SQLException sqle) {
            logger.error("SQLException � l'ex�cution de la requ�te :\n\t"
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
            logger.info("Requ�te SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, new MapListHandler());
            logger.info("Requ�te ex�cut�e. El�ments retourn�s : "
                    + result.size());
        } catch (SQLException sqle) {
            logger.error("SQLException � l'ex�cution de la requ�te :\n\t"
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
            logger.info("Requ�te SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, new MapListHandler(),
                    params);
            logger.info("Requ�te ex�cut�e. El�ments retourn�s : "
                    + result.size());
        } catch (SQLException sqle) {
            logger.error("SQLException � l'ex�cution de la requ�te :\n\t"
                    + query + "\n" + sqle.getMessage());
        }
        return result;
    }

}
