package org.paris.batch.datafile;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.paris.batch.exception.DataFileException;
import org.paris.batch.exception.DataFileFormatException;

/**
 * @author santusbr Classe représentant un format de données
 */
public class DataFileFormat {

	private Map<String, String> format = null;
	private String type = null;
	private String endrecordchar = "";
	private String replacechar = "";
	private String separator = "";
	private Boolean backtoline;
	private int recordlength = 0;
	private Boolean haveheader; // for CSV

	/**
	 * Constructeur
	 * 
	 * @param DataFileName
	 *            : Nom du datafile
	 * @param type
	 *            : Type de format (CSV ou Colonne)
	 * @param formats
	 *            : Properties contenant les formats de données
	 * @throws DataFileException
	 */
	public DataFileFormat(String DataFileName, String type, Properties formats)
			throws DataFileFormatException {
		// On stock le type de format et on va lire les properties
		// correspondantes
		try {
			this.type = type;

			Map<String, String> temptable = new LinkedHashMap<String, String>();

			// On parcours les properties de format.properties
			this.format = new LinkedHashMap<String, String>();

			Enumeration enuKeys = formats.keys();
			while (enuKeys.hasMoreElements()) {
				String key = enuKeys.nextElement().toString();
				String value = formats.getProperty(key);

				// Si la properties est inconnus, on stock la properties dans le
				// Hashtable temporaire
				if (key.contains(type + "." + DataFileName)) {
					// On récupére le charactére de complément, de fin
					// d'enregistrement et le séparateur
					if (key.contains("separator")) {
						this.separator = value;
					} else if (key.contains("replacechar")) {
						this.replacechar = value;
					} else if (key.contains("endrecordchar")) {
						this.endrecordchar = value;
					} else if (key.contains("backtoline")) {
						this.backtoline = Boolean.valueOf(value);
					} else if (key.contains("recordlength")) {
						this.recordlength = Integer.parseInt(value);
					} else if (key.contains("haveheader")) {
						this.haveheader = Boolean.valueOf(value);
					} else { // Si properties inconnus, alors on suppose que properties est une colonne
						
						// on extrait le nom de la colonne : c'est le suffixe du dernier point
						key = key.substring(key.lastIndexOf(".")+1);
						
						// on stock la properties
						temptable.put(key, value);
					}
				}
			}

			// On trie le format des colonnes en fonction des offsets seulement
			// si le fichier est de type colonné sinon on le trie par "value"
			if (this.type.equals(DataFileType.TYPE_COLONNE)) {
				
				// On trie le temptable dans le Treemap format en fonction des offsets de values
				Set<String> set = temptable.keySet();
				Iterator<String> itr = set.iterator();
				while (itr.hasNext()) {
					this.format.put(this.findFirst(temptable),
							temptable.get(this.findFirst(temptable)));
					temptable.remove(this.findFirst(temptable));
					set = temptable.keySet();
					itr = set.iterator();
				}
			} else {
				// On trie en fonction des valeurs
				this.format = sortByValue(temptable);
				}
		} catch (Exception e) {
			String msg = "Erreur lors de la création du Format de données - Format concerné: "
					+ this.type
					+ "\nException : "
					+ "Il n'y a pas de fichier source défini ou de format d'entrée";
			System.err.println(msg);
			throw new DataFileFormatException(msg);
		}
	}

	/**
	 * Méthode findFirst : Renvoi la clé ayant le plus petit offset du Hastable
	 * 
	 * @param ht
	 *            : Hastable
	 * @return key
	 * 
	 */
	private String findFirst(Map<String, String> ht) {
		String key = null;

		Set<String> set = ht.keySet();
		Iterator<String> itr = set.iterator();

		key = itr.next();

		String nextkey = null;
		int o1 = 0;
		int o2 = 0;

		while (itr.hasNext()) {
			o1 = Integer.parseInt(ht.get(key).split(",")[0]);
			nextkey = itr.next();
			o2 = Integer.parseInt(ht.get(nextkey).split(",")[0]);

			if (o2 < o1)
				key = nextkey;

		}

		return key;
	}
	
	/**
	 * Méthode sortByValue : Trie une map par value
	 * 
	 * @param map à trier
	 * @return map triée par value
	 * 
	 */
	public static Map<String, String> sortByValue(Map<String, String> map) {
		
		List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			public int compare(Map.Entry<String, String> o1,
					Map.Entry<String, String> o2) {
				Integer v1 = Integer.valueOf(o1.getValue());
				Integer v2 = Integer.valueOf(o2.getValue());
				return (v1.compareTo(v2));
			}
		});

		Map<String, String> result = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Méthode findNext : Renvoi la clé ayant le plus petit offset suivant la
	 * clé donnée
	 * 
	 * @param previewkey
	 *            : clé pour laquelle on veut trouver la suivante
	 * @return key
	 */
	public String findNext(String previewkey) {
		String key = null;

		Set<String> set = this.format.keySet();
		Iterator<String> itr = set.iterator();

		key = itr.next();

		int op = Integer.parseInt(this.format.get(previewkey).split(",")[0]);
		int o2;

		while (itr.hasNext()) {
			key = itr.next();
			// o2 = Integer.parseInt(this.format.get(nextkey).split(",")[0]);

			// if(o2<o1)
			// key = nextkey;

		}
		return key;
	}

	/**
	 * Méthode toString()
	 * 
	 * @return String : Format des données sous forme de chaine de caractère
	 */
	public String toString() {
		return "Format : " + this.format.toString();

	}

	/***************************** GETTERS AND SETTERS ***********************************/
	public int getRecordlength() {
		return recordlength;
	}

	public void setRecordlength(int recordlength) {
		this.recordlength = recordlength;
	}

	public Boolean getBacktoline() {
		return backtoline;
	}

	public Boolean getHaveheader() {
		return haveheader;
	}

	public void setBacktoline(Boolean backtoline) {
		this.backtoline = backtoline;
	}

	public void setHaveheader(Boolean haveheader) {
		this.haveheader = haveheader;
	}

	public String getEndrecordchar() {
		return endrecordchar;
	}

	public void setEndrecordchar(String endrecordchar) {
		this.endrecordchar = endrecordchar;
	}

	public String getReplacechar() {
		return replacechar;
	}

	public void setReplacechar(String replacechar) {
		this.replacechar = replacechar;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getFormat() {
		return format;
	}

	public void setFormat(LinkedHashMap<String, String> format) {
		this.format = format;
	}
}
