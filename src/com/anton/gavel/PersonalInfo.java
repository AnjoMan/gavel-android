package com.anton.gavel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PersonalInfo { 
	public static final Integer FIRST_NAME = R.id.first_name;
	public static final Integer LAST_NAME = R.id.last_name;
	public static final Integer PRIMARY_PHN = R.id.primary_phone;
	public static final Integer SECONDARY_PHN = R.id.secondary_phone;
	public static final Integer EMAIL = R.id.email_address;
	public static final Integer STREET_ADDRESS = R.id.street_address;
	public static final Integer POSTAL_CODE = R.id.postal_code;
	public static final Integer CITY = R.id.personal_cities_spinner;
	public final Integer REQUIRED[] = {FIRST_NAME, LAST_NAME, PRIMARY_PHN, EMAIL, STREET_ADDRESS,POSTAL_CODE,CITY};
	public static final Integer FIELDS[] = {FIRST_NAME, LAST_NAME, PRIMARY_PHN, SECONDARY_PHN, EMAIL, STREET_ADDRESS,POSTAL_CODE,CITY};

	private Map<Integer, String> personalInfo = new HashMap<Integer,String>();
		//store values
	
	public static final Map<String, Integer> nameToId = new HashMap<String, Integer>();
		//get id from field description
	public static final Map<Integer, String> idToName = new HashMap<Integer, String>();
		//get description from id
	static {
		idToName.put(FIRST_NAME,"first_name");
		idToName.put(LAST_NAME,"last_name");
		idToName.put(PRIMARY_PHN,"primary_phone");
		idToName.put(SECONDARY_PHN,"secondary_phone");
		idToName.put(EMAIL,"email_address");
		idToName.put(STREET_ADDRESS, "street_address");
		idToName.put(POSTAL_CODE,"postal_code");
		idToName.put(CITY,"city");
		//build map of ids to names
		
		for (int i = 0; i<FIELDS.length; i++){
			nameToId.put(idToName.get(FIELDS[i]), FIELDS[i]);
			//build map of names to ids
		}
		
		
		
	}
	public PersonalInfo(){
		//leave the info thing empty
	}
	public boolean isComplete(){
		if (personalInfo.isEmpty()) { return false; } // if no keys exist, incomplete
		else {
			for (int i = 0; i<REQUIRED.length; i++){
				if (!personalInfo.containsKey(REQUIRED[i])) {return false;}
			}//if any REQUIRED key does not exist, incomplete
			return true;//if we make it through the loop, all required keys exist
		}			
	}
	public String getField(Integer field){
		if (field.equals(SECONDARY_PHN)) {
			return personalInfo.containsKey(field) ? personalInfo.get(field) : "";				
		}
		else {
			return personalInfo.containsKey(field) ? personalInfo.get(field) : null;
		}
	}
	
	public String getField(String fieldName){
		return this.getField(nameToId.get(fieldName));
	}
	public boolean setField(Integer field, String value){
		if (value.equals("")){ 
			if (personalInfo.containsKey(field)){ personalInfo.remove(field); }
			return false; 
		} 
		else {
			personalInfo.put(field, value);
			return true;
		}
	}
	public boolean setField(String fieldName, String value){
		if (nameToId.containsKey(fieldName)) {
			this.setField(nameToId.get(fieldName), value);
			return true;
		}
		else
			return false;
	}
	
	public static Set<String> getNames(){
		return nameToId.keySet();
	}

	@Override
	public String toString(){
		return personalInfo.toString();
	}
	public void loadFromPreferences(SharedPreferences preferences){
		Set<String> names = this.getNames();
		for(String name : names)
			this.setField(name, preferences.getString(name, ""));
			//set values in mPersonalInfo (default is "", no key is added);
		
	}
	public void saveToPreferences(SharedPreferences preferences){
		Editor editor = preferences.edit();
		Set<String> names = this.getNames();
		
		for(String name : names)
			editor.putString(name,  this.getField(name));
		editor.commit();
	}
}