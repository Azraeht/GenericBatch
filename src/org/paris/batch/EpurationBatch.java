package org.paris.batch;

import java.io.IOException;

import org.paris.batch.exception.NoPropertiesFoundException;

public class EpurationBatch extends GenericBatch {
	public EpurationBatch() throws NoPropertiesFoundException, IOException {
		super();
	}

	public static void main(String[] args) throws Exception {
		//Constructeur de la classe GenericBatch afin d'instancier le log 
		//et le fichier de propri�t�s
		GenericBatch Batch = new EpurationBatch();
		
		//Configuration d'une connexion � une base de donn�es Oracle
		Batch.dbConnect();	
		
		//Ex�cution d'une requ�te de type SELECT situ�e dans le fichier de propri�t�s
		//Le nom de la requ�te est : nbLignes
		Batch.executeSelect("nbLignes");
		
		//Fermeture de la connexion � la base de donn�es Oracle
		Batch.dbDisconnect();
			
			
	}
}

