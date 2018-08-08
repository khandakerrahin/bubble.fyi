/**
 * 
 */
package org.bubble.fyi.Engine;

import org.bubble.fyi.DBOperations.Login;
import org.bubble.fyi.DBOperations.UserInfo;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.DataSources.BubbleFyiDS;

import java.util.logging.Logger;;

/**
 * @author wasif
 * Get login data from webserver
 * match login data with db users table
 * 
 */
public class LoginProcessor {
	//Login loginDBOperations;

	BubbleFyiDS bubbleDS;
	LogWriter logWriter;
	/**
	 * 
	 */

	public LoginProcessor(BubbleFyiDS bubbleDS) {
		//loginDBOperations = new Login();
		this.bubbleDS=bubbleDS;
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
		//LoginProcessor loginProcessor=new LoginProcessor(bubbleDS);
		if(loginCredentials.getErrorCode().equals("0")) {
			retval=this.checkCredentials(loginCredentials);
		}else{
			retval="E:JSON string invalid";
		}
		if(retval.equals("1")) {
			retval=fetchUserInfo(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("mode"));
		}else {
			retval="-6:Error in user Credentials";
			JsonEncoder jsonEncoder=new JsonEncoder();
			jsonEncoder.addElement("ErrorCode", retval);
			retval=jsonEncoder.buildJsonObject().toString();
		}
		return retval;
	}

	public String authenticateUser(String message, String messageBody) {
		String retval="E";
		String respCode="0005";
		JsonDecoder loginCredentials;
		if(messageBody.isEmpty()) {
			loginCredentials=new JsonDecoder(message);			
		}else {
			loginCredentials=new JsonDecoder(messageBody);
		}
		if(loginCredentials.getErrorCode().equals("0")) {
			retval=this.checkCredentials(loginCredentials);
		}else{
			respCode="0009";
		}
		if(retval.equals("1")) {
			respCode="0000";
			//retval=fetchUserInfo(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("mode"));
			retval=new UserInfo(bubbleDS).fetchUserInfoApi(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("mode")).getJsonObject().toString();
			//new UserInfo(bubbleDS).fetchUserInfo(id, mode).getJsonObject().toString();
		}else{
			//retval="-6:Error in user Credentials";
			respCode="0010";			
		}
		
		if(!respCode.equals("0000")) {
			JsonEncoder jsonEncoder=new JsonEncoder();
			jsonEncoder.addElement("ErrorCode", respCode);
			jsonEncoder.buildJsonObject();
			retval=jsonEncoder.getJsonObject().toString();
		}
		
		return retval;
	}
	
	public String cleanExpiredRecords() {
		String retval="-1";
		retval=new UserInfo(bubbleDS).expirehashRecordCleaner();
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
		return new UserInfo(bubbleDS).fetchUserInfo(id, mode).getJsonObject().toString();
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
		return new Login().compareCredentialsInDB(loginCredentials.getJsonObject().getString("username"),loginCredentials.getJsonObject().getString("password"),Integer.parseInt(loginCredentials.getJsonObject().getString("mode")));
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
		return new Login().compareCredentialsInDB(loginCredential,password,mode);
	}

}
