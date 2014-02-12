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
	 * Méthode permettant d'utiliser le fichier de configuration query.properties.
	 * @throws NoPropertiesFoundException
	 */
	public static void chargerProperties() throws Throwable {
		//ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		props = new Properties();
		String cheminDAccesProperties = "query.properties";
		String workingDir = System.getProperty("user.dir")+ "\\config\\";
		File temp = new File(workingDir, "query.properties");		
		
		//On teste l'existence du fichier de propriétés
		try {
			//props.load(classLoader.getResourceAsStream("\\query.properties"));
			InputStream resourceAsStream =  new FileInputStream(temp);
		    if (resourceAsStream != null) {
		        props.load(resourceAsStream);
		    }
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException au chargement du fichier de propriétés :\n\t" + cheminDAccesProperties + "\n" + npe.toString());
			throw new NoPropertiesFoundException(npe.getMessage());
		} catch (IOException ioe) {
			System.out.println("IOException au chargement du fichier de propriétés :\n\t" + cheminDAccesProperties + "\n" + ioe.getMessage());
			throw new NoPropertiesFoundException(ioe.getMessage());
		} 	
	}
	
	/**
	 * Méthode permettant de vérifier l'accès au fichier de configuration query.properties.
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
	 * Méthode pour se connecter à la base de données.
	 * @throws Exception 
	 */
	public void dbConnect()
		throws Exception {
		//Connexion à la base de donnée.
		try{
			DBBatch.getInstance();
			this.logger.getLogBatch().info("Connexion à la base de données établie.");
		} 
		catch(Throwable t){
			System.err.println(t.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_ERROR);
		}
	}
	
	/**
	 * Méthode pour se déconnecter à la base de données.
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
	 * Objet List dans lequel sera stocké le résultat de la requête "SELECT".
	 */ 
	private List<Object[]> list1;
	
	/**
	 * Méthode pour effectuer une requête "SELECT" dans une base de données
	 * et extraire le résultat dans un tableau
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return Le résultat de la requête.
	 */
	public List<?> executeSelectList(String nomQuery) {
		list1 = null;
		try {
			QueryRunner qRunner = new QueryRunner();
			list1 =	qRunner.query(DBBatch.connect,props.getProperty(nomQuery),new ArrayListHandler());
			LogBatch.logBatch.info("Requête exécutée.");
			
			for (int i = 0; i < list1.size(); i++) {
				Object data[] = (Object[]) list1.get(i);
				System.out.println("Résultat : "+data[0]);
				LogBatch.logBatch.info(data[0]+" lignes.");
			}
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException à l'exécution de la requête :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			System.exit(CodeRetourBatch.EXIT_ERROR) ;
			sqle.printStackTrace();
		}
		return list1;
	}
	
	/**
	 * Méthode pour effectuer une requête "SELECT" dans une base de données
	 * et extraire le résultat dans un resultSet
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return Le résultat de la requête.
	 * @throws SQLException 
	 */
	public ResultSet executeSelect(String nomQuery) throws SQLException {
		Statement statement = null;
		ResultSet resultat = null;
		
		try{
			statement = DBBatch.connect.createStatement();
			resultat = statement.executeQuery(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requête exécutée.");
			
		}catch (SQLException sqle) {
			System.out.println("SQLException à l'exécution de la requête :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			System.exit(CodeRetourBatch.EXIT_ERROR) ;
			sqle.printStackTrace();
		}finally {
	        if (statement != null) { statement.close(); }
	    }
		return resultat;
	}
	
	/**
	 * Variable integer qui indiquera si la requête "UPDATE" sera effectuée.
	 */
	private int list2;
	
	/**
	 * Méthode pour effectuer une requête "UPDATE" dans une base de données
	 * et extraire le résultat dans un tableau.
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return 1 si la requête s'est executée ou 0 si elle ne s'est pas correctement exécutée.
	 */
	public int executeUpdate(String nomQuery) {
		Statement statement = null;
		list2 = 0;
		
		try {
			statement = DBBatch.connect.createStatement();
			list2 = statement.executeUpdate(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requête exécutée.");
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException à l'exécution de la requête :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			sqle.printStackTrace();
		}
		return list2;
	}
	
	/**
	 * Variable integer qui indiquera si la requête "INSERT" sera effectuée.
	 */
	private int list3;
	
	/**
	 * Méthode pour effectuer une requête "INSERT" dans une base de données
	 * et extraire le résultat dans un tableau
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return 1 si la requête s'est executée ou 0 si elle ne s'est pas correctement exécutée.
	 */
	public int executeInsert(String nomQuery) {
		Statement statement = null;
		list3 = 0;
		
		try {
			statement = DBBatch.connect.createStatement();
			list3 = statement.executeUpdate(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requête exécutée.");
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException à l'exécution de la requête :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			sqle.printStackTrace();
		}
		return list3;
	}
	
	/**
	 * Variable integer qui indiquera si la requête "DELETE" sera effectuée.
	 */
	private int list4;
	
	/**
	 * Méthode pour effectuer une requête "DELETE" dans une base de données
	 * et extraire le résultat dans un tableau
	 * @param nomQuery nom de la variable provenant du fichier properties.
	 * @return 1 si la requête s'est executée ou 0 si elle ne s'est pas correctement exécutée.
	 */
	public int executeDelete(String nomQuery) {
		Statement statement = null;
		list4 = 0;
		
		try {
			statement = DBBatch.connect.createStatement();
			list4 = statement.executeUpdate(props.getProperty(nomQuery));
			LogBatch.logBatch.info("Requête exécutée.");
		} 
		catch (SQLException sqle) {
			System.out.println("SQLException à l'exécution de la requête :\n\t" + props.getProperty(nomQuery) + "\n" + sqle.getMessage());
			LogBatch.logBatch.info("Code de retour : "+CodeRetourBatch.EXIT_WARNING);
			sqle.printStackTrace();
		}
		return list4;
	}
	
	/**
	 * Méthode pour gérer l'arrêt du batch après une exception.
	 */
	public static void exitFailure(){
		System.exit(0);
		LogBatch.logBatch.info("Exception levée, arrêt du batch.");
	}
	
	
	
	public static void main(String[] args) throws Exception {

	}
}
