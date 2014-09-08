package org.paris.batch.utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
    public static final String DEFAULT_ENCODING = ENCODING_UTF8;

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
<<<<<<< HEAD
     *            le nom du fichier à écrire.
=======
     *            le nom du fichier � �crire.
>>>>>>> refs/remotes/GitServer/master
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
            //forcer le formatage du XML à l'encodage passé en param�tre
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

     * Ecrit le résultat d'une requéte dans un fichier au format CSV dans le format par défaut UTF8. Attention,
     * dans cette méthode il n'y a pas d'entétes.<br>
     * Evolutions futures:<br>
     * - passer le séparateur de champ en option.
     * 
     * @param filename
     *            le fichier de destination. Exemple :
     *            <code>C:\Users\galloiem\workspace\GenericBatchSampleProject\log\out.txt</code>
     *      
     * @param values
     *            l'ensemble de résultat d'une requéte sous la forme d'un

     *            <code>ArrayListHandler</code>
     *            
     * @param columnHeader
     *            la ligne d'en-tête devant apparaître au début du fichier CSV (si elle est vide ou nulle, on
     *            ne met rien, évidemment !)  
     * 
     * @throws FileWriterException
     *            si quelque chose foire à l'écriture du fichier
     */
    public void writeCSVFile(String filename, List<Object[]> values, String columnHeader) throws FileWriterException
    {
    	writeCSVFile(filename,values,columnHeader,DEFAULT_ENCODING);
    }
    
    /**

     * Ecrit le résultat d'une requéte dans un fichier au format CSV. Attention,
     * dans cette méthode il n'y a pas d'entétes.<br>
     * Evolutions futures:<br>
     * - passer le séparateur de champ en option.
     * 
     * @param filename
     *            le fichier de destination. Exemple :
     *            <code>C:\Users\galloiem\workspace\GenericBatchSampleProject\log\out.txt</code>
     *      
     * @param values
     *            l'ensemble de résultat d'une requéte sous la forme d'un

     *            <code>ArrayListHandler</code>
     *            
     * @param columnHeader
     *            la ligne d'en-tête devant apparaître au début du fichier CSV (si elle est vide ou nulle, on
     *            ne met rien, évidemment !)  
     * @param encoding
     * 			format d'encodage du fichier de sortie
     * 
     * @throws FileWriterException
     *            si quelque chose foire à l'écriture du fichier
     */
    public void writeCSVFile(String filename, List<Object[]> values, String columnHeader,String encoding) throws FileWriterException
    {
        logger.info("Ecriture du fichier : " + filename); 
        try 
        {
            //ouvrir le fichier et déclarer un BufferedWriter dessus (en utilisant l'encodage par défaut)
            File file = new File(filename);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),encoding));
            
            //traiter la ligne d'en-tête
            if(columnHeader != null)
            {
                if(columnHeader.length()>0)
                {
                    //on n'écrit l'en-tête que s'il n'est pas vide ou null (paskeu sinon, forcément, ça chie)
                    logger.debug("écriture de l'en-tête");
                    out.write(columnHeader + "\n");
                }
            }
            
            //itérer sur la liste d'objets values passée en paramètre (un élément de values = une ligne de données à sortir)
            for (Object[] v : values)
            {
                //initialiser la ligne active à vide
                String line = "";
                for (int i = 0; i < v.length; i++) 
                {
                    //pour chaque item du tableau v
                    if (v[i] != null) 
                    {
                        //concaténer la ligne avec la valeur au format string trimmé
                        line += v[i].toString().trim();
                    }
                    if (i != v.length - 1) 
                    {
                        // ...et rajouter un séparateur si on n'est pas sur la dernière valeur de la ligne
                        line += CSV_DEFAULT_SEPARATOR;
                    }
                }
                //écrire la ligne dans le fichier de sortie
                logger.debug("line : " + line);
                out.write(line + "\n");
            }
            //finito : on ferme le fichier et ya basta !
            out.close();
            logger.info("Fichier écrit.");


        } catch (Exception e) {
            String msg = "Erreur lors de l'écriture du fichier '" + filename
                    + "\n" + e.getMessage();
            logger.error(msg);

            throw new FileWriterException(msg);
        }
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
     *            l'ensemble de résultat d'une requête sous la forme d'un
     *            <code>ArrayListHandler</code>
     * @throws FileWriterException
     */
    public void writeCSVFile(String filename, List<Object[]> values) throws FileWriterException 
    {
        writeCSVFile(filename, values, null);
    }

}
