package org.paris.batch.utils;

import java.io.BufferedWriter;
import java.io.File;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.Format;
import org.paris.batch.exception.FileWriterException;

/**
 * @author galloiem
 * 
 */
public class FileWriter {

    /**
     * 
     */
    private Logger logger;

    /**
     * @param logger
     */
    public FileWriter(Logger logger) {
        this.logger = logger;
    }

    /**
     * @param filename
     * @param content
     * @throws FileWriterException
     */
    public void writeTextFile(String filename, char[] content)
            throws FileWriterException {
        logger.info("Ecriture du fichier : " + filename);
        try {
            File file = new File(filename);
            BufferedWriter out = new BufferedWriter(
                    new java.io.FileWriter(file));
            out.write(content);
            out.close();
        } catch (Exception e) {
            String msg = "Erreur lors de l'écriture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

    /**
     * Wrapper
     * 
     * @param filename
     * @param content
     * @throws FileWriterException
     */
    public void writeTextFile(String filename, String content)
            throws FileWriterException {
        writeTextFile(filename, content.toCharArray());
    }

    /**
     * @param filename
     * @param document
     * @throws FileWriterException
     */
    public void writeXMLFile(String filename, Document document)
            throws FileWriterException {
        logger.info("Ecriture du fichier : " + filename);
        try {

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new java.io.FileWriter(filename));

        } catch (Exception e) {
            String msg = "Erreur lors de l'écriture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

    /**
     * Wrapper
     * 
     * @param filename
     * @param element
     * @throws FileWriterException
     */
    public void writeXMLFile(String filename, Element element)
            throws FileWriterException {
        Document document = new Document();
        document.setRootElement(element);
        writeXMLFile(filename, document);
    }

}
