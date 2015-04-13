package org.paris.batch;

import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.GenericBatch;
import org.paris.batch.database.SQLExecutor;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;

/**
 * @CHANGE_ME@ - Batch de ...
 * Veuillez décrire ici succinctement le fonctionnement du batch
 * 
 * @author
 * 
 */
public class @CHANGE_ME@ extends GenericBatch {

    /**
     * Méthode de démarrage du batch. On y traite les éventuels paramètres d'exécution reçus de la ligne de commande.
     * @param args
     */
    public static void main(String[] args) {
        @CHANGE_ME@ batch = null;
        int result = -1;
        try {
        	//appel au constructeur
            batch = new @CHANGE_ME@();
            try {
            	//préparer l'exécution du batch (connexion à la base de données, etc...)
                batch.setup();
                //exécuter le batch
                result = batch.run();

                //traiter le résultat reçu de l'exécution
                switch (result) {
                case @CHANGE_ME@.EXIT_OK:
                    batch.exitSuccess();
                    break;
                case @CHANGE_ME@.EXIT_WARNING:
                    batch.exitWarning();
                    break;
                case @CHANGE_ME@.EXIT_ERROR:
                    batch.exitFailure();
                    break;
                default:
                    batch.exitWarning();
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                batch.exitFailure();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
* Constructeur de la classe. Celui-ci exécute les tâches de configuration par le biais de la mécanique standard de la classe parente (GenericBatch)
     * @throws ConfigurationBatchException Dans le cas où le traitement de la configuration provoque une erreur irrécupérable
     * @throws NoPropertiesFoundException Dans le cas où un fichier de propriété indispensable à la configuration est introuvable.
     */
    public @CHANGE_ME@() throws ConfigurationBatchException,
            NoPropertiesFoundException {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.paris.batch.GenericBatch#finished()
     */
    @Override
    public void finished() throws SQLExecutorException {
      //TODO Implement me !!!
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.paris.batch.GenericBatch#run()
     */
    @Override
    public int run() {
      //TODO Implement and change me !!!
      return @CHANGE_ME@.EXIT_OK;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.paris.batch.GenericBatch#setup()
     */
    @Override
    public void setup() throws ConfigurationBatchException,
            DatabaseDriverNotFoundException {
      //TODO Implement me !!!
    }
}
