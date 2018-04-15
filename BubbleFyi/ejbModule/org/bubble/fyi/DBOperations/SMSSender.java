
package org.bubble.fyi.DBOperations;

//import java.io.UnsupportedEncodingException;
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
	public SMSSender(BubbleFyiDS bubbleDS) {
		this.bubbleDS = bubbleDS;
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
		}else if(msisdn.startsWith("+0")) {
			msisdn="88"+msisdn.substring(1);
		}else if(msisdn.startsWith("+88")) {
			msisdn=msisdn.substring(1);
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
		finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.closeResultSet();
					bubbleDS.closePreparedStatement();
					//bubbleDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}

		return retval;
	}

	public String getAparty(String userId) {
		String retval="-1";
		String sql="select aparty from customer_balance where user_id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				retval=bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.info("getAparty(): "+e.getMessage());
		}
		/*
		finally{
				if(bubbleDS.getConnection() != null){
					try {
						bubbleDS.getConnection().close();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}      
			}/**/
		LogWriter.LOGGER.info("getAparty(): "+retval);
		return retval;
	}

	public double getCustomerBalance(String userId) {
		//TODO needs to update with price
		double retval=-1;
		String sql="SELECT balance FROM `customer_balance` WHERE user_id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				retval=bubbleDS.getResultSet().getDouble(1);
			}else {
				retval=-2;
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval=-3;
			LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
		}
		/*
		finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {					
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}/**/

		LogWriter.LOGGER.info("customer Balance(): "+retval);
		return retval;
	}

	private boolean updateBalanceSingleSMS(String userId,int val,double smsPrice) {
		//TODO needs to update with cost
		boolean retval=false;
		//TODO needs to change for variable price
		double amount=smsPrice*val;
		String sqlUpdateUser="UPDATE `customer_balance` SET balance = balance-? WHERE user_id=?";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setDouble(1, amount);
			bubbleDS.getPreparedStatement().setString(2, userId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval=true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("userId(): "+e.getMessage());
		}
		/*finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					retval=false;
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
		double smsPrice=0.25;
		String userID=jsonDecoder.getJsonObject().getString("id"); 
		String message=jsonDecoder.getJsonObject().getString("smsText");
		/*
		String s1 = message;
		try {
			byte[] bytes = s1.getBytes("UTF-8");
			message = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}/**/
		LogWriter.LOGGER.info(" ----message----:"+message);
		
		try {
			String userStatus=getUserType(userID);
			String aparty=getAparty(userID);
			
			
			int smsCount=getSMSSize(message);
			//TODO if aparty null check 
			//if(userStatus.equals("1") || userStatus.equals("5")) { 
			//1=active customer 5=admin 10=high value customer
			if(userStatus.equals("1") || userStatus.equals("5") || userStatus.equals("10")) {
				//TODO checkBalance(userID) returns available balance
				String sqlInsert="INSERT INTO smsinfo"
						+ " (userid,message,bparty,source,aparty) "
						+ "VALUES (?, ?, ?,'bubble',?)";
				if(getCustomerBalance(userID)>=smsPrice) {
					try {
						if(updateBalanceSingleSMS(userID,smsCount,smsPrice)) {
							try {

								//TODO charge customer boolean updateBalanceSingleSMS(userID)
								bubbleDS.prepareStatement(sqlInsert);
								//	(custodian_name,organisation_name,username,password,email,phone, address,postcode,city);
								bubbleDS.getPreparedStatement().setString(1, userID);					
								bubbleDS.getPreparedStatement().setString(2,message );
								bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
								bubbleDS.getPreparedStatement().setString(4, aparty);
								bubbleDS.execute();
								errorCode="0:Successfully Inserted";								
								bubbleDS.closePreparedStatement();
							}catch(SQLException e) {
								updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
								//TODO update balance add 
								errorCode="11:Inserting credentials failed";
								e.printStackTrace();
								LogWriter.LOGGER.severe(e.getMessage());
								bubbleDS.closePreparedStatement();
								//new UserDBOperations().deleteUsersEntry(userId); //only deletes from users table
							}catch(Exception de) {
								//TODO update balance add 
								updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
								errorCode="-10: other exception";
								bubbleDS.closePreparedStatement();
								LogWriter.LOGGER.severe(de.getMessage());
								//de.printStackTrace();
								LogWriter.LOGGER.info("other Exception:"+de.getMessage());
							}
						}else {
							errorCode="-16: Payment Deduction Failed";
						}


					}catch(SQLException e){
						errorCode= "-2";
						LogWriter.LOGGER.severe(e.getMessage());
					}catch(Exception e){
						errorCode= "-3";
						LogWriter.LOGGER.severe(e.getMessage());
						e.printStackTrace();
					}
				}else {
					errorCode="5:low Balance";
				}
			}else {
				errorCode="-6:Sent sms not allowed";
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.closePreparedStatement();
				//	bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}  

		}
		return errorCode;
	}
	

	public int getSMSSize(String message) {
		int retval=1;
		int tempSize= smsLength(message);
		if(isUnicode(message)){
			if(isLongSmsUNICODE(tempSize)) {
				retval =smsCountUNICODE(tempSize);
			}else {
				retval=1;;
			}
		}else {
			if(isLongSmsASCII(tempSize)) {
				retval = smsCountASCII(tempSize);
			}else {
				retval=1;
			}
		}
		return retval;
	}
	public boolean isUnicode(String text) {
		boolean RETVAL =false;
		for(int i = 0; i < text.length() ; i++) {
			if(text.charAt(i)>=128){
				RETVAL = true;
				break;	
			}
		}
		return RETVAL;	
	}	
	public boolean isLongSmsASCII(int textSize) {
		boolean RETVAL =false;
		if(textSize>160)
			RETVAL=true;
		return RETVAL;
	}
	public boolean isLongSmsUNICODE(int textSize) {
		boolean RETVAL =false;
		if(textSize>67)
			RETVAL=true;
		return RETVAL;
	}
	public int smsCountUNICODE(int textSize) {
		int count=-100;
		count = (int) Math.ceil(textSize/67.00);		
		return count;
	}
	public int smsCountASCII(int textSize) {
		int count=-100;
		count = (int) Math.ceil(textSize/157.00);		
		return count;
	}
	public int smsLength(String text) {
		System.out.println("Length of TEXT : "+text.length());
		return text.length();
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
		}/*finally{
			if(bubbleDS.getConnection() != null){
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}  

		}/**/
		return errorCode;
	}
}


