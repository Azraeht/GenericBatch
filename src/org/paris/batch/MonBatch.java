package org.paris.batch;

import java.io.IOException;

import org.paris.batch.exception.NoPropertiesFoundException;

public class MonBatch extends GenericBatch {
	public MonBatch() throws NoPropertiesFoundException, IOException {
		super();
	}

	public static void main(String[] args) throws Exception {
		//Constructeur de la classe GenericBatch afin d'instancier le log
		GenericBatch Batch = new GenericBatch();
		
		//Configuration du nom et du repertoire du log
		Batch.logger.configurationLog();
		//Configuration d'une connexion � une base de donn�es Oracle
		Batch.dbConnect();
		
		//Batch.dbConnect("10.161.98.127", "1521", "S40Rec7", "editions", "editions");
		
		//Ex�cution d'une requ�te de type SELECT situ�e dans le fichier de propri�t�s
		//Le nom de la requ�te est : nbLignes
		Batch.executeSelect("nbLignes");
		//Fermeture de la connexion � la base de donn�es Oracle
		Batch.dbDisconnect();
		
		
	}
}
