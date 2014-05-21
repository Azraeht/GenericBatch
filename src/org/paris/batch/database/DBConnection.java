package org.paris.batch.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.paris.batch.ConfigurationManagerBatch;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;

/**
 * Connection utilisée par la class {@link SQLExecutor}
 * 
 * @author galloiem
 *
 */
public class DBConnection {
    /**
     * URL JDBC pour Oracle
     */
    private static final String URL_ORACLE = "jdbc:oracle:thin:@%s:%s:%s";
    /**
     * URL JDBC pour MySQL
     */
    private static final String URL_MYSQL = "jdbc:mysql://%s:%s/%s";

    /**
     * constructeur
     * 
     * @param p
     *            propriétés pour la création de la connection
     * @return Connection object
     * @throws DatabaseDriverNotFoundException
     * @throws ConfigurationBatchException
     */
    public static Connection getConnection(Properties p)
            throws DatabaseDriverNotFoundException, ConfigurationBatchException {
        Connection connect;
        
        // System.out.print(p); 

        String driver = p
                .getProperty(ConfigurationManagerBatch.DB_JDBC_DRIVER_KEY);
        if (!DbUtils.loadDriver(driver)) {
            // Si le driver n'est pas d�tect� l'application s'arr�te
            String msg = "Driver `" + driver + "` absent.";
            System.err.println(msg);

            throw new DatabaseDriverNotFoundException(msg);
        }
        // Set jdbc database url according driver.
        String url = "";
        if (p.getProperty("db.jdbc.driver").contains("oracle")) {
            url = String.format(URL_ORACLE,
                    p.getProperty(ConfigurationManagerBatch.DB_HOST_KEY),
                    p.getProperty(ConfigurationManagerBatch.DB_PORT_KEY),
                    p.getProperty(ConfigurationManagerBatch.DB_ID_KEY));
            
        } else if (p.getProperty("db.jdbc.driver").contains("mysql")) {
            url = String.format(URL_MYSQL,
                    p.getProperty(ConfigurationManagerBatch.DB_HOST_KEY),
                    p.getProperty(ConfigurationManagerBatch.DB_PORT_KEY),
                    p.getProperty(ConfigurationManagerBatch.DB_ID_KEY));
        }
        // else if (){} and so on...
        // �tablissement de la connexion au SGBD
        try {
            connect = DriverManager.getConnection(url,
                    p.getProperty(ConfigurationManagerBatch.DB_USER_KEY),
                    p.getProperty(ConfigurationManagerBatch.DB_PASS_KEY));

            // TODO : ATTENTION au CAST !!!!!!!!   
            connect.setAutoCommit(Boolean.parseBoolean(p.getProperty(
                    ConfigurationManagerBatch.DB_AUTOCOMMIT_KEY, "false")));

        } catch (SQLException sqle) {
            String msg = "Probl�me de connexion � la base de donn�es :\n\t"
                    + "\n" + sqle.getMessage();
            System.err.println(msg);
            throw new ConfigurationBatchException(msg);
        }

        return connect;
    }
}
