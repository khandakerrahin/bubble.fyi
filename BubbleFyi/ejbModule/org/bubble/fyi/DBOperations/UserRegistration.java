/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonDecoder;
//import org.bubble.fyi.Initializations.SecretKey;
import org.bubble.fyi.Logs.LogWriter;

/**
 * @author hafiz
 *
 */
public class UserRegistration {
	BubbleFyiDS bubbleDS;
	/**
	 * 
	 */
	public UserRegistration() {
		bubbleDS = new BubbleFyiDS();
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
	 * @param jsonDecoder schoolName,email,phone,password,custodianName,address,city,postcode
	 * @return 0:Successfully Inserted
	 * <br>1:User with the email address exists
	 * <br>2:Inserting organization details failed
	 * <br>11:Inserting user credentials failed
	 * <br>E:JSON string invalid
	 * <br>-1:Default Error Code
	 * <br>-2:SQLException
	 * <br>-3:General Exception
	 * <br>-4:SQLException while closing connection
	 */
	public String registerNewUser(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		/*
			String sqlInsertUsers="INSERT INTO tbl_user("
					+ "user_name,user_email,user_password,user_type,status,phone,key_seed,passwd_enc)" 
					+ "VALUES" 
					+ "(?,?,?,'Admin',1,?,?,AES_ENCRYPT(?,concat_ws('',?,?,?,?)))";/**/
		String sqlInsertCustomer="INSERT INTO tbl_user"
				+ "(custodian_name,organisation_name,username,password,email,msisdn, address,postcode)"
				+ "VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?)";
		String sqlInsertAdmin="INSERT INTO tbl_admin"
				+ " (name, email, password, phone) "
				+ "VALUES ("
				+ "?, ?, ?, ?)";

		String userId="-1";
      try {
		String userType= jsonDecoder.getJsonObject().getString("userType");
		if(userType =="Admin") {
			try {
				//json: name,email,phone,password
				bubbleDS.prepareStatement(sqlInsertAdmin,true);
				bubbleDS.getPreparedStatement().setString(1, jsonDecoder.getJsonObject().getString("name"));
				bubbleDS.getPreparedStatement().setString(2, jsonDecoder.getJsonObject().getString("email"));
				bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("phone")));
				bubbleDS.getPreparedStatement().setString(4, jsonDecoder.getJsonObject().getString("password"));


				boolean insertSuccess=false;
				try{ 
					bubbleDS.execute();
					insertSuccess=true;
					//userId=getUserId();
				}catch(SQLIntegrityConstraintViolationException de) {
					errorCode="1:User with the email address or phone number exists";
					LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:"+de.getMessage());
				}catch(SQLException e) {
					errorCode="11:Inserting user credentials failed";
					LogWriter.LOGGER.severe("SQLException"+e.getMessage());
				}
				//LogWriter.LOGGER.info("UserID:"+userId);
				bubbleDS.closePreparedStatement();
			}catch(Exception e) {
				//TODO

			}
		}else {

			try {
				bubbleDS.prepareStatement(sqlInsertCustomer);
				//	(custodian_name,organisation_name,username,password,email,msisdn, address,postcode);
				bubbleDS.getPreparedStatement().setString(1, jsonDecoder.getJsonObject().getString("custodian_name"));					
				bubbleDS.getPreparedStatement().setString(2, jsonDecoder.getJsonObject().getString("organisation_name"));
				bubbleDS.getPreparedStatement().setString(3, jsonDecoder.getJsonObject().getString("username"));
				bubbleDS.getPreparedStatement().setString(4, jsonDecoder.getJsonObject().getString("password"));
				bubbleDS.getPreparedStatement().setString(5, jsonDecoder.getJsonObject().getString("email"));
				bubbleDS.getPreparedStatement().setString(6, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
				bubbleDS.getPreparedStatement().setString(7, jsonDecoder.getJsonObject().getString("address"));
				bubbleDS.getPreparedStatement().setString(8, jsonDecoder.getJsonObject().getString("postcode"));
				bubbleDS.execute();
				errorCode="0:Successfully Inserted";
			}catch(SQLException e) {
				errorCode="2:Inserting customer details failed";
				LogWriter.LOGGER.severe(e.getMessage());
				bubbleDS.closePreparedStatement();
				//new UserDBOperations().deleteUsersEntry(userId); //only deletes from users table
			}
			if(bubbleDS.getConnection() != null) bubbleDS.closePreparedStatement();
		 }

		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}finally{
			if(bubbleDS.getConnection() != null){
				try {
					//					fsDS.getConnection().setAutoCommit(true);
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
	
	return errorCode;
}
/**
 * 
 * @return
 * @throws SQLException
 */
private String getUserId() throws SQLException {
	String retval="-1";
	ResultSet rs=bubbleDS.getGeneratedKeys();
	if(rs.next()) {
		retval=rs.getString(1);
	}
	return retval;
}
@SuppressWarnings("unused")
private String getUserIdFromSequence(BubbleFyiDS bubbleDS) throws SQLException {
	String retval="-1";
	String sqlSequence="SELECT LAST_INSERT_ID()";
	bubbleDS.prepareStatement(sqlSequence);
	ResultSet rs=bubbleDS.executeQuery();
	if(rs.next()) {
		retval=rs.getString(1);
	}
	bubbleDS.closePreparedStatement();		
	return retval;
}

}
