
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonDecoder;
import org.bubble.fyi.Engine.JsonEncoder;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;

//import com.sun.media.jfxmedia.logging.Logger;

/**
 * @author wasif
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
	
	/*
	public static boolean isNumeric(String strNum) {
	    return strNum.matches("-?\\d+(\\.\\d+)?");
	}/**/
	
	/**
	 * 
	 * @param msisdn
	 * @return true if msisdn fromat is valid
	 */
	private boolean isValidMsisdn(String msisdn) {
		boolean retval=false;
		msisdn= msisdnNormalize(msisdn);
		boolean retvaltmp=msisdn.matches("-?\\d+(\\.\\d+)?");
		
		if(!msisdn.startsWith("88011") && msisdn.length()==13 && retvaltmp) {
			retval=true;
		}
		/*
		if(msisdn.length()==13){
			retval=true;
		}	/**/	
		/*
		if(retvaltmp) {
			retval=true;
		}/**/	
		//TOOD needs to work here 
		LogWriter.LOGGER.info("isNumeric resp :: "+retvaltmp +" nomalized msisdn:: "+msisdn+""+" msisdn.length(): "+msisdn.length()+" retval :: "+ retval );
		return retval;
	}
	/**
	 * 
	 * @param userId
	 * @return approved customer 1 , admin 5 high value 10 new customer not yet approved 0
	 */
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

	/**
	 * 
	 * @param userId
	 * @return Aparty
	 */
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
		LogWriter.LOGGER.info("getAparty(): "+retval);
		return retval;
	}

	/**
	 * 
	 * @param aparty
	 * @return String telco name based on sender prefix RT BP BL GP TT
	 */
	//public String getTelcoDetail(String userId,String aparty) {
	public String getTelcoDetail(String aparty) { 
		String retval="-1"; //TODO needs to change after mobile number portability comes
		if(aparty.startsWith("035") || aparty.startsWith("88035") || aparty.startsWith("+88035")) {
			retval= "BP";
		}else if(aparty.startsWith("+880444") || aparty.startsWith("880444") ) {
			retval= "RT";
		}else if(aparty.startsWith("+88019") || aparty.startsWith("88019") || aparty.startsWith("019")) {
			retval= "BL";
		}else if(aparty.startsWith("+88017") || aparty.startsWith("88017") || aparty.startsWith("017") ) {
			retval= "GP";
		}else if(aparty.startsWith("+88015") || aparty.startsWith("88015") || aparty.startsWith("015")) {
			retval= "TT";
		}else {
			retval= "RT";
		}
		/**
		String sql="select telco from customer_balance where user_id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				retval=bubbleDS.getResultSet().getString(1);
				//retval+=""+bubbleDS.getResultSet().getString(2);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.info("getTelcoDetail(): "+e.getMessage());
		}		
		LogWriter.LOGGER.info("getAparty(): "+retval);/**/
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @return customer balance double
	 */
	public double getCustomerBalance(String userId) {
		// needs to update with price
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
		LogWriter.LOGGER.info("customer Balance(): "+retval);
		return retval;
	}
	/**
	 * 
	 * @param userId
	 * @param chargeFor 1= regular Charge (non masking), 2= masking charge, 3= targetBased charge less than 3selection
	 * 4= targetBased charge more than 3 selection 5= inbox change 6=custom charge
	 * @return customer charged value for respected selection 
	 */
	/*
	public double getCustomerChargingDetail(String userId,int chargeFor) {
		// 1= regular Charge (non masking), 2= masking charge, 3= targetBased charge less than 3selection
		// 4= targetBased charge more than 3 selection 5= inbox change 6=custom charge
		double retval=-1;
		double regular_charge=-1;
		double masking=-1;
		double targetBased_3=-1;
		double targetBased_5=-1;
		double inbox=-1;		
		double custom=-1;

		String sql="SELECT regular_charge,masking,targetBased_3,targetBased_5,inbox,custom FROM tbl_charging where user_id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				regular_charge=bubbleDS.getResultSet().getDouble(1);
				masking=bubbleDS.getResultSet().getDouble(2);
				targetBased_3=bubbleDS.getResultSet().getDouble(3);
				targetBased_5=bubbleDS.getResultSet().getDouble(4);
				inbox=bubbleDS.getResultSet().getDouble(5);
				custom=bubbleDS.getResultSet().getDouble(6);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval=-3;
			LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
		}
		if(chargeFor == 1) retval=regular_charge;
		else if(chargeFor == 2) retval=masking;
		else if(chargeFor == 3) retval=targetBased_3;
		else if(chargeFor == 4) retval=targetBased_5;		
		else if(chargeFor == 5) retval=inbox;
		else if(chargeFor == 6) retval=custom;
		else retval=regular_charge;

		LogWriter.LOGGER.info("customer Charge: "+retval);
		return retval;
	}/**/
	/**
	 * 
	 * @param String userId
	 * @param int smsCount
	 * @param double smsPrice
	 * @return boolean true if balance update successfull
	 */
	private boolean updateBalanceSingleSMS(String userId,int smsCount,double smsPrice) {
		// needs to update with cost
		boolean retval=false;
		// needs to change for variable price
		double tmpAmount=smsPrice*smsCount;
		double amount= (double)Math.round(tmpAmount * 100d) / 100d; // 4.80000001 -> 4.8000001*10=48.00001.round 48 -> 48/10=4.8
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
	 * <br>-9:Customer Charging not Configured
	 * <br>-16: Payment Deduction Failed
	 * <br>5:low Balance
	 */
	public String insertToSender(JsonDecoder jsonDecoder) { 
		UserDBOperations UDOp=new UserDBOperations(bubbleDS);
		String errorCode="-1";//default errorCode
		String userID=jsonDecoder.getJsonObject().getString("id"); 
		//double smsPrice=0.25;
		Double smsCost=0.0;		
		double smsPrice=UDOp.getCustomerChargingDetailUDB(userID,1);//1=regular non masking charge
		if(smsPrice>0.0) {		
			String message=jsonDecoder.getJsonObject().getString("smsText");
			LogWriter.LOGGER.info(" ----message----:"+message);

			try {
				String userStatus=getUserType(userID);
				String aparty=getAparty(userID);
				String telco =getTelcoDetail(aparty);
				int smsCount=getSMSSize(message);
				// if aparty null check 
				//if(userStatus.equals("1") || userStatus.equals("5")) { 
				//1=active customer 5=admin 10=high value customer
				if(userStatus.equals("1") || userStatus.equals("5") || userStatus.equals("10")) {
					//checkBalance(userID) returns available balance
					double tmpSMSCost=smsPrice*smsCount;
					smsCost=(double)Math.round(tmpSMSCost * 100d) / 100d;
					LogWriter.LOGGER.info("smsCost:"+smsCost+" smsPrice:"+smsPrice+" smsCount:"+smsCount);
					String sqlInsert="INSERT INTO smsinfo"
							+ " (userid,message,bparty,source,aparty,telco_partner,cost,sms_count) "
							+ "VALUES (?, ?, ?,'bubble',?,?,?,?)";
					if(getCustomerBalance(userID)>=smsPrice) {
						try {
							if(updateBalanceSingleSMS(userID,smsCount,smsPrice)) {
								try {
									//TODO charge customer boolean updateBalanceSingleSMS(userID)
									bubbleDS.prepareStatement(sqlInsert);
									bubbleDS.getPreparedStatement().setString(1, userID);					
									bubbleDS.getPreparedStatement().setString(2, message);
									bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
									bubbleDS.getPreparedStatement().setString(4, aparty);
									bubbleDS.getPreparedStatement().setString(5, telco); //telco_partner									
									bubbleDS.getPreparedStatement().setDouble(6, smsCost);
									bubbleDS.getPreparedStatement().setInt(7, smsCount);
									bubbleDS.execute();
									errorCode="0:Successfully Inserted";								
									bubbleDS.closePreparedStatement();
									UDOp.transaction_logger(userID,smsCost,0,1,"");
								}catch(SQLException e) {
									updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
									// update balance add 
									errorCode="11:Inserting credentials failed";
									e.printStackTrace();
									LogWriter.LOGGER.severe(e.getMessage());
									bubbleDS.closePreparedStatement();
									//new UserDBOperations().deleteUsersEntry(userId); //only deletes from users table
								}catch(Exception de) {
									// update balance add 
									updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
									errorCode="-10: other exception";
									bubbleDS.closePreparedStatement();
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
		}else {
			errorCode="-9:Customer Charging not Configured";
		}
		return errorCode;
	}

	/**
	 * 
	 * @param jsonDecoder
	 * @return errorcode and error response 
	 */
	public JsonEncoder insertToSenderV2(JsonDecoder jsonDecoder) {
		JsonEncoder jsonEncoder = new JsonEncoder();		
		String errorCode = "-1";
		String errorResponse = "-1";
		int msisdnCount= 0;
		String failedMSISDNList ="";
		int successCount=0;
		String successMSISDNList="";
		int failCount=0;
		//String wrongMSISDNFormat="";
		Double smsCost=0.0;
		String broadcast_id="0";
		String errorCodetmp = "-1";
		String errorResponsetmp = "-1";	
		UserDBOperations UDOp=new UserDBOperations(bubbleDS);
		String userID=jsonDecoder.getJsonObject().getString("id");
		String msisdnList=jsonDecoder.getEString("msisdn");
		String [] msisdnListArray= msisdnList.split(",");
		int arraySize =msisdnListArray.length;
		msisdnCount=arraySize;
		double smsPrice=UDOp.getCustomerChargingDetailUDB(userID,1);//1=regular non masking charge
		String message=jsonDecoder.getJsonObject().getString("smsText");
		int smsCount=getSMSSize(message);		
		double tmpSMSCost=smsPrice*smsCount;
		double temptotalCost=tmpSMSCost*arraySize;
		smsCost=(double)Math.round(tmpSMSCost * 100d) / 100d;
		double totalCost=(double)Math.round(temptotalCost * 100d) / 100d;

		if(getCustomerBalance(userID)>=totalCost) {
			LogWriter.LOGGER.info("Sufficent balance: arraySize: "+arraySize+" totalCost: "+totalCost);
			//proceed
			if(smsPrice>0.0) {		
				LogWriter.LOGGER.info(" ----message----:"+message);
				try {
					String userStatus=getUserType(userID);
					String aparty=getAparty(userID);
					String telco =getTelcoDetail(aparty);
					//if aparty null check 
					//if(userStatus.equals("1") || userStatus.equals("5")) { 
					//1=active customer 5=admin 10=high value customer				
					if(arraySize>1) {
						// broadcastType=2 for single sms
						broadcast_id=createInsertId(userID,2,arraySize);	
					}
					if(userStatus.equals("1") || userStatus.equals("5") || userStatus.equals("10")) {
						for(int i=0;i<arraySize;i++){
							if(isValidMsisdn(msisdnListArray[i])) {						
							LogWriter.LOGGER.info("bparty :"+ msisdnListArray[i]+" ,smsCost:"+smsCost+" smsPrice:"+smsPrice+" smsCount:"+smsCount);
							String sqlInsert="INSERT INTO smsinfo"
									+ " (userid,message,bparty,source,aparty,telco_partner,cost,sms_count,broadcastId) "
									+ "VALUES (?, ?, ?,'bubble',?,?,?,?,?)";
							if(getCustomerBalance(userID)>=smsPrice) {
								try {
									if(updateBalanceSingleSMS(userID,smsCount,smsPrice)) {
										try {
											//TODO charge customer boolean updateBalanceSingleSMS(userID)
											bubbleDS.prepareStatement(sqlInsert);
											bubbleDS.getPreparedStatement().setString(1, userID);					
											bubbleDS.getPreparedStatement().setString(2, message);
											bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(msisdnListArray[i]));
											bubbleDS.getPreparedStatement().setString(4, aparty);
											bubbleDS.getPreparedStatement().setString(5, telco); //telco_partner									
											bubbleDS.getPreparedStatement().setDouble(6, smsCost);
											bubbleDS.getPreparedStatement().setInt(7, smsCount);
											bubbleDS.getPreparedStatement().setString(8, broadcast_id);
											bubbleDS.execute();
											errorCodetmp="0";
											errorResponsetmp="Successfully Inserted";
											errorCode="0";
											errorResponse="Successfully Inserted";
											bubbleDS.closePreparedStatement();
											UDOp.transaction_logger(userID,smsCost,0,1,"");
										}catch(SQLException e) {
											updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
											// update balance add 
											errorCode="11";
											errorResponse="Inserting credentials failed";
											e.printStackTrace();
											LogWriter.LOGGER.severe(e.getMessage());
											bubbleDS.closePreparedStatement();
											//new UserDBOperations().deleteUsersEntry(userId); //only deletes from users table
										}catch(Exception de) {
											updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
											errorCode="-10";
											errorResponse="other exception";
											bubbleDS.closePreparedStatement();
											LogWriter.LOGGER.info("other Exception:"+de.getMessage());
										}
									}else {
										errorCode="-16";
										errorResponse="Payment Deduction Failed";
									}
								}catch(SQLException e){
									errorCode= "-2";
									errorResponse="SQLException";
									LogWriter.LOGGER.severe(e.getMessage());
								}catch(Exception e){
									errorCode= "-3";
									errorResponse="General Exception";
									LogWriter.LOGGER.severe(e.getMessage());
									e.printStackTrace();
								}
							}else {
								errorCode="5";
								errorResponse="low Balance";
							}
						}else {
							errorCode="12";
							errorResponse="Invalid Msisdn";
						}
							if(errorCode.equals("0")) {
								successCount++;
								successMSISDNList=successMSISDNList+msisdnListArray[i]+",";
							}else{							
								failCount++;
								failedMSISDNList=failedMSISDNList+msisdnListArray[i]+",";
							}
						}
					}else {
						errorCode="-6";
						errorResponse="Sent sms not allowed";
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					if(bubbleDS.getConnection() != null){
						try {
							bubbleDS.closePreparedStatement();
						} catch (SQLException e) {
							errorCode="-4";
							errorResponse="SQLException while closing connection";
							LogWriter.LOGGER.severe(e.getMessage());
						}
					}  
				}
			}else {
				errorCode="-10";
				errorResponse="Customer Charging not Configured";
			}
		}else {
			errorCode="5";
			errorResponse="low Balance";
		}
		if(errorCodetmp == "0") {
			errorCode=errorCodetmp;
			errorResponse=errorResponsetmp;
		}

		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorResponse);
		//if(errorCode=="0") {
		failCount=msisdnCount-successCount;
		jsonEncoder.addElement("TotalMsisdnCount", Integer.toString(msisdnCount));
		jsonEncoder.addElement("SuccessCount", Integer.toString(successCount));	
		int lioS = successMSISDNList.lastIndexOf(",");
		if (lioS > 0)
			successMSISDNList = successMSISDNList.substring(0, lioS);
		jsonEncoder.addElement("SuccessMsisdnList", successMSISDNList);
		jsonEncoder.addElement("FailedCount", Integer.toString(failCount));
		int lioF = failedMSISDNList.lastIndexOf(",");
		if (lioF > 0)
			failedMSISDNList = failedMSISDNList.substring(0, lioF);
		jsonEncoder.addElement("FailedMsisdnList", failedMSISDNList);			
		//}
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
		
	}


	/**
	 * 
	 * @return String id
	 * @throws SQLException
	 */
	private String getlatestInsertId() throws SQLException {
		String retval = "-1";
		ResultSet rs = bubbleDS.getGeneratedKeys();
		if (rs.next()) {
			retval = rs.getString(1);
		}
		rs.close();
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param broadcastType
	 * @param msisdnCount
	 * @return id failed -1 otherwise insert id of broadcast_log
	 */
	private String createInsertId(String userId,int broadcastType,int msisdnCount) {
		String id="-1";
		String sql="INSERT INTO smsdb.broadcast_log" + 
				"(user_id,broadcast_type,msisdn_count) " + 
				"VALUES (?,?,?)";						
		try {
			bubbleDS.prepareStatement(sql,true);			
			bubbleDS.getPreparedStatement().setString(1,userId);
			bubbleDS.getPreparedStatement().setInt(2,broadcastType);
			bubbleDS.getPreparedStatement().setInt(3,msisdnCount);
			bubbleDS.execute();
			String groupid=getlatestInsertId();
			id=groupid;
			bubbleDS.closePreparedStatement();
		}catch(Exception e) {
			e.printStackTrace();
		}	
		return id;
	}

	/**
	 * 
	 * @param String message
	 * @return int number of sms count
	 */
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
	/**
	 * 	
	 * @param text
	 * @return boolean isUnicode
	 */
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
		count = (int) Math.ceil(textSize/153.00);		
		return count;
	}
	/**
	 * 
	 * @param text
	 * @return length of sms Text
	 */
	public int smsLength(String text) {
		System.out.println("Length of TEXT : "+text.length());
		return text.length();
	}
	/**
	 * 
	 * @param userid
	 * @param masking
	 * @return true if masking is associated with user
	 */
	public boolean isMakingAssociated(String userid, String masking) {
		//query using userid and masking, true only if it returns true for both 
		boolean retval = false;
		String output = "-1";
		String sql = "SELECT count(*) as counter FROM smsdb.masking_detail t where t.user_id=? and t.masking=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			bubbleDS.getPreparedStatement().setString(2, masking);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				output = bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			output = "-2";
		} finally {
			try {
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (output.matches("1"))
			retval = true;
		return retval;
	}

	/**
	 * 
	 * @param telcoPrefix
	 * @param masking
	 * @param caseSensitivity int 0=inSencitive 1=sencitive
	 * @return true if masking is allowed for that telcoPartner
	 */
	public boolean isMakingAvailableForTelcoPartner(String telcoPrefix, String masking, int caseSensitivity) {
		// add case sensitivity clause 
		//query using userid and masking, true only if it returns true for both 
		//  SQL upper case issue faced while trying to check irrespective of masking case
		boolean retval = false;
		String sql = "SELECT count(*) as counter FROM smsdb.masking_detail t where t.telco_prefix=? and t.masking=?";
		if(caseSensitivity==1) {
			masking=masking.toUpperCase();
			sql="SELECT count(*) as counter FROM smsdb.masking_detail t where t.telco_prefix=? and upper(t.masking)=?";
		}
		String output = "-1";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, telcoPrefix);
			bubbleDS.getPreparedStatement().setString(2, masking);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				output = bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			output = "-2";
		} finally {
			try {
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (output.matches("1"))
			retval = true;
		return retval;
	}



	/**
	 * 
	 * @param userid
	 * @param aparty
	 * @return true if input aparty(sender) is associated with user
	 */
	public boolean isSenderAssociatedWithUser(String userid, String aparty) {
		//Pending 
		//TODO query using userid and masking, true only if it returns true for both 
		boolean retval = false;
		String output = "-1";
		String sql = "SELECT count(*) as counter FROM smsdb.customer_balance t where t.user_id=? and t.aparty=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			bubbleDS.getPreparedStatement().setString(2, aparty);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				output = bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			output = "-2";
		} finally {
			try {
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (output.matches("1"))
			retval = true;
		return retval;
	}

	/**
	 * 
	 * @param input
	 * @return true if it is a masking listed in bubble db
	 */
	public boolean ifmasking(String aparty) {
		boolean retval = false;
		String output = "-1";
		String sql = "SELECT count(*) as counter FROM smsdb.masking_detail t where t.masking=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, aparty);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				output = bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			output = "-2";
		} finally {
			try {
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (output.matches("1"))
			retval = true;
		return retval;
	}
	/**
	 * 
	 * @param jsonDecoder
	 * @param id
	 * @return json containing error code.
	 * works for single sms only not bulk
	 */

	public JsonEncoder insertToSenderAPI(JsonDecoder jsonDecoder,String id) {
		JsonEncoder jsonEncoder=new JsonEncoder();
		UserDBOperations UDOp=new UserDBOperations(bubbleDS);
		String messageId="-1";
		Double smsCost=0.00;
		String errorCode="0005";//default errorCode
		//String userID=jsonDecoder.getJsonObject().getString("id"); 
		String userID=id;		
		double smsPrice=UDOp.getCustomerChargingDetailUDB(userID,1);//1=regular non masking charge
		if(smsPrice>0.0) {		
			String message=jsonDecoder.getJsonObject().getString("smsText");

			LogWriter.LOGGER.info("message: "+message);

			try {
				String userStatus=getUserType(userID);
				String aparty=getAparty(userID);
				int smsCount=getSMSSize(message);
				String telco =getTelcoDetail(aparty);
				//1=active customer 5=admin 10=high value customer
				if(userStatus.equals("1") || userStatus.equals("5") || userStatus.equals("10")) {

					double tmpSMSCost=smsPrice*smsCount;
					smsCost=(double)Math.round(tmpSMSCost * 100d) / 100d;

					//TODO checkBalance(userID) returns available balance
					String sqlInsert="INSERT INTO smsinfo"
							+ " (userid,message,bparty,source,source_id,aparty,telco_partner,cost,sms_count) "
							+ "VALUES (?, ?, ?,'API',? ,?,?,?,?)";
					if(getCustomerBalance(userID)>=smsPrice) {
						try {
							if(updateBalanceSingleSMS(userID,smsCount,smsPrice)) {
								try {
									bubbleDS.prepareStatement(sqlInsert,true);
									bubbleDS.getPreparedStatement().setString(1, userID);					
									bubbleDS.getPreparedStatement().setString(2, message);
									bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
									bubbleDS.getPreparedStatement().setString(4, NullPointerExceptionHandler.isNullOrEmpty(jsonDecoder.getJsonObject().getString("sourceId"))?"":jsonDecoder.getJsonObject().getString("sourceId"));
									bubbleDS.getPreparedStatement().setString(5, aparty);
									bubbleDS.getPreparedStatement().setString(6, telco); //telco_partner									
									bubbleDS.getPreparedStatement().setDouble(7, smsCost);
									bubbleDS.getPreparedStatement().setInt(8, smsCount);
									bubbleDS.execute();
									messageId=getMessageId();									
									errorCode="0000";					
									bubbleDS.closePreparedStatement();
									UDOp.transaction_logger(userID,smsCost,0,1,"");
								}catch(Exception e) {
									updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
									//errorCode="11:Inserting credentials failed";
									errorCode="0014";
									e.printStackTrace();
									LogWriter.LOGGER.severe(e.getMessage());
									bubbleDS.closePreparedStatement();
								}
							}else {
								//errorCode="-16: Payment Deduction Failed";
								errorCode="0011";
							}
						}catch(Exception e){
							errorCode="0005";
							LogWriter.LOGGER.severe(e.getMessage());
							e.printStackTrace();
						}
					}else {
						//errorCode="5:low Balance";
						errorCode="0013";
					}
				}else {
					errorCode="0012";
					//errorCode="-6:Sent sms not allowed";
				}
			}catch(Exception e){
				e.printStackTrace();
				errorCode="0005";
			}finally{
				if(bubbleDS.getConnection() != null){
					try {
						bubbleDS.closePreparedStatement();
					} catch (SQLException e) {
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}  
			}
		}else {
			//errorCode="-9:Customer Charging not Configured";
			errorCode="0015";
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		if(messageId !="-1")
			jsonEncoder.addElement("messageId", messageId);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}
	/**/
	/*	
	public JsonEncoder insertToSenderAPI(JsonDecoder jsonDecoder,String id) { //masking template added
		JsonEncoder jsonEncoder=new JsonEncoder();
		UserDBOperations UDOp=new UserDBOperations(bubbleDS);
		String messageId="-1";
		Double smsCost=0.00;
		String errorCode="0005";//default errorCode
		//String userID=jsonDecoder.getJsonObject().getString("id"); 
		String userID=id;		
		double smsPrice=UDOp.getCustomerChargingDetailUDB(userID,1);//1=regular non masking charge
		if(smsPrice>0.0) {		
			String message=jsonDecoder.getJsonObject().getString("smsText");

			LogWriter.LOGGER.info("message: "+message);

			try {
				String userStatus=getUserType(userID);
				String aparty=getAparty(userID);
				int smsCount=getSMSSize(message);
				String telco =getTelcoDetail(aparty);
				//1=active customer 5=admin 10=high value customer
				if(userStatus.equals("1") || userStatus.equals("5") || userStatus.equals("10")) {

					double tmpSMSCost=smsPrice*smsCount;
					smsCost=(double)Math.round(tmpSMSCost * 100d) / 100d;

					// checkBalance(userID) returns available balance
					String sqlInsert="INSERT INTO smsinfo"
							+ " (userid,message,bparty,source,source_id,aparty,telco_partner,cost,sms_count) "
							+ "VALUES (?, ?, ?,'API',? ,?,?,?,?)";
					if(getCustomerBalance(userID)>=smsPrice) {
						try {
							if(updateBalanceSingleSMS(userID,smsCount,smsPrice)) {
								try {
									bubbleDS.prepareStatement(sqlInsert,true);
									bubbleDS.getPreparedStatement().setString(1, userID);					
									bubbleDS.getPreparedStatement().setString(2, message);
									bubbleDS.getPreparedStatement().setString(3, this.msisdnNormalize(jsonDecoder.getEString("msisdn")));
									bubbleDS.getPreparedStatement().setString(4, NullPointerExceptionHandler.isNullOrEmpty(jsonDecoder.getJsonObject().getString("sourceId"))?"":jsonDecoder.getJsonObject().getString("sourceId"));
									bubbleDS.getPreparedStatement().setString(5, aparty);
									bubbleDS.getPreparedStatement().setString(6, telco); //telco_partner									
									bubbleDS.getPreparedStatement().setDouble(7, smsCost);
									bubbleDS.getPreparedStatement().setInt(8, smsCount);
									bubbleDS.execute();
									messageId=getMessageId();									
									errorCode="0000";					
									bubbleDS.closePreparedStatement();
									UDOp.transaction_logger(userID,smsCost,0,1,"");
								}catch(Exception e) {
									updateBalanceSingleSMS(userID,(-1)*smsCount,smsPrice);
									//errorCode="11:Inserting credentials failed";
									errorCode="0014";
									e.printStackTrace();
									LogWriter.LOGGER.severe(e.getMessage());
									bubbleDS.closePreparedStatement();
								}
							}else {
								//errorCode="-16: Payment Deduction Failed";
								errorCode="0011";
							}
						}catch(Exception e){
							errorCode="0005";
							LogWriter.LOGGER.severe(e.getMessage());
							e.printStackTrace();
						}
					}else {
						//errorCode="5:low Balance";
						errorCode="0013";
					}
				}else {
					errorCode="0012";
					//errorCode="-6:Sent sms not allowed";
				}
			}catch(Exception e){
				e.printStackTrace();
				errorCode="0005";
			}finally{
				if(bubbleDS.getConnection() != null){
					try {
						bubbleDS.closePreparedStatement();
					} catch (SQLException e) {
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}  
			}
		}else {
			//errorCode="-9:Customer Charging not Configured";
			errorCode="0015";
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		if(messageId !="-1")
			jsonEncoder.addElement("messageId", messageId);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}/***/

	/**
	 * 
	 * @return messageID for last inserted request 
	 * @throws SQLException
	 */
	private String getMessageId() throws SQLException {
		String retval="-1";
		ResultSet rs=bubbleDS.getGeneratedKeys();
		if(rs.next()) {
			retval=rs.getString(1);
		}
		rs.close();
		return retval;
	}
}




