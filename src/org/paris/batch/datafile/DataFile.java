/**
 *
 * @author santusbr
 *
 */
package org.paris.batch.datafile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.paris.batch.datas.Datas;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DataFileException;
import org.paris.batch.exception.DataFileFormatException;

/**
 * @author santusbr
 * Classe DataFile : Repr�sente un fichier de donn�es et poss�de les m�thodes n�cessaires � sa manipulation
 */

public class DataFile {

	private String name = null;
	private File fichierSource = null;
	private File fichierDestination = null;

	private String typeFormat = null;

	// Formats de donn�es in et out
	protected DataFileFormat in = null;
	protected DataFileFormat out = null;

	protected Datas datas = null;


	/**
	 * Constructeur
	 * @param filename : nom du fichier de donn�es
	 * @param sourcePath : chemin de la source
	 * @param destinationPath : chemin de la destination
	 */
	public DataFile(String filename, String sourcePath, String destinationPath, String formatIn, String typeFormat, Properties formats) throws DataFileException{

		try{
			// Initialisation du DataFile
			this.setName(filename);
			// D�finition des fichiers de source et de destination
			this.setFichierSource(new File(sourcePath+"/"+filename));

			if(destinationPath != null){
				this.setFichierDestination(new File(destinationPath+"/"+filename));
			}
			// D�finition du type de fichier CSV / Colonn�
			this.setTypeFormat(typeFormat);
		}catch(Exception e){
			String msg = "Erreur lors de l'acc�s au fichier - Fichier concern�: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}
	/**
	 * M�thode de chargement des donn�es du fichier en m�moire
	 */
	public void loadData(String FileOrigine) throws DataFileException{
		FileInputStream fstream;

		if(this.getFichierSource() != null && this.getIn() != null){

			try {
				if(FileOrigine.equals(DataFileType.ORIGINE_SOURCE)){
					fstream = new FileInputStream(this.fichierSource);
				}else if (FileOrigine.equals(DataFileType.ORIGINE_DESTINATION)) {
					fstream = new FileInputStream(this.fichierDestination);
				}else{
					fstream = null;
				}
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String strLine;

				// On parcours le fichier
				while((strLine = br.readLine()) != null){
					String strLineOut = "";

					// On stock les donn�es en fonction du format
					HashMap<String, Object> Data = new HashMap<String, Object>();

					// On cr�e un iterateur pour from
					Set<String> set = this.getIn().getFormat().keySet();
					Iterator<String> itr = set.iterator();
					String formKey = null;
					String formValue = null;
					int fromoffset = 0;
					int fromlength = 0;

					// Pour chaque donn�e dans from
					while(itr.hasNext()){
						// On r�cup�re la cl�
						formKey = itr.next();

						// On r�cup�re offset et length
						formValue = this.getIn().getFormat().get(formKey);
						fromoffset = Integer.parseInt(formValue.split(",")[0])-1;
						fromlength = Integer.parseInt(formValue.split(",")[1]);

						// On charge les infos dans Datas (key,value)
						Data.put(formKey, strLine.substring(fromoffset, fromoffset+fromlength));
					}
					this.datas.add(Data);
				}
				fstream.close();
			} catch (Exception e) {
				String msg = "Erreur lors du chargement en m�moire du fichier - Fichier concern�: "
						+ this.getName() + "\nException : " + e.getMessage();
				System.err.println(msg);
				throw new DataFileException(msg);
			}
		}else{
			String msg = "Erreur lors du chargement en m�moire du fichier - Fichier concern�: "
					+ this.getName() + "\nException : " + "Il n'y a pas de fichier source d�fini ou de format d'entr�e";
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}

	/**
	 * Methode : copie le fichier source dans le r�pertoire destination
	 * @throws DataFileException 
	 */
	public void sourceToDestination() throws DataFileException{

		System.out.println("Copie du fichier : "+ this.fichierSource.getName());

		FileInputStream fstream;
		try {
			// On ouvre le fichier source
			fstream = new FileInputStream(this.fichierSource);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// On ouvre le fichier destination
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.fichierDestination));


			String strLine;
			// On parcours le fichier
			while((strLine = br.readLine()) != null){
				bufferedWriter.write((new StringBuilder(String.valueOf(strLine))).append("\r\n").toString());
			}

			// On ferme le fichier source
			br.close();
			// On ferme le fichier de destination
			bufferedWriter.close();

		} catch (IOException e) {
			String msg = "Erreur lors de la copie du fichier source vers sa destination - Fichier concern�: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}

	}
	/**
	 * Methode : formatFile, permet de formater le fichier de donn�es en fonction du format fournit
	 * @throws DataFileException 
	 */
	public void formatFile(DataFileFormat from, DataFileFormat to) throws DataFileException {
		FileInputStream fstream;
		// Si le fichier destination n'existe pas encore on le copie � partir de la source
		if(!this.fichierDestination.exists()){
			this.sourceToDestination();

		}
		// On cr�� un fichier temporaire dans lequel on va �crire les donn�es format�e

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fichierDestination.getPath()+"_temp"));
			// On lit et format les donn�es
			fstream = new FileInputStream(this.fichierDestination);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			// On parcours le fichier
			while((strLine = br.readLine()) != null){
				String strLineOut = "";

				// On stock les donn�es en fonction du format
				Hashtable<String, String> Data = new Hashtable<String, String>();

				// On cr�e un iterateur pour from
				Set<String> set = from.getFormat().keySet();
				Iterator<String> itr = set.iterator();
				String formKey = null;
				String formValue = null;
				int fromoffset = 0;
				int fromlength = 0;

				// Pour chaque donn�e dans from
				while(itr.hasNext()){
					// On r�cup�re la cl�
					formKey = itr.next();

					// On r�cup�re offset et length
					formValue = from.getFormat().get(formKey);
					fromoffset = Integer.parseInt(formValue.split(",")[0])-1;
					fromlength = Integer.parseInt(formValue.split(",")[1]);

					// On charge les infos dans Datas (key,value)
					Data.put(formKey, strLine.substring(fromoffset, fromoffset+fromlength));
				}
				System.out.println(Data.toString());

				// On cr�e un iterateur pour to
				set = to.getFormat().keySet();
				itr = set.iterator();
				String toKey = null;
				String toValue = null;
				int tooffset = 0;
				int tolength = 0;
				int finaloffset = 0;

				// On r�cup�re le s�parateur, le charact�re de remplacement, la taille de l'enregistrement et le charact�re de fin de ligne
				String replacechar = "";
				if(!to.getReplacechar().trim().isEmpty())
					replacechar = to.getReplacechar();
				String separator = "";
				if(!to.getSeparator().trim().isEmpty())
					separator = to.getSeparator();
				int recordlength = 0;
				if(to.getRecordlength() > 0)
					recordlength = to.getRecordlength();
				String endrecordchar = "";
				if(!to.getEndrecordchar().trim().isEmpty())
					endrecordchar = to.getEndrecordchar();


				while(itr.hasNext()){
					// On r�cup�re la premi�re donn�e du format
					toKey = itr.next();

					// On r�cup�re la taille total de la chaine
					finaloffset = strLineOut.length();
					// On r�cup�re la donn�e, son offset et sa length
					toValue = to.getFormat().get(toKey);
					tooffset = Integer.parseInt(toValue.split(",")[0])-1;
					tolength = Integer.parseInt(toValue.split(",")[1]);


					// S�parateur de champ

					// Compl�ment de ligne
					if(!replacechar.isEmpty()){
						while(finaloffset < tooffset){
							strLineOut = strLineOut+replacechar;
							finaloffset ++;
						}
					}

					// On �dite la donn�e pour qu'elle entre dans la length de to
					String data = Data.get(toKey);
					if(data.length() > tolength)
						data = data.substring(0, tolength);
					// On ajoute la donn�e � chaine finale 
					strLineOut = strLineOut+data;
				}

				finaloffset = strLineOut.length();
				// On comble la fin de ligne
				if(recordlength > 0){
					if(!endrecordchar.trim().isEmpty())
						recordlength = recordlength - endrecordchar.length();
					while(finaloffset < recordlength){
						strLineOut = strLineOut+replacechar;
						finaloffset ++;
					}
				}

				// Fin de ligne
				if(!to.getEndrecordchar().trim().isEmpty())
					strLineOut = strLineOut+to.getEndrecordchar();
				if(to.getBacktoline())
					strLineOut = strLineOut+"\n";
				bw.write(strLineOut);		
			}

			// On ferme le fichier source
			br.close();
			// On ferme le fichier de destination
			bw.close();

			// On supprime l'ancien fichier destination
			File destination = new File(fichierDestination.getPath());
			destination.delete();
			File destinationtemp = new File(fichierDestination.getPath()+"_temp");
			destinationtemp.renameTo(new File(fichierDestination.getPath()));


			// On renome le fichier temporaire
		} catch (IOException e) {
			String msg = "Erreur lors du formatage du fichier - Fichier concern�: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}

	/**
	 * M�thode permettant d'�crire un fichier de donn�es dans le r�pertoire destination en fonction d'un format (in ou out)
	 * @param format
	 */
	public void writeDataFile(DataFileType format){
		// TODO r�diger m�thode
	}
	/********************** GETTERS AND SETTERS***************************/
	public File getFichierSource() {
		return fichierSource;
	}

	public void setFichierSource(File fichierSource) {
		this.fichierSource = fichierSource;
	}

	public File getFichierDestination() {
		return fichierDestination;
	}

	public void setFichierDestination(File fichierDestination) {
		this.fichierDestination = fichierDestination;
	}

	public DataFileFormat getIn() {
		return in;
	}

	public void setIn(Properties formats) throws DataFileException {
		try{
			this.in = new DataFileFormat(this.name, DataFileType.FORMAT_IN, formats);
		} catch (DataFileFormatException e) {
			String msg = "Erreur lors de la d�finition du format d'entr�e - Fichier concern�: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		};
	}

	public DataFileFormat getOut() {
		return out;
	}

	public void setOut(Properties formats) throws DataFileException {
		try {
			this.out = new DataFileFormat(this.name, DataFileType.FORMAT_OUT, formats);
		} catch (DataFileFormatException e) {
			String msg = "Erreur lors de la d�finition du format de sortie - Fichier concern�: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		};
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getTypeFormat() {
		return typeFormat;
	}

	public void setTypeFormat(String typeFormat) {
		this.typeFormat = typeFormat;
	}

}
