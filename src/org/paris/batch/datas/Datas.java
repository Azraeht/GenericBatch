package org.paris.batch.datas;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author santusbr
 * Classe repr�sentant une structure de donn�e issue d'un fichier ou d'une base de donn�e
 */
public class Datas {

	private ArrayList<HashMap<String, Object>> datas = new ArrayList();
	
	public void add(HashMap<String, Object> data){
		this.datas.add(data);
	}
	public void delete(HashMap<String, Object> data){
		this.datas.remove(data);
	}
	public HashMap<String, Object> get(String key,String value){
		
		// TODO m�thode permettant de recherche une donn�e dans Datas
		return null;
	}
	public String toString(){
		
		// TODO m�thode permettant de convertir en string le Arraylist datas
		return null;
	}
	
}
