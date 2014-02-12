package org.paris.batch;

import java.io.IOException;

import org.paris.batch.exception.NoPropertiesFoundException;

public class EpurationBatch extends GenericBatch {
	public EpurationBatch() throws NoPropertiesFoundException, IOException {
		super();
	}

	public static void main(String[] args) throws Exception {
		//Constructeur de la classe GenericBatch afin d'instancier le log 
		//et le fichier de propriétés
		GenericBatch Batch = new EpurationBatch();
		
		//Configuration d'une connexion à une base de données Oracle
		Batch.dbConnect();	
		
		//Exécution d'une requête de type SELECT située dans le fichier de propriétés
		//Le nom de la requête est : nbLignes
		Batch.executeSelect("nbLignes");
		
		//Fermeture de la connexion à la base de données Oracle
		Batch.dbDisconnect();
			
			
	}
}

