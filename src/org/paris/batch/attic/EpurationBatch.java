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
        // et le fichier de propri�t�s
        //EpurationBatch  Batch = new EpurationBatch();

        // Configuration d'une connexion � une base de donn�es Oracle
        //Batch.dbConnect();

        // Ex�cution d'une requ�te de type SELECT situ�e dans le fichier de
        // propri�t�s
        // Le nom de la requ�te est : nbLignes
      //  Batch.executeSelect("nbLignes");

        // Fermeture de la connexion � la base de donn�es Oracle
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
