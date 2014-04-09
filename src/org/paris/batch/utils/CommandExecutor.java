package org.paris.batch.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.paris.batch.exception.CommandExecutorException;

/**
 * @author galloiem
 * 
 */
public class CommandExecutor {

    private Logger logger;

    /**
     * 
     */
    public CommandExecutor(Logger logger) {
        this.logger = logger;
    }

    public int runCommand(String cmd) throws CommandExecutorException {
        logger.info("Exécution de la commande : " + cmd);
        final Runtime r = Runtime.getRuntime();
        int returnCode = -1;
        try {
            final Process p = r.exec(cmd);
            returnCode = p.waitFor();
            String line;
            final BufferedReader stderr = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()));
            while ((line = stderr.readLine()) != null) {
                logger.warn(line);
            }
        } catch (Exception e) {
            String msg = "Erreur d'exécution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);
            
            throw new CommandExecutorException(msg);
        }

        return returnCode;
    }

}
