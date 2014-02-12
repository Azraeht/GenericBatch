package org.paris.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;

import org.paris.batch.CodeRetourBatch;
import org.paris.batch.DBBatch;
import org.paris.batch.LogBatch;

import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.GenericBatchException;

/** 
 * @author Guillaume Weber
 */

public class GenericBatch {	
	
	public GenericBatch() throws NoPropertiesFoundException, IOException{
		this.logger = new LogBatch();
		this.logger.configurationLog();
		checkProperties();
	}
	
	/**
	 * Objet Properties
	 */
	static Properties props;
	
	/**
	 * Objet Logger
	 */
	protected LogBatch logger;
	
	
	/**
	 * M�thode permettant d'utiliser le fichier de configuration query.properties.
	 * @throws NoPropertiesFoundException
	 */
	public static void chargerProperties() throws Throwable {
		//ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		props = new Properties();
		String cheminDAccesProperties = "query.properties";
		String workingDir = System.getProperty("user.dir")+ "\\config\\";
		File temp = new File(workingDir, "query.properties");		
		
		//On teste l'existence du fichier de propri�t�s
		try {
			//props.load(classLoader.getResourceAsStream("\\query.properties"));
			InputStream resourceAsStream =  new FileInputStream(temp);
		    if (resourceAsStream != null) {
		        props.load(resourceAsStream);
		    }
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException au chargement du fichier de propri�t�s :\n\t" + cheminDAccesProperties + "\n" + npe.toString());
			throw new NoPropertiesFoundException(npe.getMessage());
		} catch (IOException ioe) {
			System.out.println("IOException au chargement du fichier de propri�t�s :\n\t" + cheminDAccesProperties + "\n" + ioe.getMessage());
			throw new NoPropertiesFoundException(ioe.getMessage());
		} 	
	}
	
	/**
	 * M�thode permettant de v�rifier l'acc�s au fichier de configuration query.properties.
	 */
	public void checkProperties() {
		try {
			chargerProperties();
			LogBatch.logBatch.info("Chargement du fichier query.properties.");
		}
		catch(Throwable t){
			System.err.println(t.getMessage());
			//LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
		}
	}
	
	/**
	 * M�thode pour se connecter � la base de donn�es.
	 * @throws Exception 
	 */
	public void dbConnect()
		throws Exception {
		//Connexion � la base de donn�e.
		try{
			DBBatch.getInstance();
			this.logger.getLogBatch().info("Connexion � la base de donn�es �tablie.");
		} 
		catch(Throwable t){
			System.err.println(t.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
		}
	}
	
	/**
	 * M�thode pour se d�connecter � la base de donn�es.
	 */
	public void dbDisconnect() throws GenericBatchException {
		try{
			DBBatch.closeInstance();
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_OK);
			System.exit(CodeRetourBatch.EXIT_OK) ;
		}
		catch(Throwable t) {
			System.err.println(t.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
			System.exit(CodeRetourBatch.EXIT_ERROR);
		}
	}
	
	/**
	 * Objet List dans lequel sera stock� le r�sultat de la requ�te "SELECT".
	 */ 
	private List<Object[]> list1;
	
	/**
	 * M�thode pour effectuer une requ�te "SELECT" dans une base de donn�es
	 * et extraire le r�sultat dans un tableau
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return Le r�sultat de la requ�te.
	 */
	public List<?> executeSelectList(String nomQuery) {
		list1 = null;
		try {
			QueryRunner qRunner = new QueryRunner();
			list1 =	qRunner.query(DBBatch.connect,props.getProperty(nomQuery),new ArrayListHandler());
			LogBatch.logBatch.info("Requ�te ex�cut�e.");
			
			for (int i = 0; i < list1.size(); i++) {
				Object data[] = (Object[]) list1.get(i);
				System.out.println("R�sultat : "+data[0]);
				LogBatch.logBatch.info(data[0]+" lignes.");
			}
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException � l'ex�cution de la requ�te :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			System.exit(CodeRetourBatch.EXIT_ERROR) ;
			sqle.printStackTrace();
		}
		return list1;
	}
	
	/**
	 * M�thode pour effectuer une requ�te "SELECT" dans une base de donn�es
	 * et extraire le r�sultat dans un resultSet
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return Le r�sultat de la requ�te.
	 * @throws SQLException 
	 */
	public ResultSet executeSelect(String nomQuery) throws SQLException {
		Statement statement = null;
		ResultSet resultat = null;
		
		try{
			statement = DBBatch.connect.createStatement();
			resultat = statement.executeQuery(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requ�te ex�cut�e.");
			
		}catch (SQLException sqle) {
			System.out.println("SQLException � l'ex�cution de la requ�te :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			System.exit(CodeRetourBatch.EXIT_ERROR) ;
			sqle.printStackTrace();
		}finally {
	        if (statement != null) { statement.close(); }
	    }
		return resultat;
	}
	
	/**
	 * Variable integer qui indiquera si la requ�te "UPDATE" sera effectu�e.
	 */
	private int list2;
	
	/**
	 * M�thode pour effectuer une requ�te "UPDATE" dans une base de donn�es
	 * et extraire le r�sultat dans un tableau.
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas correctement ex�cut�e.
	 */
	public int executeUpdate(String nomQuery) {
		Statement statement = null;
		list2 = 0;
		
		try {
			statement = DBBatch.connect.createStatement();
			list2 = statement.executeUpdate(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requ�te ex�cut�e.");
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException � l'ex�cution de la requ�te :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			sqle.printStackTrace();
		}
		return list2;
	}
	
	/**
	 * Variable integer qui indiquera si la requ�te "INSERT" sera effectu�e.
	 */
	private int list3;
	
	/**
	 * M�thode pour effectuer une requ�te "INSERT" dans une base de donn�es
	 * et extraire le r�sultat dans un tableau
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas correctement ex�cut�e.
	 */
	public int executeInsert(String nomQuery) {
		Statement statement = null;
		list3 = 0;
		
		try {
			statement = DBBatch.connect.createStatement();
			list3 = statement.executeUpdate(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requ�te ex�cut�e.");
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException � l'ex�cution de la requ�te :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			sqle.printStackTrace();
		}
		return list3;
	}
	
	/**
	 * Variable integer qui indiquera si la requ�te "DELETE" sera effectu�e.
	 */
	private int list4;
	
	/**
	 * M�thode pour effectuer une requ�te "DELETE" dans une base de donn�es
	 * et extraire le r�sultat dans un tableau
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return 1 si la requ�te s'est execut�e ou 0 si elle ne s'est pas correctement ex�cut�e.
	 */
	public int executeDelete(String nomQuery) {
		Statement statement = null;
		list4 = 0;
		
		try {
			statement = DBBatch.connect.createStatement();
			list4 = statement.executeUpdate(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requ�te ex�cut�e.");
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException � l'ex�cution de la requ�te :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			sqle.printStackTrace();
		}
		return list4;
	}
	
	/**
	 * M�thode pour g�rer l'arr�t du batch apr�s une exception.
	 */
	public static void exitFailure(){
		System.exit(0);
		LogBatch.logBatch.info("Exception lev�e, arr�t du batch.");
	}
	
	
	
	public static void main(String[] args) throws Exception {

	}
}
