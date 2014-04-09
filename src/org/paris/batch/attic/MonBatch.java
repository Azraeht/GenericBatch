package org.paris.batch.attic;

import org.paris.batch.GenericBatch;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.NoPropertiesFoundException;

public class MonBatch extends GenericBatch {
	public MonBatch() throws NoPropertiesFoundException, ConfigurationBatchException {
		super();
	}

	public static void main(String[] args) throws Exception {
		//Constructeur de la classe GenericBatch afin d'instancier le log
	//	GenericBatch Batch = new GenericBatch();
		
		//Configuration du nom et du repertoire du log
	//	Batch.logger.configurationLog();
		//Configuration d'une connexion à une base de données Oracle
	//	Batch.dbConnect();
		
		//Batch.dbConnect("10.161.98.127", "1521", "S40Rec7", "editions", "editions");
		
		//Exécution d'une requête de type SELECT située dans le fichier de propriétés
		//Le nom de la requête est : nbLignes
//		Batch.executeSelect("nbLignes");
		//Fermeture de la connexion à la base de données Oracle
	//	Batch.dbDisconnect();
		
		
	}

    @Override
    public void setup() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finished() {
        // TODO Auto-generated method stub
        
    }
}
