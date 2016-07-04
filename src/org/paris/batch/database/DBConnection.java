package org.paris.batch.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OraclePreparedStatement;

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
	 * URL JDBC pour PostGreSQL
	 */
	private static final String URL_POSTGRE = "jdbc:postgresql://%s:%s/%s";

	public static boolean isOracleConnection(Connection c)
	{
	    String errMsg;
	    try
	    {
	        DatabaseMetaData dbmd = c.getMetaData();
	        if(dbmd.getDriverName().toUpperCase().indexOf("ORACLE") > 0)
	            return true;
	        else
	            return false;
	    }
	    catch(SQLException sqle)
	    {
	        errMsg = "DBConnection : Impossible d'obtenir les métadonnées de la connection :\n" + sqle.getMessage();
	    }
	    return false;
	}
	
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
				.getProperty(ConfigurationParameters.DB_JDBC_DRIVER_KEY);
		if (!DbUtils.loadDriver(driver)) {

			// Si le driver n'est pas détecté l'application s'arréte

			String msg = "Driver `" + driver + "` absent.";
			System.err.println(msg);

			throw new DatabaseDriverNotFoundException(msg);
		}
		// Set jdbc database url according driver.
		String url = "";
		if (p.getProperty(ConfigurationParameters.DB_JDBC_DRIVER_KEY).contains("oracle")) {
			url = String.format(URL_ORACLE,

					p.getProperty(ConfigurationParameters.DB_HOST_KEY),
					p.getProperty(ConfigurationParameters.DB_PORT_KEY),
					p.getProperty(ConfigurationParameters.DB_ID_KEY));

		} else if (p.getProperty(ConfigurationParameters.DB_JDBC_DRIVER_KEY).contains("mysql")) {
			url = String.format(URL_MYSQL,
					p.getProperty(ConfigurationParameters.DB_HOST_KEY),
					p.getProperty(ConfigurationParameters.DB_PORT_KEY),
					p.getProperty(ConfigurationParameters.DB_ID_KEY));
		}else if(p.getProperty(ConfigurationParameters.DB_JDBC_DRIVER_KEY).contains("postgresql")){
			url = String.format(URL_POSTGRE,
					p.getProperty(ConfigurationParameters.DB_HOST_KEY),
					p.getProperty(ConfigurationParameters.DB_PORT_KEY),
					p.getProperty(ConfigurationParameters.DB_ID_KEY));
		}
		// else if (){} and so on...

		// établissement de la connexion au SGBD
		try {
			connect = DriverManager.getConnection(url,
					p.getProperty(ConfigurationParameters.DB_USER_KEY),
					p.getProperty(ConfigurationParameters.DB_PASS_KEY));

			// TODO : ATTENTION au CAST !!!!!!!!   
			connect.setAutoCommit(Boolean.parseBoolean(p.getProperty(
					ConfigurationParameters.DB_AUTOCOMMIT_KEY, "false")));

		} catch (SQLException sqle) {

			String msg = "Problème de connexion à la base de données :\n"
                    + "URL = " + url + "\n"
			        + "User = " + p.getProperty(ConfigurationParameters.DB_USER_KEY) + "\n"
                    + "Password = " + p.getProperty(ConfigurationParameters.DB_PASS_KEY) + "\n"
                    + "Autocommit " + (Boolean.parseBoolean(p.getProperty(ConfigurationParameters.DB_AUTOCOMMIT_KEY, "false")) ? "on" : "off") + "\n"
			        + sqle.getMessage();
			System.err.println(msg);
			throw new ConfigurationBatchException(msg);
		}

		return connect;
	}
	
	public static OraclePreparedStatement oraclePrepareStatement(Connection c, String query) throws SQLException
	{
	    OracleConnection oc;
	    oc = (OracleConnection) c;
	    return ((OraclePreparedStatement)oc.prepareStatement(query));
	}
	
	public static void oracleSetArray(Connection c, PreparedStatement preparedStatement, int paramIndex, String dataType, Object[] paramArray) throws SQLException
	{
	    OraclePreparedStatement ops = (OraclePreparedStatement)preparedStatement;
	    ops.setARRAY(paramIndex, oracle.sql.ARRAY.toARRAY(paramArray, (OracleConnection)c));
	}
	
	public static ResultSet oraclePreparedStatementExecuteQuery(PreparedStatement preparedStatement) throws SQLException
	{
	    OraclePreparedStatement ops = (OraclePreparedStatement)preparedStatement;
	    return(ops.executeQuery());
	}
}
