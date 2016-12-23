/**
 *
 * @author santusbr, rivierech
 *
 */
package org.paris.batch.datafile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.paris.batch.datas.DataStructure;
import org.paris.batch.exception.DataFileException;
import org.paris.batch.exception.DataFileFormatException;
import org.paris.batch.exception.StatAnalyzerException;
import org.paris.batch.stats.StatAnalyzer;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * @author santusbr Classe DataFile : Représente un fichier de données et
 *         posséde les méthodes nécessaires à sa manipulation (lecture/écriture)
 */
public class DataFile {

	private String name = null;
	private File fichierSource = null;
	private File fichierDestination = null;

	private String typeFile = null;

	/** Descriptif de format de données en entrée */
	protected DataFileFormat in = null;
	/** Descriptif de format de données en sortie */
	protected DataFileFormat out = null;

	/** structure de données mise en jeu */
	public DataStructure datas = null;

	protected Logger logger = null;
	
	/** fourniture de quelques statistiques sur les différents traitements de cette classe */
	private StatAnalyzer stats = null;

	
	/**
	 * Constructeur implémentant les éléments obligatoires et invariables pour un objet DataFile.
	 * 
	 * @param typeFile
	 *            Type de fichier CSV ou Colonné (DataFileType)
	 * @param logger
	 *            Logger fourni par la classe appelante (implémentant
	 *            GenericBatch)
	 * @param stats
	 *            StatAnalyzer fourni par la classe appelante (implémentant
	 *            GenericBatch)
	 * @throws DataFileException
	 */
	private DataFile(String typeFile, Logger logger, StatAnalyzer stats) 
						throws DataFileException {
		// contrôle de cohérence des données de construction
		String errMsg = "";

		if (stats == null)
			errMsg += "Le composant StatAnalyzer n'est pas valide";
		
		if (logger == null)
			errMsg += "Le composant Logger n'est pas valide";
		
		if (typeFile == null)
			errMsg += "Le composant (String)typeFile n'est pas valide";

		// TODO : compléter les tests de cohérence des paramètres en entrée avec
		// typeFile
		if (!errMsg.equals(""))
			throw new DataFileException(errMsg);

		try {
			// définition du logger
			this.logger = logger;

			// on créer une DataStructure pour enrigistrer les données à traiter
			this.datas = new DataStructure();
			
			// définition du StatAnalyzer
			this.stats = stats;

			// Définition du type de fichier CSV / Colonné
			this.setTypeFile(typeFile);
		}
		catch (Exception e) {
			String msg = "Erreur lors de l'accés au fichier - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}
	
	
	
	
	
	/**
	 * Constructeur implémentant un objet DataFile destiné à la production d'un fichier sans partir d'un fichier source.
	 * 
	 * @param destinationPath
	 *            chemin de la destination
	 * @param outputFilename
	 *            nom du fichier de données en sortie si vous ne souhaitez pas garder le nom du fichier d'origine
	 * @param typeFile
	 *            Type de fichier CSV ou Colonné (DataFileType)
	 * @param formats
	 *            Properties contenant les formats de DataFile
	 * @param nameDataFileFormatOut
	 *            [optionnel] : Nom du DataFileFormat en sortie si différent du nom du
	 *            fichier
	 * @param logger
	 *            Logger fourni par la classe appelante (implémentant
	 *            GenericBatch)
	 * @param stats
	 *            StatAnalyzer fourni par la classe appelante (implémentant
	 *            GenericBatch)
	 * @throws DataFileException
	 */
	public DataFile(String destinationPath, String outputFilename,
			String typeFile, Properties formats,  String nameDataFileFormatOut,
			Logger logger, StatAnalyzer stats) throws DataFileException {
		
		this(typeFile, logger, stats);
		
		// contrôle de cohérence des données de construction
		String errMsg = "";

		// vérifier que le chemin destination n'est pas vide
		if (destinationPath.equals("") || destinationPath == null) {
			errMsg += "Le chemin d'accès au fichier de destination est vide\n";
		} else {
			// vérifier que le chemin destination existe;
			File destFileDir = new File(destinationPath);
			if (!destFileDir.exists()) {
				errMsg += "Le chemin d'accès au fichier de destination n'existe pas\n";
			} else {
				if (!destFileDir.isDirectory()) {
					errMsg += "Le chemin d'accès au fichier de destination ne correspond pas à un répertoire\n";
				} else {
					if (!destFileDir.canWrite()) {
						errMsg += "Le chemin d'accès au fichier de destination n'est pas un répertoire accessible en écriture\n";
					}
				}
			}
		}
		

		// TODO : compléter les tests de cohérence des paramètres en entrée avec
		// formatIn
		// formats
		if (!errMsg.equals(""))
			throw new DataFileException(errMsg);

		// tout est en ordre ? Alors va pour la construction. Yeeeeepeeeee !
		try {
			// définition du nom de fichier
			this.setName(outputFilename); 
			//TODO, a 1ere vu l'attribut Name sert surtout lors de la constitution des messages d'erreurs, 
			//il y aurait un travail de mise en conformité de ces derniers suite à l'intégration de cette nouvelle fonctionnalité
			
			// définition des formats du fichier de destination
			this.setOut(formats, nameDataFileFormatOut);

			// / Définition du fichier de destination
			this.setFichierDestination(new File(destinationPath
					+ File.separator + outputFilename));
			
		} catch (DataFileException dfe) {
			String msg = "Erreur lors de l'accés au fichier - Fichier concerné: "
					+ this.getName() + "\nDataFileException : " + dfe.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
		catch (Exception e) {
			String msg = "Erreur lors de l'accés au fichier - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}
	
	
	/**
	 * Constructeur implémentant un objet DataFile.
	 * 
	 * @param inputFilename
	 *            nom du fichier de données en entrée
	 * @param sourcePath
	 *            chemin de la source
	 * @param destinationPath
	 *            chemin de la destination
	 * @param outputFilename
	 *            [optionnel] nom du fichier de données en sortie si vous ne souhaitez pas garder le nom du fichier d'origine
	 * @param typeFile
	 *            Type de fichier CSV ou Colonné (DataFileType)
	 * @param formats
	 *            Properties contenant les formats de DataFile
	 * @param nameDataFileFormatIn
	 *            [optionnel] : Nom du DataFileFormat en entrée si différent du nom du
	 *            fichier
	 * @param nameDataFileFormatOut
	 *            [optionnel] : Nom du DataFileFormat en sortie si différent du nom du
	 *            fichier
	 * @param logger
	 *            Logger fourni par la classe appelante (implémentant
	 *            GenericBatch)
	 * @param stats
	 *            StatAnalyzer fourni par la classe appelante (implémentant
	 *            GenericBatch)
	 * @throws DataFileException
	 */
	public DataFile(String inputFilename, String sourcePath, String destinationPath, String outputFilename,
			String typeFile, Properties formats, String nameDataFileFormatIn, String nameDataFileFormatOut,
			Logger logger, StatAnalyzer stats) throws DataFileException {
		
		this(typeFile, logger, stats);
		
		// contrôle de cohérence des données de construction
		String errMsg = "";

		// vérifier que le nom de fichier n'est pas vide
		if (inputFilename.equals("") || inputFilename == null) {
			errMsg += "Le nom de fichier à utiliser en lecture est vide\n";
		}

		// vérifier que le chemin source n'est pas vide
		if (sourcePath.equals("") || sourcePath == null) {
			errMsg += "Le chemin d'accès au fichier d'entrée est vide\n";
		} else {
			// vérifier que le chemin source correspond à un vrai répertoire
			File sourceFileDir = new File(sourcePath);
			if (!sourceFileDir.exists()) {
				errMsg += "Le chemin d'accès au fichier d'entrée n'existe pas";
			} else {
				if (!sourceFileDir.isDirectory()) {
					errMsg += "Le chemin d'accès au fichier d'entrée ne correspond pas à un répertoire\n";
				}
				// vérifier que le chemin source est un répertoire accessible en
				// lecture
				else {
					if (!sourceFileDir.canRead()) {
						errMsg += "Le chemin d'accès au fichier d'entrée n'est pas un répertoire accessible en lecture\n";
					}
				}
			}
		}

		// vérifier que le chemin destination n'est pas vide
		if (destinationPath.equals("") || destinationPath == null) {
			errMsg += "Le chemin d'accès au fichier de destination est vide\n";
		} else {
			// vérifier que le chemin destination existe;
			File destFileDir = new File(destinationPath);
			if (!destFileDir.exists()) {
				errMsg += "Le chemin d'accès au fichier de destination n'existe pas\n";
			} else {
				if (!destFileDir.isDirectory()) {
					errMsg += "Le chemin d'accès au fichier de destination ne correspond pas à un répertoire\n";
				} else {
					if (!destFileDir.canWrite()) {
						errMsg += "Le chemin d'accès au fichier de destination n'est pas un répertoire accessible en écriture\n";
					}
				}
			}
		}
		
		// TODO : compléter les tests de cohérence des paramètres en entrée avec
		// formatIn
		// formats
		if (!errMsg.equals(""))
			throw new DataFileException(errMsg);

		// tout est en ordre ? Alors va pour la construction. Yeeeeepeeeee !
		try {
			// définition du nom de fichier
			this.setName(inputFilename);

			// définition des formats de fichier source et destination
			this.setIn(formats, nameDataFileFormatIn);
			this.setOut(formats, nameDataFileFormatOut);

			// Définition des fichiers de source et de destination
			this.setFichierSource(new File(sourcePath + File.separator
					+ inputFilename));

			// Si le nom de fichier destination n'est pas renseigné, le nom de fichier sera celui de l'origine
			if (outputFilename != null && outputFilename != "") {
				this.setFichierDestination(new File(destinationPath
						+ File.separator + outputFilename));
			} else {
				this.setFichierDestination(new File(destinationPath
						+ File.separator + inputFilename));
			}
			
		} catch (DataFileException dfe) {
			String msg = "Erreur lors de l'accés au fichier - Fichier concerné: "
					+ this.getName() + "\nDataFileException : " + dfe.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
		catch (Exception e) {
			String msg = "Erreur lors de l'accés au fichier - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}

	/**
	 * Méthode de chargement des données du fichier en mémoire //TODO
	 * 
	 * @param FileOrigine
	 *            : Nom du fichier à charger
	 * @throws DataFileException
	 * @throws IOException
	 */
	public void setData(DataStructure datas) 
			throws DataFileException {
		
		// contrôle de cohérence des données de construction
		String errMsg = "";
				
		if ((datas == null) || !(datas.getDatas().size()>0))
			errMsg += "Le composant DataStructure n'est pas valide";
		
		this.datas = datas;
		
		if (!errMsg.equals(""))
			throw new DataFileException("Erreur lors du chargement des données: " + errMsg);
		
	}
	

	
	
	/**
	 * Méthode de chargement des données du fichier en mémoire
	 * 
	 * @param FileOrigine
	 *            : Nom du fichier à charger
	 * @throws DataFileException
	 * @throws IOException
	 */
	public void loadData(String FileOrigine) throws DataFileException,
			IOException, StatAnalyzerException {

		// On vérifie si le nom de fichier à traiter a été renseigné
		if (FileOrigine != null && FileOrigine != "") {

			// en fonction du type de fichier, on délégue le chargement des données à la méthode appropriée
			if (typeFile.equals(DataFileType.TYPE_COLONNE))
			{
				try {
					this.loadColonneData(FileOrigine);
				}
				catch (DataFileException dfe) 
				{
					String msg = "DataFileException : "
							+ dfe.getMessage();
					System.err.println(msg);
					throw new DataFileException(msg);
				}
			}
			else if (typeFile.equals(DataFileType.TYPE_CSV))
			{
				try {
					this.loadCSVData(FileOrigine);
				}
				catch (DataFileException dfe) 
				{
					String msg = "DataFileException : "
							+ dfe.getMessage();
					System.err.println(msg);
					throw new DataFileException(msg);
				}
			}	
				
			else {
				String msg = "Erreur lors du chargement en mémoire du fichier - Fichier concerné: "
						+ this.getName()
						+ "\nException : "
						+ "Le type de fichier source n'est pas reconnu";
				System.err.println(msg);
				throw new DataFileException(msg);
			}
		} else {
			String msg = "Erreur lors du chargement en mémoire du fichier - Fichier concerné: "
					+ this.getName()
					+ "\nException : "
					+ "Le nom du fichier à charger est inconnu";
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}

	/**
	 * Méthode de chargement des données du fichier colonné en mémoire
	 * 
	 * @param FileOrigine
	 *            : Nom du fichier à charger
	 * @throws DataFileException
	 */
	private void loadColonneData(String FileOrigine) throws DataFileException {
		FileInputStream fstream;

		if (this.getFichierSource() != null && this.getIn() != null) {

			try {
				if (FileOrigine.equals(DataFileType.ORIGINE_SOURCE)) {
					fstream = new FileInputStream(this.fichierSource);
				} else if (FileOrigine.equals(DataFileType.ORIGINE_DESTINATION)) {
					fstream = new FileInputStream(this.fichierDestination);
				} else {
					fstream = null;
				}
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine;

				// On parcours le fichier
				while ((strLine = br.readLine()) != null) {
					String strLineOut = "";
					
					// On mets à jour la stat concernant le nombre de lignes traitées
					this.stats.update("lignes_traitées");

					// On stock les données en fonction du format
					HashMap<String, Object> Data = new HashMap<String, Object>();

					// On crée un iterateur pour from
					Set<String> set = this.getIn().getFormat().keySet();
					Iterator<String> itr = set.iterator();
					String formKey = null;
					String formValue = null;
					int fromoffset = 0;
					int fromlength = 0;

					// Pour chaque donnée dans from
					while (itr.hasNext()) {
						// On récupére la clé
						formKey = itr.next();

						// On récupére offset et length
						formValue = this.getIn().getFormat().get(formKey);
						fromoffset = Integer.parseInt(formValue.split(",")[0]) - 1;
						fromlength = Integer.parseInt(formValue.split(",")[1]);

						// On charge les infos dans Datas (key,value)
						Data.put(
								formKey,
								strLine.substring(fromoffset, fromoffset
										+ fromlength));
					}
					this.datas.add(Data);
					// On met à jour la stat pour le nombre de lignes importées
					this.stats.update("lignes_importées");
				}
				fstream.close();
			} catch (Exception e) {
				String msg = "Erreur lors du chargement en mémoire du fichier - Fichier concerné: "
						+ this.getName() + "\nException : " + e.getMessage();
				System.err.println(msg);
				throw new DataFileException(msg);
			}
		} else {
			String msg = "Erreur lors du chargement en mémoire du fichier colonné - Fichier concerné: "
					+ this.getName()
					+ "\nException : "
					+ "Il n'y a pas de fichier source défini ou de format d'entrée";
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}

	/**
	 * Méthode de chargement des données du fichier CSV en mémoire
	 * 
	 * @param FileOrigine
	 *            : Nom du fichier à charger
	 * @throws DataFileException
	 * @throws IOException
	 */
	private void loadCSVData(String FileOrigine) throws DataFileException,
			IOException, StatAnalyzerException {
		
		FileInputStream fstream = null;
		DataFileFormat fileFormat = null;
		String filename = null;
			try {
				// On récupére le fichier à charger en mémoire
				logger.debug("On récupére le fichier à charger en mémoire");
				if (FileOrigine.equals(DataFileType.ORIGINE_SOURCE)) {
					fstream = new FileInputStream(this.fichierSource);
					fileFormat = this.getIn();
					filename = this.fichierSource.getName();
				} else if (FileOrigine.equals(DataFileType.ORIGINE_DESTINATION)) {
					fstream = new FileInputStream(this.fichierDestination);
					fileFormat = this.getOut();
					filename = this.fichierDestination.getName();
				} else {
					String msg = "Erreur lors du chargement en mémoire du fichier csv - Fichier concerné: "
							+ this.getName() + "\nDataFileException : Echec de la récupération en mémoire du fichier, le répertoire de lecture n\'a pas été indiqué correctement." 
							+ "\nChoisir: " + DataFileType.ORIGINE_SOURCE + " ou " + DataFileType.ORIGINE_DESTINATION + " lors de l'appel de loadData().";
					throw new DataFileException(msg);	
				}
				
				// On configure les paramètres du fichier à charger
				logger.debug("On configure les paramètres du fichier à charger");
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
						

				
				CSVReader reader = new CSVReader(br, fileFormat.getSeparator().toCharArray()[0],
						CSVParser.DEFAULT_QUOTE_CHARACTER, (fileFormat.getHaveheader() ? 1 : 0));

				// On parcours le fichier ligne par ligne
				logger.debug("On parcours le fichier ligne par ligne");
				String[] nextLine;
				int ligne = (fileFormat.getHaveheader() ? 2 : 1);
				while ((nextLine = reader.readNext()) != null) {

					// On mets à jour la stat concernant le nombre de lignes traitées
					logger.debug("On mets à jour la stat concernant le nombre de lignes traitées");
					this.stats.update("lignes_traitées");
					
					// On récupére la liste des colonnes à traiter
					logger.debug("On récupére la liste des colonnes à traiter");
					Set<String> set = this.getIn().getFormat().keySet();
					Iterator<String> itr = set.iterator();

					// On vérifie si la ligne contient bien toute les colonnes
					// même vide sinon on rejete la ligne du chargement et on
					// loggue tout ça
					logger.debug("On vérifie si la ligne contient bien toute les colonnes");
					if (nextLine.length != set.size()) {
						logger.error("ligne rejetée (nombre de colonnes incorrect) : [attendue=" + set.size()
								+ "; reçue=" + nextLine.length
								+ "; donnée=" + Arrays.toString(nextLine)
								+ " dans " + filename + ":" 
								+ ligne + "]");
						// On met à jour la stat pour le nombre de lignes rejeté
						this.stats.update("lignes_rejetées");
					} else {
						
						// Pour chaque colonne
						HashMap<String, Object> tempData = new HashMap<String, Object>();
						String formKey = null;
						int colonne = 0;
						while (itr.hasNext()) {

							// On récupére la clé : nom de la colonne
							formKey = itr.next();

							// On charge les données des colonnes dans une hashmap 
							// temporaire le temps de récuperer la totalité des colonnes 
							tempData.put(formKey, nextLine[colonne]);
							colonne++;
						}
						// Après avoir récuperer l'ensemble des données pour chaque colonne
						// on peut charger la ligne dans la mémoire interne de données
						this.datas.add(tempData);
						
						// On met à jour la stat pour le nombre de lignes importées
						this.stats.update("lignes_importées");
					}
					ligne ++;
				}
				// Après avoir lu tout le fichier, on peut fermer les module de lecture
				reader.close();
				fstream.close();
			} catch (NullPointerException e) {
				String msg = "Erreur lors du chargement en mémoire du fichier csv - Fichier concerné: "
						+ this.getName() + "\nException : " + e.getMessage();
				System.err.println(msg);
				throw new DataFileException(msg);
			}
	}

	/**
	 * Methode : copie le fichier source dans le répertoire destination
	 * 
	 * @throws DataFileException
	 */
	public void sourceToDestination() throws DataFileException {

		// System.out.println("Copie du fichier : "+
		// this.fichierSource.getName());

		FileInputStream fstream;
		try {
			// On ouvre le fichier source
			fstream = new FileInputStream(this.fichierSource);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// On ouvre le fichier destination
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
					this.fichierDestination));

			String strLine;
			// On parcours le fichier
			while ((strLine = br.readLine()) != null) {
				bufferedWriter
						.write((new StringBuilder(String.valueOf(strLine)))
								.append("\r\n").toString());
			}

			// On ferme le fichier source
			br.close();
			// On ferme le fichier de destination
			bufferedWriter.close();

		} catch (IOException e) {
			String msg = "Erreur lors de la copie du fichier source vers sa destination - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}

	}

	/**
	 * Methode : formatFile, permet de formater le fichier de données en
	 * fonction du format fournit
	 * 
	 * @throws DataFileException
	 */
	public void formatFile(DataFileFormat from, DataFileFormat to)
			throws DataFileException {
		FileInputStream fstream;
		// Si le fichier destination n'existe pas encore on le copie à partir de
		// la source
		if (!this.fichierDestination.exists()) {
			this.sourceToDestination();

		}
		// On créé un fichier temporaire dans lequel on va écrire les données
		// formatée

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					fichierDestination.getPath() + "_temp"));
			// On lit et format les données
			fstream = new FileInputStream(this.fichierDestination);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			int compteurligne = 0;
			// On parcours le fichier
			while ((strLine = br.readLine()) != null) {
				String strLineOut = "";

				// On stock les données en fonction du format
				Hashtable<String, String> Data = new Hashtable<String, String>();

				// On crée un iterateur pour from
				Set<String> set = from.getFormat().keySet();
				Iterator<String> itr = set.iterator();
				String formKey = null;
				String formValue = null;
				int fromoffset = 0;
				int fromlength = 0;

				// Pour chaque donnée dans from
				while (itr.hasNext()) {
					// On récupére la clé
					formKey = itr.next();

					// On récupére offset et length
					formValue = from.getFormat().get(formKey);
					fromoffset = Integer.parseInt(formValue.split(",")[0]) - 1;
					fromlength = Integer.parseInt(formValue.split(",")[1]);

					// On charge les infos dans Datas (key,value)
					Data.put(
							formKey,
							strLine.substring(fromoffset, fromoffset
									+ fromlength));
				}
				// System.out.println(Data.toString());

				// On crée un iterateur pour to
				set = to.getFormat().keySet();
				itr = set.iterator();
				String toKey = null;
				String toValue = null;
				int tooffset = 0;
				int tolength = 0;
				int finaloffset = 0;

				// On récupére le séparateur, le charactére de remplacement, la
				// taille de l'enregistrement et le charactére de fin de ligne
				String replacechar = "";
				if (!to.getReplacechar().trim().isEmpty())
					replacechar = to.getReplacechar();
				String separator = "";
				if (!to.getSeparator().trim().isEmpty())
					separator = to.getSeparator();
				int recordlength = 0;
				if (to.getRecordlength() > 0)
					recordlength = to.getRecordlength();
				String endrecordchar = "";
				if (!to.getEndrecordchar().trim().isEmpty())
					endrecordchar = to.getEndrecordchar();

				while (itr.hasNext()) {
					// On récupére la premiére donnée du format
					toKey = itr.next();

					// On récupére la taille total de la chaine
					finaloffset = strLineOut.length();
					// On récupére la donnée, son offset et sa length
					toValue = to.getFormat().get(toKey);
					tooffset = Integer.parseInt(toValue.split(",")[0]) - 1;
					tolength = Integer.parseInt(toValue.split(",")[1]);

					// Séparateur de champ

					// Complément de ligne
					if (!replacechar.isEmpty()) {
						while (finaloffset < tooffset) {
							strLineOut = strLineOut + replacechar;
							finaloffset++;
						}
					}

					// On édite la donnée pour qu'elle entre dans la length de
					// to
					String data = Data.get(toKey);
					if (data.length() > tolength)
						data = data.substring(0, tolength);
					// On ajoute la donnée à chaine finale
					strLineOut = strLineOut + data;
				}

				finaloffset = strLineOut.length();
				// On comble la fin de ligne
				if (recordlength > 0) {
					if (!endrecordchar.trim().isEmpty())
						recordlength = recordlength - endrecordchar.length();
					while (finaloffset < recordlength) {
						strLineOut = strLineOut + replacechar;
						finaloffset++;
					}
				}

				// Fin de ligne
				if (!to.getEndrecordchar().trim().isEmpty())
					strLineOut = strLineOut + to.getEndrecordchar();
				if (to.getBacktoline())
					strLineOut = strLineOut + "\n";
				bw.write(strLineOut);
				compteurligne++;
			}

			this.logger.info("DataFile - Formatage du fichier " + this.name
					+ " | " + compteurligne
					+ " lignes du fichier traitées dans "
					+ this.fichierDestination.getPath() + "/"
					+ this.fichierDestination.getName());

			// On ferme le fichier source
			br.close();
			// On ferme le fichier de destination
			bw.close();

			// On supprime l'ancien fichier destination
			File destination = new File(fichierDestination.getPath());
			destination.delete();
			File destinationtemp = new File(fichierDestination.getPath()
					+ "_temp");
			destinationtemp.renameTo(new File(fichierDestination.getPath()));

			// On renome le fichier temporaire
		} catch (IOException e) {
			String msg = "Erreur lors du formatage du fichier - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			throw new DataFileException(msg);
		}
	}

	/**
	 * Méthode permettant de vider les données stockées en mémoire interne
	 * 
	 */
	public void clearDatas() {
		this.datas = new DataStructure();
	}
	
	
	public void writeData(String format) throws DataFileException {
		FileWriter fileWriter = null;
		DataFileFormat fileFormat = null;
		String fileName = "";
		try {
			
			// On définit le repertoire de sortie
			if (format.equals(DataFileType.ORIGINE_SOURCE)) {
				fileWriter = new FileWriter(this.fichierSource, true);
				fileName = this.fichierSource.getAbsolutePath();
				fileFormat = this.getIn();
			} else if (format.equals(DataFileType.ORIGINE_DESTINATION)) {
				fileWriter = new FileWriter(this.fichierDestination, true);
				fileFormat = this.getOut();
				fileName = this.fichierDestination.getAbsolutePath();
			}
			
			// en fonction du type de fichier, on délégue le chargement des données à la méthode appropriée
			if (typeFile.equals(DataFileType.TYPE_COLONNE))
				this.writeColonneDataFile(fileName, fileFormat, fileWriter);
			else if (typeFile.equals(DataFileType.TYPE_CSV))
				this.writeCSVDataFile(fileName, fileFormat, fileWriter);
			else {
				String msg = "Erreur lors du chargement en mémoire du fichier - Fichier concerné: "
						+ this.getName()
						+ "\nException : "
						+ "Le type de fichier source n'est pas reconnu";
				System.err.println(msg);
				throw new DataFileException(msg);
			}
		} catch (Exception e){
			String msg = "Erreur lors d'écriture des données en mémoire dans le fichier - Fichier concerné: "
					+ fileName + "\nException : " + e.getMessage();
			System.err.println(msg);
			logger.error(msg);
			throw new DataFileException(msg);
		}
	}
	
	/**
	 * Méthode permettant d'écrire un fichier de données colonné dans le répertoire
	 * destination ou source
	 * 
	 * @param format repertoire de sortie
	 * @throws DataFileException 
	 */
	private void writeColonneDataFile(String fileName, DataFileFormat fileFormat, FileWriter fileWriter) throws DataFileException {
		// TODO
	}

	/**
	 * Méthode permettant d'écrire un fichier de données CSV dans le répertoire
	 * destination ou source
	 * 
	 * @param format repertoire de sortie
	 * @throws DataFileException 
	 */
	private void writeCSVDataFile(String fileName, DataFileFormat fileFormat, FileWriter fileWriter) throws DataFileException, StatAnalyzerException {
		CSVWriter writer = null;
		String etapeExecution = "";
		try {
			// On configure les paramètres du fichier de sortie
			etapeExecution = "[configuration params fichier sortie]";
			writer = new CSVWriter(fileWriter,fileFormat.getSeparator().toCharArray()[0], CSVWriter.NO_QUOTE_CHARACTER);

			// On écrit l'entête si le paramètre HaveHeader est vrai
			etapeExecution = "[écriture du header]";
			if (fileFormat.getHaveheader()){
				writer.writeNext(fileFormat.getFormat().keySet().toArray(new String[fileFormat.getFormat().keySet().size()]));
			}
			
			// Pour chaque donnée
			etapeExecution = "[parcour des données]";
			for (HashMap<String, Object> hm : this.datas.getDatas()) {

				// On récupére la liste des colonnes à écrire dans le fichier
				Set<String> set = fileFormat.getFormat().keySet();
				if (set == null)
					logger.error("le file format est nulle");
				Iterator<String> itr = set.iterator();

				// Une tableau temporaire permettant de stocker les données des colonnes
				// le temps que toutes les colonnes de la ligne soit traitées
				ArrayList<String> tempData = new ArrayList<String>();

				// Pour chaque colonne
				while (itr.hasNext()) {
					// On récupére la donnée correspondant à la colonne
					// Si non disponible : ce sera un String vide ""
					tempData.add((String) hm.get(itr.next()));
				}
				
				// Après avoir récupérées toutes les données de toutes les colonnes, on
				// écrit la ligne de données dans le fichier
				String[] record = tempData.toArray(new String[tempData.size()]);
				writer.writeNext(record);
				
				// On met à jour la stat pour le nombre de lignes écrites
				this.stats.update("lignes_écrites");
			}
			// Après traitement, on peut fermer le module écriture
			etapeExecution = "[fermeture module d'écriture]";
			writer.close();
		} catch (IOException ioe) {
			String msg = "Erreur lors d'écriture des données en mémoire dans le fichier csv - Fichier concerné: "
					+ fileName + "\nIOException : " + ioe.getMessage();
			System.err.println(msg);
			logger.error(msg);
			throw new DataFileException(msg);
		} catch (Exception e) {
			String msg = "Erreur lors d'écriture des données en mémoire dans le fichier csv - Etape: " + etapeExecution + " - Fichier concerné: "
					+ fileName + "\nException : " + e.getMessage();
			System.err.println(msg);
			logger.error(msg);
			throw new DataFileException(msg);
		}
	}

	/********************** GETTERS AND SETTERS ***************************/
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

	public void setIn(Properties formats, String dataFileFormat)
			throws DataFileException {
		try {
			String nameDataFileFormat = this.name;
			
			// Si un nom de DataFileFormat a été renseigné, on le récupère 
			// sinon on prends le nom du fichier à traiter par défault
			if (dataFileFormat != null && dataFileFormat != "")
				nameDataFileFormat = dataFileFormat;
			
			this.in = new DataFileFormat(nameDataFileFormat,
					DataFileType.FORMAT_IN, formats);
		} catch (DataFileFormatException e) {
			String msg = "Erreur lors de la définition du format d'entrée - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			logger.error(msg);
			throw new DataFileException(msg);
		}
		;
	}

	public DataFileFormat getOut() {
		return out;
	}

	public void setOut(Properties formats, String dataFileFormat)
			throws DataFileException {
		try {
			String nameDataFileFormat = this.name;
			
			// Si un nom de DataFileFormat a été renseigné, on le récupère 
			// sinon on prends le nom du fichier à traiter par défault
			if (dataFileFormat != null && dataFileFormat != "")
				nameDataFileFormat = dataFileFormat;
			
			this.out = new DataFileFormat(nameDataFileFormat,
					DataFileType.FORMAT_OUT, formats);
		} catch (DataFileFormatException e) {
			String msg = "Erreur lors de la définition du format de sortie - Fichier concerné: "
					+ this.getName() + "\nException : " + e.getMessage();
			System.err.println(msg);
			logger.error(msg);
			throw new DataFileException(msg);
		}
		;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTypeFile() {
		return typeFile;
	}

	public void setTypeFile(String typeFormat) {
		this.typeFile = typeFormat;
	}
}
