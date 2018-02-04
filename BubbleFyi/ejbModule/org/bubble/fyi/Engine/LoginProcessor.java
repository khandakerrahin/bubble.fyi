/**
 * 
 */
package org.bubble.fyi.Engine;

import org.bubble.fyi.DBOperations.Login;
import org.bubble.fyi.DBOperations.UserInfo;
import org.bubble.fyi.Logs.LogWriter;
import java.util.logging.Logger;;

/**
 * @author wasif
 * Get login data from webserver
 * match login data with db users table
 * 
 */
public class LoginProcessor {
	Login loginDBOperations;
	/**
	 * 
	 */
	
	public LoginProcessor() {
		loginDBOperations = new Login();
	}
	
	/**
	 * @json { "username":"t1@sp.com", "password":"specialt1pass", "mode":"1"} <br>mode 1:email, 2:phone
	 * @action login
	 * @param message jsonDecoder
	 * @param messageBody jsonDecoder
	 * @return jsonEncoder userInfo on success
	 * <br>errorCode 0 indicated success in fetching data
	 * <br>-1:General Error
	 * <br>-2:SQLException in fetchUserInfo()
	 * <br>-3:Exception
	 * <br>-4:SQLException while closing
	 * <br>1:User verified
	 * <br>0:User credentials did not match
	 * <br>E:General Exception
	 * <br>-2:General Error at compareCredentialsInDB()
	 * <br>E: General Error
	 * <br>E:JSON string invalid
	 * 
	 */
	public String processLogin(String message, String messageBody) {
		String retval="E";
		JsonDecoder loginCredentials;
		if(messageBody.isEmpty()) {
			loginCredentials=new JsonDecoder(message);
			
		}else {
			loginCredentials=new JsonDecoder(messageBody);
		}
		LoginProcessor loginProcessor=new LoginProcessor();
		if(loginCredentials.getErrorCode().equals("0")) {
			retval=loginProcessor.checkCredentials(loginCredentials);
		}else{
			retval="E:JSON string invalid";
		}
		if(retval.equals("1")) {
			retval=fetchUserInfo(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("mode"));
		}else {
			retval="-6:Error in user Credentials";
		}
		return retval;
	}
	
	public String processLoginAPI(String message, String messageBody) {
		String retval="E";
		JsonDecoder loginCredentials;
		if(messageBody.isEmpty()) {
			loginCredentials=new JsonDecoder(message);
			
		}else {
			loginCredentials=new JsonDecoder(messageBody);
		}
		LoginProcessor loginProcessor=new LoginProcessor();
		if(loginCredentials.getErrorCode().equals("0")) {
			retval=loginProcessor.checkCredentials(loginCredentials);
		}else{
			retval="E:JSON string invalid";
		}
		if(retval.equals("1")) {
			retval=fetchUserInfo(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("mode"));
		}else {
			retval="-6:Error in user Credentials";
		}
		return retval;
	}
	/**
	 * 
	 * @param id
	 * @param mode 1:email, 2:phone
	 * @return jsonEncoder userInfo on success
	 * <br>errorCode 0 indicated success in fetching data
	 * <br>-1:General Error
	 * <br>-2:SQLException
	 * <br>-3:Exception
	 * <br>-4:SQLException while closing
	 * 
	 */
	private String fetchUserInfo(String id, String mode) {
		return new UserInfo().fetchUserInfo(id, mode).getJsonObject().toString();
	}
	/**
	 * 
	 * @param loginCredentials
	 * @return 1:User verified
	 * 0:User credentials did not match
	 * E:General Exception
	 * -2:General Error at compareCredentialsInDB()
	 */
	public String checkCredentials(JsonDecoder loginCredentials){
		return loginDBOperations.compareCredentialsInDB(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("password"),Integer.parseInt(loginCredentials.getJsonObject().getString("mode")));
	}
	/**
	 * 
	 * @param loginCredential
	 * @param password
	 * @param mode
	 * @return 1:User verified
	 * 0:User credentials did not match
	 * E:General Exception
	 * -2:General Error at compareCredentialsInDB()
	 */
	public String checkCredentials(String loginCredential, String password, int mode){
		return loginDBOperations.compareCredentialsInDB(loginCredential,password,mode);
	}
	
}
