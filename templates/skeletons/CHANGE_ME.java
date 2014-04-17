package org.paris.batch;

import org.paris.batch.ConfigurationManagerBatch;
import org.paris.batch.GenericBatch;
import org.paris.batch.database.SQLExecutor;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;

/**
 * @CHANGE_ME@
 * 
 * @author
 * 
 */
public class @CHANGE_ME@ extends GenericBatch {

    /**
     * @param args
     */
    public static void main(String[] args) {
        @CHANGE_ME@ batch = null;
        int result = -1;
        try {
            batch = new @CHANGE_ME@();
            try {
                batch.setup();
                result = batch.run();

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
     * @throws ConfigurationBatchException
     * @throws NoPropertiesFoundException
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

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.paris.batch.GenericBatch#run()
     */
    @Override
    public int run() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.paris.batch.GenericBatch#setup()
     */
    @Override
    public void setup() throws ConfigurationBatchException,
            DatabaseDriverNotFoundException {

    }
}
