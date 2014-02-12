package org.paris.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;

import org.paris.batch.exception.DatabaseConnectionFailedException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;

public class DBBatch {
	/**
	 * Nom du driver
	 */
	private static String jdbcDriver = "oracle.jdbc.OracleDriver";
	/**
	 * Objet Connection
	 */
	static Connection connect;
	/**
	 * Objet Properties
	 */
	static Properties propertie;
	
	/**
	 * Méthode qui retourne l'instance
	 * et la crée si elle n'existe pas.
	 * @param urlOracle URL de connexion
	 * @param portOracle Port de connexion
	 * @param SIDOracle SID de connexion
	 * @return Connection On obtient une connexion
	 * @throws DatabaseDriverNotFoundException Le driver est absent
	 * @throws NoPropertiesFoundException 
	 */
	public static Connection getInstance() throws DatabaseDriverNotFoundException, DatabaseConnectionFailedException, NoPropertiesFoundException {
		//chargement du driver
		boolean conn;
		conn = DbUtils.loadDriver(jdbcDriver);
		
		propertie = new Properties();
		String cheminDAccesProperties = "config.properties";
		String workingDir = System.getProperty("user.dir")+ "\\config\\";
		File temp = new File(workingDir, "config.properties");		
		
		//On teste l'existence du fichier de propriétés
		try {
			//props.load(classLoader.getResourceAsStream("\\query.properties"));
			InputStream resourceAsStream =  new FileInputStream(temp);
		    if (resourceAsStream != null) {
		        propertie.load(resourceAsStream);
		    }
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException au chargement du fichier de propriétés :\n\t" + cheminDAccesProperties + "\n" + npe.toString());
			throw new NoPropertiesFoundException(npe.getMessage());
		} catch (IOException ioe) {
			System.out.println("IOException au chargement du fichier de propriétés :\n\t" + cheminDAccesProperties + "\n" + ioe.getMessage());
			throw new NoPropertiesFoundException(ioe.getMessage());
		}
		
		try{
			if(!conn){
				//Si le driver n'est pas détecté l'application s'arrête
				System.out.println("Driver absent.");

				throw(new DatabaseDriverNotFoundException("Ouhlà, le driver paraît absent. On est grave dans la mouise cap'taine"));

			}
		} catch (DatabaseDriverNotFoundException ddne){
			System.out.println("DatabaseDriverNotFoundException au chargement du driver :\n\t" 
		+ "\n" + ddne.getMessage());
			throw new DatabaseDriverNotFoundException (ddne.getMessage());
			
		}
		
		
		if(connect == null){
			try {
				//Établissement de la connexion au SGBD
				connect = DriverManager.getConnection("jdbc:oracle:thin:@"+propertie.getProperty("urlOracle")+":"
				+propertie.getProperty("portOracle")+":"+propertie.getProperty("SIDOracle"), propertie.getProperty("user"), 
				propertie.getProperty("pass"));
				connect.setAutoCommit(false);
			} catch (SQLException sqle) {
				System.out.println("Problème de connexion à la base de données :\n\t" 
						+ "\n" + sqle.getMessage());
				throw new DatabaseDriverNotFoundException (sqle.getMessage());
			}
		}
		return connect;	
	}
	
	/**
	 * Méthode qui retourne l'instance MySQL
	 * et la crée si elle n'existe pas.
	 * @param urlMySQL URL de connexion
	 * @param baseMySQL nom de la base de donnée
	 * @return Connection On obtient une connexion
	 * @throws ClassNotFoundException Le driver est absent
	 */
	public static Connection getInstance(String urlMySQL, 
			String baseMySQL, String userMySQL, String passMySQL) throws DatabaseDriverNotFoundException, DatabaseConnectionFailedException, ClassNotFoundException {
		//chargement du driver
		
		
		try{
			if(Class.forName("com.mysql.jdbc.Driver") == null){
				//Si le driver n'est pas détecté l'application s'arrête
				System.out.println("Driver absent.");

				throw(new DatabaseDriverNotFoundException("Ouhlà, le driver paraît absent. On est grave dans la mouise cap'taine"));
				
			}
		} catch (DatabaseDriverNotFoundException ddne){
			System.out.println("DatabaseDriverNotFoundException au chargement du driver :\n\t" 
		+ "\n" + ddne.getMessage());
			throw new DatabaseDriverNotFoundException (ddne.getMessage());
			
		}
		
		
		if(connect == null){
			try {
				//Établissement de la connexion au SGBD
				connect = DriverManager.getConnection("jdbc:mysql://"+propertie.getProperty("urlMySQL")+"/"
				+propertie.getProperty("baseMySQL"), propertie.getProperty("userMySQL"),
				propertie.getProperty("passMySQL"));
				connect.setAutoCommit(false);
			} catch (SQLException sqle) {
				System.out.println("Problème de connexion à la base de données :\n\t" 
						+ "\n" + sqle.getMessage());
				throw new DatabaseConnectionFailedException (sqle.getMessage());
			}
		}
		return connect;	
	}
	
	/**
	 * Méthode qui termine l'instance
	 */
	public static void closeInstance(){
		if(connect != null){
			DbUtils.closeQuietly(connect);
		} else {
			LogBatch.logBatch.info("Impossible de fermer la connexion");
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);			
		}
	}
}
