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
     * CSV : s�parateur de champ par d�faut
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
     *            journal d'�v�nements
     */
    public FileWriter(Logger logger) {
        this.logger = logger;
    }

    /**
     * M�thode pour �crire un fichier au format texte � partir d'une chaine type
     * C.
     * 
     * @param filename
     *            le nom du fichier � �crire.
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
            String msg = "Erreur lors de l'�criture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

    /**
     * Wrapper de la m�thode
     * <code>writeTextFile(String filename, char[] content)</code>.
     * 
     * @param filename
     *            le nom du fichier � �crire.
     * @param content
     *            le contenu
     * @throws FileWriterException
     */
    public void writeTextFile(String filename, String content)
            throws FileWriterException {
        writeTextFile(filename, content.toCharArray());
    }

    /**
     * M�thode pour �crire un fichier au format XML � partir d'un objet
     * <code>org.jdom2.Document</code>.
     * 
     * @param filename
     *            le nom du fichier � �crire.
     * @param document
     *            le document XML
     * @throws FileWriterException
     */
    public void writeXMLFile(String filename, Document document)
            throws FileWriterException {
        writeXMLFile(filename, document, DEFAULT_ENCODING);
    }

    /**
     * M�thode pour �crire un fichier au format XML � partir d'un objet
     * <code>org.jdom2.Document</code>.
     * 
     * @param filename
     *            le nom du fichier � �crire.
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
            String msg = "Erreur lors de l'�criture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

    /**
     * Wrapper de la m�thode
     * <code>writeXMLFile(String filename, Document document)</code> pour �crire
     * un fichier au format XML � partir d'un objet
     * <code>org.jdom2.Element</code>. Le document conteneur est automatiquement
     * cr��.
     * 
     * @param filename
     *            le nom du fichier � �crire.
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
     * Ecrit le r�sultat d'une requ�te dans un fichier au format CSV. Attention,
     * dans cette m�thode il n'y a pas d'ent�tes.<br>
     * 
     * Evolutions futures:<br>
     * - passer le s�parateur de champ en option.
     * 
     * @param filename
     *            le fichier de destination. Exemple :
     *            <code>C:\Users\galloiem\workspace\GenericBatchSampleProject\log\out.txt</code>
     * @param values
     *            l'ensemble de r�sultat d'une requ�te sous la forme d'un
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
            logger.info("Fichier �crit.");

        } catch (Exception e) {
            String msg = "Erreur lors de l'�criture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
    }

}
