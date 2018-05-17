package org.bubble.fyi.Initializations;

import java.util.HashMap;

import org.bubble.fyi.Api.AuthenticationToken;



public class LoadConfigurations {

	replyMessageLoader replySMSLoader = new replyMessageLoader();
	HashMap<String, String> DBResponseCode = new HashMap<String,String>();
    public static HashMap<String, AuthenticationToken> authenticationTokenHM = new HashMap<String,AuthenticationToken>();
    public static HashMap<String, String> checkValidUserToken = new HashMap<String,String>();
	
	public HashMap<String, AuthenticationToken> getAuthenticationTokenHM() {
		return authenticationTokenHM;
	}
	
	public static String generateNewTokenId(String userID) {
		String retval=null;
		AuthenticationToken at= new AuthenticationToken();
		String tokenId=at.generateNewTokenId(userID);
		authenticationTokenHM.put(tokenId, at);
		retval=tokenId;
		return retval;
	}

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
	
 
}

