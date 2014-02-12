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
	 * M�thode qui retourne l'instance
	 * et la cr�e si elle n'existe pas.
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
		
		//On teste l'existence du fichier de propri�t�s
		try {
			//props.load(classLoader.getResourceAsStream("\\query.properties"));
			InputStream resourceAsStream =  new FileInputStream(temp);
		    if (resourceAsStream != null) {
		        propertie.load(resourceAsStream);
		    }
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException au chargement du fichier de propri�t�s :\n\t" + cheminDAccesProperties + "\n" + npe.toString());
			throw new NoPropertiesFoundException(npe.getMessage());
		} catch (IOException ioe) {
			System.out.println("IOException au chargement du fichier de propri�t�s :\n\t" + cheminDAccesProperties + "\n" + ioe.getMessage());
			throw new NoPropertiesFoundException(ioe.getMessage());
		}
		
		try{
			if(!conn){
				//Si le driver n'est pas d�tect� l'application s'arr�te
				System.out.println("Driver absent.");

				throw(new DatabaseDriverNotFoundException("Ouhl�, le driver para�t absent. On est grave dans la mouise cap'taine"));

			}
		} catch (DatabaseDriverNotFoundException ddne){
			System.out.println("DatabaseDriverNotFoundException au chargement du driver :\n\t" 
		+ "\n" + ddne.getMessage());
			throw new DatabaseDriverNotFoundException (ddne.getMessage());
			
		}
		
		
		if(connect == null){
			try {
				//�tablissement de la connexion au SGBD
				connect = DriverManager.getConnection("jdbc:oracle:thin:@"+propertie.getProperty("urlOracle")+":"
				+propertie.getProperty("portOracle")+":"+propertie.getProperty("SIDOracle"), propertie.getProperty("user"), 
				propertie.getProperty("pass"));
				connect.setAutoCommit(false);
			} catch (SQLException sqle) {
				System.out.println("Probl�me de connexion � la base de donn�es :\n\t" 
						+ "\n" + sqle.getMessage());
				throw new DatabaseDriverNotFoundException (sqle.getMessage());
			}
		}
		return connect;	
	}
	
	/**
	 * M�thode qui retourne l'instance MySQL
	 * et la cr�e si elle n'existe pas.
	 * @param urlMySQL URL de connexion
	 * @param baseMySQL nom de la base de donn�e
	 * @return Connection On obtient une connexion
	 * @throws ClassNotFoundException Le driver est absent
	 */
	public static Connection getInstance(String urlMySQL, 
			String baseMySQL, String userMySQL, String passMySQL) throws DatabaseDriverNotFoundException, DatabaseConnectionFailedException, ClassNotFoundException {
		//chargement du driver
		
		
		try{
			if(Class.forName("com.mysql.jdbc.Driver") == null){
				//Si le driver n'est pas d�tect� l'application s'arr�te
				System.out.println("Driver absent.");

				throw(new DatabaseDriverNotFoundException("Ouhl�, le driver para�t absent. On est grave dans la mouise cap'taine"));
				
			}
		} catch (DatabaseDriverNotFoundException ddne){
			System.out.println("DatabaseDriverNotFoundException au chargement du driver :\n\t" 
		+ "\n" + ddne.getMessage());
			throw new DatabaseDriverNotFoundException (ddne.getMessage());
			
		}
		
		
		if(connect == null){
			try {
				//�tablissement de la connexion au SGBD
				connect = DriverManager.getConnection("jdbc:mysql://"+propertie.getProperty("urlMySQL")+"/"
				+propertie.getProperty("baseMySQL"), propertie.getProperty("userMySQL"),
				propertie.getProperty("passMySQL"));
				connect.setAutoCommit(false);
			} catch (SQLException sqle) {
				System.out.println("Probl�me de connexion � la base de donn�es :\n\t" 
						+ "\n" + sqle.getMessage());
				throw new DatabaseConnectionFailedException (sqle.getMessage());
			}
		}
		return connect;	
	}
	
	/**
	 * M�thode qui termine l'instance
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
