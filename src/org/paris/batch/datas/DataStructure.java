package org.paris.batch.datas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author santusbr
 * Classe représentant une structure de donnée issue d'un fichier ou d'une base de données
 */
public class DataStructure {

	private ArrayList<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();
	
	public void add(HashMap<String, Object> data){
		this.datas.add(data);
	}
	public void delete(HashMap<String, Object> data){
		this.datas.remove(data);
	}
	
	/**
	 * Retourne l'ArrayList des données
	 * @return données
	 */
	public ArrayList<HashMap<String, Object>> getDatas(){
		return datas;
	}
	
	public HashMap<String, Object> get(String key,String value){
		
		// TODO méthode permettant de recherche une donnée dans Datas
		return null;
	}
	
	/**
	 *	Méthode permettant de convertir en string l'Arraylist datas
	 */
	public String toString(){
		
		String s = "";
		for (HashMap<String, Object> h : datas){
		    Iterator it = h.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        s += "[" + pairs.getKey() + " = " + pairs.getValue()+"] ";
		        it.remove();
		    }
		    s += '\n';
		}
		return s;
	}
	
}
