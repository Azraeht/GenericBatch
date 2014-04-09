package org.paris.batch.attic;

import org.paris.batch.GenericBatch;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.NoPropertiesFoundException;

public class EpurationBatch extends GenericBatch {
    public EpurationBatch() throws NoPropertiesFoundException,  ConfigurationBatchException {
        super();
    }

    public static void main(String[] args) throws Exception {
        // Constructeur de la classe GenericBatch afin d'instancier le log
        // et le fichier de propriétés
        //EpurationBatch  Batch = new EpurationBatch();

        // Configuration d'une connexion à une base de données Oracle
        //Batch.dbConnect();

        // Exécution d'une requête de type SELECT située dans le fichier de
        // propriétés
        // Le nom de la requête est : nbLignes
      //  Batch.executeSelect("nbLignes");

        // Fermeture de la connexion à la base de données Oracle
      //  Batch.dbDisconnect();

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
