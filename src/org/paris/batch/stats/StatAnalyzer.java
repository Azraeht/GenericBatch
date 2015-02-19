package org.paris.batch.stats;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.paris.batch.exception.StatAnalyzerException;

/**
 * Classe permettant de fournir des statistiques sur les différents traitements des composants de GenericBatch
 * @author paramesm
 *
 */
public class StatAnalyzer {
	
	/**
	 * 
	 * On stocke les stats dans une linkedHashMap car cela permet de garder l'ordre
	 * de l'insertion et d'afficher les stats avec la fonction printStats 
	 * avec cette même ordre
	 * 
	 **/
	private LinkedHashMap<String, Integer> stats = null;
	
	private Logger logger = null;

	
	/**
	 * Constructeur 
	 * @param logger
	 */
	public StatAnalyzer(Logger logger){
		this.stats = new LinkedHashMap<String, Integer>();
		this.logger = logger;
	}
	
	/**
	 * Met à jour la valeur de la stat avec + 1
	 * @param key de la stat à mettre à jour
	 */
	public void update(String key) throws StatAnalyzerException{
		
		// On vérifie si la clé est valide
		checkKeyValid(key);
		
		// Si la stat existe on fait +1 de la valeur
		if (this.stats.containsKey(key))
			this.stats.put(key, this.stats.get(key)+1);
		// Sinon on en crée une avec la valeur à 1
		else
			this.stats.put(key, 1);
		logger.debug("Mise à jour de la stat " + key);
	}
	
	/**
	 * Vérifie si la clé est valide et crée une exception si ce n'est pas le cas
	 * @param key
	 */
	public void checkKeyValid(String key) throws StatAnalyzerException{
		if (key == null || key.trim().equals(""))
			throw new StatAnalyzerException("La clé Stat-Analyser est incorrect.");
	}
	
	/**
	 * Permet de créer une nouvelle stat avec la valeur à 0
	 * @param key la stat à créer
	 */
	public void add(String key) throws StatAnalyzerException{
		
		// On vérifie si la clé est valide
		checkKeyValid(key);
		
		this.stats.put(key,0);
		logger.debug("Création d'une nouvelle stat " + key);
	}
	
	/**
	 * Permet d'obtenir la valeur d'une stat pendant l'exécution du traitement
	 * @param key la stat souhaitée
	 * @return la valeur de la stat
	 */
	public Integer get(String key) throws StatAnalyzerException{
		// On vérifie si la clé est valide
		checkKeyValid(key);
		
		if (this.stats.containsKey(key))
			return this.stats.get(key);
		return 0;
	}
	
	/**
	 * Réinitialise la stat à 0 
	 * @param key la stat à reinitialiser
	 */
	public void reset(String key) throws StatAnalyzerException{
		// On vérifie si la clé est valide
		checkKeyValid(key);
		
		this.stats.put(key, 0);
		logger.debug("la stat " + key + " a été réinitialisée");
	}
	
	/**
	 * Affiche tous les stats dans la trace d'exécution
	 */
	public void printStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("bilan du traitement (STAT-Analyzer) : [" );
        Iterator<Entry<String, Integer>> iter = this.stats.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Integer> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(';').append(' ');
            }
        }
        sb.append("]");
		logger.info(sb.toString());
	}

	/**
	 * Méthode toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return stats.toString();
	}
}
