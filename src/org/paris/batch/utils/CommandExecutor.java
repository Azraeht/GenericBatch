package org.paris.batch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.paris.batch.exception.CommandExecutorException;

/**
 * Ex�cution de commandes sur le syst�me hote.
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
     *            journal d'�v�nements
     */
    public CommandExecutor(Logger logger) {
        this.logger = logger;
    }

    /**
     * Ex�cute la commande sp�cifi�e
     * 
     * @param cmd
     *            la commande � ex�cuter
     * @return La sortie standard de l'ex�cution de la commande
     * @throws CommandExecutorException
     */
    public String runCommand(String cmd) throws CommandExecutorException {
        logger.info("Ex�cution de la commande : `" + cmd + "`");
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
            String msg = "Erreur d'ex�cution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);

            throw new CommandExecutorException(msg);
        }
        logger.debug("Sortie de la commande: \n" + output);
        logger.info("Commande `" + cmd + "` ex�cut�e.");

        return output.toString();
    }

    @SuppressWarnings("unused")
    private int runCommandInt(String cmd) throws CommandExecutorException {
        logger.info("Ex�cution de la commande : " + cmd);
        final Runtime r = Runtime.getRuntime();
        int returnCode = -1;
        try {
            Process p = r.exec(cmd);
            returnCode = p.waitFor();
        } catch (Exception e) {
            String msg = "Erreur d'ex�cution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);

            throw new CommandExecutorException(msg);
        }

        return returnCode;
    }

    @SuppressWarnings("unused")
    private int runCommandWithStreams(String cmd)
            throws CommandExecutorException {
        logger.info("Ex�cution de la commande : " + cmd);
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
            String msg = "Erreur d'ex�cution de la commande : " + cmd + "\n"
                    + e.getMessage();
            logger.error(msg);

            throw new CommandExecutorException(msg);
        }
        return result;
    }
}
