package org.paris.batch.datas;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author santusbr
 * Classe représentant une structure de donnée issue d'un fichier ou d'une base de donnée
 */
public class DataStructure {

	private ArrayList<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();
	
	public void add(HashMap<String, Object> data){
		this.datas.add(data);
	}
	public void delete(HashMap<String, Object> data){
		this.datas.remove(data);
	}
	public HashMap<String, Object> get(String key,String value){
		
		// TODO méthode permettant de recherche une donnée dans Datas
		return null;
	}
	public String toString(){
		
		// TODO méthode permettant de convertir en string le Arraylist datas
		return null;
	}
	
}
