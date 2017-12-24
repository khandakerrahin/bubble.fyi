package org.bubble.fyi.Initializations;

import java.util.HashMap;



public class LoadConfigurations {

	replyMessageLoader replySMSLoader = new replyMessageLoader();
	HashMap<String, String> DBResponseCode = new HashMap<String,String>();
	public LoadConfigurations() {
		// TODO Auto-generated constructor stub
	}

	public void loadConfigurationFromDB() {
		replySMSLoader.getRelpyMessage();
		
	}

	/**
	 * @return HashMap(rawtypes) replySMSLoader.replyMessage
	 */
	@SuppressWarnings("rawtypes")
	public HashMap getReplySmsHash(){
		return  this.replySMSLoader.replyMessage;
	} 
	

	/**
	 * @return HashMap<String,String> DBResponseCode
	 */
	public HashMap<String,String> getOpenCodeResponseCodeMapp(){

		DBResponseCode.put("0", "Success");    	
		DBResponseCode.put("1", "Already subscribed");
		DBResponseCode.put("2", "Not subscribed");
		DBResponseCode.put("3", "No permission");	
		DBResponseCode.put("4", "4");

		return  this.DBResponseCode;
	}   
}

