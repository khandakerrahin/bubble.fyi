
package org.bubble.fyi.DBOperations;

import java.sql.SQLException;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonDecoder;
import org.bubble.fyi.Logs.LogWriter;

/**
 * @author hafiz
 *
 */
public class SMSSender {
	BubbleFyiDS bubbleDS;
	/**
	 * 
	 */
	public SMSSender() {
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
	//new customer not yet approved 0
	//approved customer 1 , admin 5 
	public String getUserType(String userId) {
		String retval="-1";
		String sql="select flag from tbl_users where id=?";
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, userId);
				bubbleDS.executeQuery();
				if(bubbleDS.getResultSet().next()) {
					retval=bubbleDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
			}
			/*finally{
				if(bubbleDS.getConnection() != null){
					try {
						bubbleDS.getConnection().close();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}      
			}/**/
		
		return retval;
	}
	
	public String getAparty(String userId) {
		String retval="-1";
		String sql="select aparty from tbl_users where id=?";
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, userId);
				bubbleDS.executeQuery();
				if(bubbleDS.getResultSet().next()) {
					retval=bubbleDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
			}
			/*finally{
				if(bubbleDS.getConnection() != null){
					try {
						bubbleDS.getConnection().close();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}      
			}/**/
		
		return retval;
	}
	/**
	 * 
	 * @param jsonDecoder msisdn,smsText,id
	 * @return 0:Successfully Inserted
	 * <br>1:User with the email address exists
	 * <br>2:Inserting  details failed
	 * <br>11:Inserting  credentials failed
	 * <br>E:JSON string invalid
	 * <br>-1:Default Error Code
	 * <br>-2:SQLException
	 * <br>-3:General Exception
	 * <br>-4:SQLException while closing connection
	 */
	public String insertToSender(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode	
		//TODO check if eligible to send sms. 
		String userID=jsonDecoder.getJsonObject().getString("id"); 
		//String aparty=getAparty(userID);
		//TODO insert aparty 
		try {
		String userStatus=getUserType(userID);
		//if(userStatus.equals("1") || userStatus.equals("5")) {
		if(userStatus.equals("1") || userStatus.equals("5") || userStatus.equals("10")) {
		String sqlInsert="INSERT INTO smsinfo"
				+ " (userid,message,bparty,source) "
				+ "VALUES (?, ?, ?,'bubble')";

		try {
			try {
				bubbleDS.prepareStatement(sqlInsert);
				//	(custodian_name,organisation_name,username,password,email,phone, address,postcode,city);
				bubbleDS.getPreparedStatement().setString(1, userID);					
				bubbleDS.getPreparedStatement().setString(2, jsonDecoder.getJsonObject().getString("smsText"));
				bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
				bubbleDS.execute();
				errorCode="0:Successfully Inserted";
			}catch(SQLException e) {
				errorCode="11:Inserting  credentials failed";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
				bubbleDS.closePreparedStatement();
				//new UserDBOperations().deleteUsersEntry(userId); //only deletes from users table
			}catch(Exception de) {
				errorCode="-10: other exception";
				LogWriter.LOGGER.severe(de.getMessage());
				//de.printStackTrace();
				LogWriter.LOGGER.info("other Exception:"+de.getMessage());
			}
			if(bubbleDS.getConnection() != null) bubbleDS.closePreparedStatement();

		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				errorCode="-4";
				e.printStackTrace();
			}
		}
	}else {
		errorCode="-6:Sent sms not allowed";
	}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}  
		
	}
		return errorCode;
	}
	
	public String insertToSenderAPI(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode	
		//TODO check if eligible to send sms. 
		String userID=jsonDecoder.getJsonObject().getString("id"); 
		//String aparty=getAparty(userID);
		//TODO insert aparty 
		try {
		String userStatus=getUserType(userID);
		if(userStatus.equals("1") || userStatus.equals("5")) {
			
		String sqlInsert="INSERT INTO smsinfo"
				+ " (userid,message,bparty) "
				+ "VALUES (?, ?, ?)";

		try {
			try {
				bubbleDS.prepareStatement(sqlInsert);
				//	(custodian_name,organisation_name,username,password,email,phone, address,postcode,city);
				bubbleDS.getPreparedStatement().setString(1, userID);					
				bubbleDS.getPreparedStatement().setString(2, jsonDecoder.getJsonObject().getString("smsText"));
				bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
				bubbleDS.execute();
				errorCode="0:Successfully Inserted";
			}catch(SQLException e) {
				errorCode="11:Inserting  credentials failed";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
				bubbleDS.closePreparedStatement();
				//new UserDBOperations().deleteUsersEntry(userId); //only deletes from users table
			}catch(Exception de) {
				errorCode="-10: other exception";
				LogWriter.LOGGER.severe(de.getMessage());
				//de.printStackTrace();
				LogWriter.LOGGER.info("other Exception:"+de.getMessage());
			}
			if(bubbleDS.getConnection() != null) bubbleDS.closePreparedStatement();

		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				errorCode="-4";
				e.printStackTrace();
			}
		}
		}else {
			errorCode="-6:Sent sms not allowed";
		}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}  
		
	}
		return errorCode;
	}
}


