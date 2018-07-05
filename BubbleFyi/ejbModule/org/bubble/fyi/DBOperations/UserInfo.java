/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonEncoder;
import org.bubble.fyi.Initializations.LoadConfigurations;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Api.AuthenticationToken;

/**
 * @author hafiz
 *
 */
public class UserInfo {
	BubbleFyiDS bubbleDS;
	/**
	 * 
	 */
	public UserInfo(BubbleFyiDS bubbleDS) {
		//bubbleDS= new BubbleFyiDS(); 
		this.bubbleDS=bubbleDS;
	}
	
	public JsonEncoder listUsers() {
		JsonEncoder jsonUserList=new JsonEncoder();
		
		return jsonUserList;		
	}
	/**
	 * If msisdn starts with 0, prepends 88.
	 * If msisdn starts with 880 or any other number, returns the String
	 * @param msisdn
	 * @return msisdn of the format 8801xx
	 */
	private String msisdnNormalize(String msisdn) {
		if(msisdn.startsWith("0")) {
			msisdn="88"+msisdn;
		}
		return msisdn;
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
	 */
	public JsonEncoder fetchUserInfo(String id, String mode) {
		JsonEncoder jsonEncoder=new JsonEncoder();
		String errorCode="-1";//default errorCode
		//String sql="SELECT u.user_id, u.user_name, o.organization_name, u.user_email, u.user_type, u.phone, u.status, o.custodian_email,o.custodian_name,o.custodian_phone,o.organization_type,o.address,o.city,o.postcode FROM users u left join organizations o on u.user_id=o.user_id where u.<mode>=?";
		//TODO
		/*String sql="SELECT u.id, u.custodian_name,u.address, u.organization_name, u.username,u.email, if(u.flag=5,'Admin','Customer') as user_type,u.flag, u.phone, u.postcode,u.city,ifnull(u.logo_file,\"\") as logofile,cb.balance/0.25 as smsCount "
				+ "FROM tbl_users u,customer_balance cb where u.id=cb.user_id and u.<mode>=?";*/
		String sql="SELECT u.id, u.custodian_name,u.address, u.organization_name, u.username,u.email, if(u.flag=5,'Admin','Customer') as user_type,u.flag, u.phone, u.postcode,u.city,ifnull(u.logo_file,\"\") as logofile,u.feature_list "
				+ "FROM tbl_users u where  u.<mode>=?";		
		if(mode.equals("2")) { //email
			sql=sql.replace("<mode>", "email");
		}else if(mode.equals("1")) { //phone
			sql=sql.replace("<mode>", "phone");
			id=msisdnNormalize(id);
		}else {
			sql=sql.replace("<mode>", "username");
		}
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				jsonEncoder.addElement("id", rs.getString("id"));
				jsonEncoder.addElement("username", rs.getString("username"));
				jsonEncoder.addElement("email", rs.getString("email"));
				jsonEncoder.addElement("phoneNumber", rs.getString("phone"));
				jsonEncoder.addElement("userType", rs.getString("user_type"));
				jsonEncoder.addElement("status", rs.getString("flag"));
				jsonEncoder.addElement("logoFileName", rs.getString("logofile"));
				
				String featureList= rs.getString("feature_list");
				if(featureList.length()>=5) {
				jsonEncoder.addElement("geographicTarget", ""+featureList.charAt(0));
				jsonEncoder.addElement("demographicTarget", ""+featureList.charAt(1));
				jsonEncoder.addElement("behavioralTarget", ""+featureList.charAt(2));
				jsonEncoder.addElement("psychographicTarget", ""+featureList.charAt(3));
				jsonEncoder.addElement("masking", ""+featureList.charAt(4));
				
				}
				else {
					jsonEncoder.addElement("geographicTarget", "0");
					jsonEncoder.addElement("demographicTarget", "0");
					jsonEncoder.addElement("behavioralTarget", "0");
					jsonEncoder.addElement("psychographicTarget", "0");
					jsonEncoder.addElement("masking", "0");
				}
				if(featureList.contains("00000") || featureList.contains("00001") ) {
					jsonEncoder.addElement("targetSMS", "0");
				}else {
					jsonEncoder.addElement("targetSMS", "1");
				}
				//jsonEncoder.addElement("smsRemaining", rs.getString("smsCount"));	
				if(!rs.getString("user_type").equals("Admin")) {
				jsonEncoder.addElement("custodian_name", rs.getString("custodian_name"));
				jsonEncoder.addElement("address", rs.getString("address"));
				jsonEncoder.addElement("organization_name", rs.getString("organization_name"));
				jsonEncoder.addElement("postcode", rs.getString("postcode"));
				jsonEncoder.addElement("city", rs.getString("city"));
				}
				
				errorCode="0";
			}else {
				errorCode="-9:User details could not be retrieved";
			}
			rs.close();
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		/*finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}/**/
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}
	
	
	public JsonEncoder fetchUserInfoApi(String id, String mode) {
		JsonEncoder jsonEncoder=new JsonEncoder();
		String userID="-1";
		String errorCode="-1";//default errorCode
		String sql="SELECT u.id,c.balance "
				+ "FROM tbl_users u,customer_balance c where  u.<mode>=? and u.id=c.user_id";		
		if(mode.equals("2")) { //email
			sql=sql.replace("<mode>", "email");
		}else if(mode.equals("1")) { //phone
			sql=sql.replace("<mode>", "phone");
			id=msisdnNormalize(id);
		}else {
			sql=sql.replace("<mode>", "username");//username
		}
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				userID=rs.getString("id");
				//jsonEncoder.addElement("id", userID);
				jsonEncoder.addElement("balance", rs.getString("balance"));			
				errorCode="0000";
			}else {
				errorCode="0020";
			}
			rs.close();
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		}catch(Exception e){
			errorCode= "0020";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		
		if(LoadConfigurations.checkValidUserToken.containsKey(userID)) {
			String oldToken=LoadConfigurations.checkValidUserToken.get(userID);
			LoadConfigurations.checkValidUserToken.remove(userID);
			
			if(LoadConfigurations.authenticationTokenHM.containsKey(oldToken)) 
			LoadConfigurations.authenticationTokenHM.remove(oldToken);
			
			//LogWriter.LOGGER.info("inside validity check after " + oldToken);
		}
		
		if(errorCode.equals("0000")) {
			// add token
			//AuthenticationToken at= new AuthenticationToken();
			//String tokenId=at.generateNewTokenId(userID);
			//TODO check if it always returns token or not 
			String tokenn=LoadConfigurations.generateNewTokenId(userID);
			LoadConfigurations.checkValidUserToken.put(userID, tokenn);
			jsonEncoder.addElement("token", tokenn);
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}
	
	public String expirehashRecordCleaner() {
		String retval="0";
		//TODO 
		/*
		if(LoadConfigurations.checkValidUserToken.containsKey(userID)) {
			String oldToken=LoadConfigurations.checkValidUserToken.get(userID);
			LoadConfigurations.checkValidUserToken.remove(userID);
			
			if(LoadConfigurations.authenticationTokenHM.containsKey(oldToken)) 
			LoadConfigurations.authenticationTokenHM.remove(oldToken);
			
			LogWriter.LOGGER.info("inside validity check after " + oldToken);
		}/**/
		return retval;
	}

}
