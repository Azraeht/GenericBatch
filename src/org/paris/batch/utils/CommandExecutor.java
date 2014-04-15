package org.paris.batch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.paris.batch.exception.CommandExecutorException;

/**
 * Exécution de commandes sur le système hote.
 * 
 * @author galloiem
 * 
 */
public class CommandExecutor {

    private Logger logger;

    /**
     * Constructeur
     * 
     * @param logger
     *            journal d'événements
     */
    public CommandExecutor(Logger logger) {
        this.logger = logger;
    }

    /**
     * Exécute la commande spécifiée
     * 
     * @param cmd
     *            la commande à exécuter
     * @return La sortie standard de l'exécution de la commande
     * @throws CommandExecutorException
     */
    public String runCommand(String cmd) throws CommandExecutorException {
        logger.info("Exécution de la commande : `" + cmd + "`");
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            String msg = "Erreur d'exécution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);

            throw new CommandExecutorException(msg);
        }
        logger.debug("Sortie de la commande: \n" + output);
        logger.info("Commande `" + cmd + "` exécutée.");

        return output.toString();
    }

    @SuppressWarnings("unused")
    private int runCommandInt(String cmd) throws CommandExecutorException {
        logger.info("Exécution de la commande : " + cmd);
        final Runtime r = Runtime.getRuntime();
        int returnCode = -1;
        try {
            Process p = r.exec(cmd);
            returnCode = p.waitFor();
        } catch (Exception e) {
            String msg = "Erreur d'exécution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);

            throw new CommandExecutorException(msg);
        }

        return returnCode;
    }

    @SuppressWarnings("unused")
    private int runCommandWithStreams(String cmd)
            throws CommandExecutorException {
        logger.info("Exécution de la commande : " + cmd);
        int result = -1;
        Runtime runtime = Runtime.getRuntime();
        final Process process;
        try {
            process = runtime.exec(cmd);
            result = process.exitValue();
            // Consommation de la sortie standard de l'application externe dans
            // un Thread separe
            new Thread() {
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        String line = "";
                        try {
                            while ((line = reader.readLine()) != null) {
                                logger.debug(line);
                            }
                        } finally {
                            reader.close();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }.start();

            // Consommation de la sortie d'erreur de l'application externe dans
            // un Thread separe
            new Thread() {
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()));
                        String line = "";
                        try {
                            while ((line = reader.readLine()) != null) {
                                logger.debug(line);
                            }
                        } finally {
                            reader.close();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }.start();

        } catch (Exception e) {
            String msg = "Erreur d'exécution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);

            throw new CommandExecutorException(msg);
        }
        return result;
    }
}
