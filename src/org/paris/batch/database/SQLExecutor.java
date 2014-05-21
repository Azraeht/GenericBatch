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
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.SQLExecutorException;

/**
 * Services de base de donn�es.
 * 
 * @author galloiem
 */
public class SQLExecutor {

    private Connection connection;
    private Logger logger;
    private QueryRunner runner;
    private ProcRunner procRunner;

    /**
     * Constructeur
     * 
     * @param properties
     *            les informations n�cessaires pour la cr�ation de la connection
     * @param logger
     *            journal d'�v�nements
     * @throws DatabaseDriverNotFoundException
     * @throws ConfigurationBatchException
     */
    public SQLExecutor(Properties properties, Logger logger)
            throws DatabaseDriverNotFoundException, ConfigurationBatchException {
        super();
        this.connection = DBConnection.getConnection(properties);
        this.logger = logger;
        runner = new QueryRunner();
        procRunner = new ProcRunner();
        logger.debug("SQLExecutor configured.");
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
            logger.debug("Fermeture de la connection effectu�e.");

        } catch (Throwable t) {
            String msg = "Erreur � la fermeture de la connection : "
                    + t.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
    }

    /**
     * La m�thode execute une proc�dure ou un package Oracle.
     * Elle s'appuie sur la classe ProcRunner qui vient compl�ter la librairie DBUtils
     * @throws SQLExecutorException
     */
    public List<?> executeCallableStatement(String statementCall) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Appel � ex�cuter : " + statementCall);
            
            //result = runner.update(connection, query);
            result = procRunner.queryProc(connection, statementCall, new MapListHandler());
            logger.info("Traitement ex�cut�. El�ments retourn�s : " + result.size());
        } catch (SQLException sqle) {
            String msg = "Exception � l'ex�cution de '" + statementCall + "'\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        catch (Exception e)
        {
            String msg = "Exception � l'ex�cution de '" + statementCall + "'\n" + e.getMessage();
            logger.error(msg);
            throw new SQLExecutorException(msg);
        }
        return result;

        
    }

    /**
     * M�thode pour effectuer une requ�te "DELETE","UPDATE" ou "INSERT" dans une
     * base de donn�es sans param�tres.
     * 
     * @param query
     *            requete SQL � executer.
     * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas
     *         correctement ex�cut�e.
     * @throws SQLExecutorException
     */
    public int executeUpdate(String query) throws SQLExecutorException {
        int result = 0;
        try {
            logger.info("Requ�te SQL : " + query);
            result = runner.update(connection, query);
            logger.info("Requ�te ex�cut�e. R�sultat retourn� : " + result);
        } catch (Exception sqle) {
            String msg = "Exception � l'ex�cution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * M�thode pour effectuer une requ�te "DELETE","UPDATE" ou "INSERT" dans une
     * base de donn�es avec param�tres.
     * 
     * @param query
     *            la requ�te SQL
     * @param params
     *            The query replacement parameters.
     * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas
     *         correctement ex�cut�e.
     * @throws SQLExecutorException
     */
    public int executeUpdate(String query, Object... params)
            throws SQLExecutorException {
        int result = 0;
        try {
            logger.info("Requ�te SQL : " + query);
            result = runner.update(connection, query, params);
            logger.info("Requ�te ex�cut�e. R�sultat retourn� : " + result);
        } catch (Exception sqle) {
            String msg = "Exception � l'ex�cution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @param handler
     * @param query
     *            la requête SQL
     * @return une liste contenant les résultats
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requête SQL : " + query);
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
     *            la requête SQL
     * @param params
     * @return <code>List<Map<String, String>></code>
     * @throws SQLExecutorException
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
     * @deprecated Wrapper pour la m�thode
     *             <code>executeSelectWithRS(String query, Object... params)</code>
     * 
     * @param query
     *            la requ�te SQL
     * @return <code>java.sql.ResultSet</code>.
     */
    @SuppressWarnings("javadoc")
    public ResultSet executeSelectWithRS(String query)
            throws SQLExecutorException {
        return executeSelectWithRS(query, (Object[]) null);
    }

    /**
     * @deprecated Attention, cette m�thode a �t� �crite pour la transition du
     *             job MDPH0001. Il convient d'utiliser les autres m�thodes qui
     *             ne retournent pas de <code>java.sql.ResultSet</code>.
     * 
     * @param query
     *            la requ�te SQL
     * @return <code>java.sql.ResultSet</code>.
     */
    @SuppressWarnings("javadoc")
    public ResultSet executeSelectWithRS(String query, Object... params)
            throws SQLExecutorException {
        ResultSet result = null;
        try {
            logger.info("Requ�te SQL : " + query);
            PreparedStatement ps = connection.prepareStatement(query);
            if (params != null)
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            result = ps.executeQuery();
            logger.info("Requ�te ex�cut�e.");
        } catch (Exception sqle) {
            String msg = "Exception � l'ex�cution de `" + query + "`\n"
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
     *            la requ�te SQL
     * @param params
     *            liaison des param�tres pour la requ�te SQL
     * @return une liste du type d�finie par le param�tre handler
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query, Object... params) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.info("Requ�te SQL : " + query);
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, query, handler, params);
            logger.info("Requ�te ex�cut�e. El�ments retourn�s : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception � l'ex�cution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

}
