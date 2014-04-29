package org.paris.batch.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.paris.batch.exception.FileWriterException;

/**
 * Gestion de fichiers
 * 
 * @author galloiem
 * 
 */
public class FileWriter {
    /**
     * CSV : séparateur de champ par défaut
     */
    public static final String CSV_DEFAULT_SEPARATOR = ";";
    /**
     * Encodage Unicode
     */
    public static final String ENCODING_UTF8 = "UTF-8";
    /**
     * Encodage Latin1
     */
    public static final String ENCODING_ISO8859 = "ISO8859-1";
    /**
     * DEFAULT_ENCODING
     */
    public static final String DEFAULT_ENCODING = ENCODING_ISO8859;

    private Logger logger;

    /**
     * Constructeur
     * 
     * @param logger
     *            journal d'événements
     */
    public FileWriter(Logger logger) {
        this.logger = logger;
    }

    /**
     * Méthode pour écrire un fichier au format texte à partir d'une chaine type
     * C.
     * 
     * @param filename
     *            le nom du fichier à écrire.
     * @param content
     *            le contenu
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
     * Wrapper de la méthode
     * <code>writeTextFile(String filename, char[] content)</code>.
     * 
     * @param filename
     *            le nom du fichier à écrire.
     * @param content
     *            le contenu
     * @throws FileWriterException
     */
    public void writeTextFile(String filename, String content)
            throws FileWriterException {
        writeTextFile(filename, content.toCharArray());
    }

    /**
     * Méthode pour écrire un fichier au format XML à partir d'un objet
     * <code>org.jdom2.Document</code>.
     * 
     * @param filename
     *            le nom du fichier à écrire.
     * @param document
     *            le document XML
     * @throws FileWriterException
     */
    public void writeXMLFile(String filename, Document document)
            throws FileWriterException {
        writeXMLFile(filename, document, DEFAULT_ENCODING);
    }

    /**
     * Méthode pour écrire un fichier au format XML à partir d'un objet
     * <code>org.jdom2.Document</code>.
     * 
     * @param filename
     *            le nom du fichier à écrire.
     * @param document
     *            le document XML
     * @param encoding
     *            le format d'encodage
     * @throws FileWriterException
     */
    public void writeXMLFile(String filename, Document document, String encoding)
            throws FileWriterException {
        logger.info("Ecriture du fichier : " + filename);
        try {

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat().setEncoding(encoding));
            xmlOutput.output(document, new java.io.FileWriter(filename));

        } catch (Exception e) {
            String msg = "Erreur lors de l'écriture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

    /**
     * Wrapper de la méthode
     * <code>writeXMLFile(String filename, Document document)</code> pour écrire
     * un fichier au format XML à partir d'un objet
     * <code>org.jdom2.Element</code>. Le document conteneur est automatiquement
     * créé.
     * 
     * @param filename
     *            le nom du fichier à écrire.
     * @param element
     *            le contenu
     * @throws FileWriterException
     */
    public void writeXMLFile(String filename, Element element)
            throws FileWriterException {
        Document document = new Document();
        document.setRootElement(element);
        writeXMLFile(filename, document);
    }

    /**
     * Ecrit le résultat d'une requête dans un fichier au format CSV. Attention,
     * dans cette méthode il n'y a pas d'entêtes.<br>
     * 
     * Evolutions futures:<br>
     * - passer le séparateur de champ en option.
     * 
     * @param filename
     *            le fichier de destination. Exemple :
     *            <code>C:\Users\galloiem\workspace\GenericBatchSampleProject\log\out.txt</code>
     * @param values
     *            l'ensemble de résultat d'une requête sous la forme d'un
     *            <code>ArrayListHandler</code>
     * @throws FileWriterException
     */
    public void writeCSVFile(String filename, List<Object[]> values)
            throws FileWriterException {
        logger.info("Ecriture du fichier : " + filename);
        try {
            File file = new File(filename);
            BufferedWriter out = new BufferedWriter(
                    new java.io.FileWriter(file));
            for (Object[] v : values) {
                String line = "";
                for (int i = 0; i < v.length; i++) {
                    if (v[i] != null) {
                        line += v[i].toString().trim();
                    }
                    if (i != v.length - 1) {
                        line += CSV_DEFAULT_SEPARATOR;
                    }
                }
                logger.debug("line : " + line);
                out.write(line + "\n");
            }
            out.close();
            logger.info("Fichier écrit.");

        } catch (Exception e) {
            String msg = "Erreur lors de l'écriture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

}
