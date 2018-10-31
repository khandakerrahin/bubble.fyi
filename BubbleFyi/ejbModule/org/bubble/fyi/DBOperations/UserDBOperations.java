/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonDecoder;
import org.bubble.fyi.Engine.JsonEncoder;
import org.bubble.fyi.Initializations.SecretKey;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;
import org.bubble.fyi.Utilities.RandomStringGenerator;

/**
 * @author wasif update information delete user/organization
 */
public class UserDBOperations {
	BubbleFyiDS bubbleDS;

	// private static final Logger LOGGER =
	// LogWriter.LOGGER.getLogger(UserDBOperations.class.getName());
	/**
	 * 
	 */
	public UserDBOperations(BubbleFyiDS bubbleDS) {
		this.bubbleDS = bubbleDS;
	}

	/**
	 *  transaction rollback in case of error
	 * 
	 * @param userId
	 * @return 0 success all negative values is error.
	 */
	public String deleteUser(String userId) {
		String retval = "-1";
		try {
			String sqlDeleteUser = "DELETE FROM users WHERE user_id=?";
			bubbleDS.prepareStatement(sqlDeleteUser);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			LogWriter.LOGGER.info("User deleted from users list.");
			String sqlDeleteOrgs = "DELETE FROM organizations WHERE user_id=?";
			bubbleDS.prepareStatement(sqlDeleteOrgs);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			LogWriter.LOGGER.info("User deleted.");
			retval = "0";
		} catch (SQLException e) {
			retval = "-2";
			LogWriter.LOGGER.severe("deleteUser(): " + e.getMessage());
		}
		return retval;
	}

	public String modifyUser(String userId, JsonDecoder modifications) {
		// TODO Modify user
		String retval = "-1";
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param oldPass
	 * @param newPass
	 * @return 0:Password set 1:User not found 2:Current password is invalid 3:Error
	 *         encountered while setting password -1: General Error -2: SQLException
	 *         -3: SQLException while closing connection
	 */
	public String modifyPasswordDB(String userId, String oldPass, String newPass) {
		// Modify password
		String retval = "-1";
		// check if old password matches
		// retrieve old password, keySeed
		String keySeed = "", passwd = "";
		// String sql="select
		// AES_DECRYPT(passwd_enc,concat_ws('',?,key_seed,key_seed,key_seed)) as passwd,
		// key_seed from tbl_users where id=?";
		String sql = "select password as passwd from tbl_users where id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				passwd = bubbleDS.getResultSet().getString(1);
			} else {
				retval = "1:User not found";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (oldPass.equals(passwd)) {
				// proceed to change passwd
				if (setNewPassword(userId, newPass)) {
					retval = "0:Password set";
					LogWriter.LOGGER.info("New password");
				} else {
					retval = "3:Error encountered while setting password";
					LogWriter.LOGGER.info("Error encountered while setting password.");
				}
			} else {
				// password didn't match
				if (retval.startsWith("-1")) {
					retval = "2:Current password is invalid";
					LogWriter.LOGGER.info("Current password did not match.");
				}
			}
		} catch (SQLException e) {
			retval = "-2";
			LogWriter.LOGGER.severe("modifyPassword(): " + e.getMessage());
		}
		return retval;
	}

	public String modifyProfileDB(String id, String city, String postCode, String address, String custodianName,
			String logoFile) {
		// Modify password
		String retval = "-1";
		String userId = "";
		// TODO need to change below logic
		String sql = "select id from tbl_users where id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				userId = bubbleDS.getResultSet().getString(1);
			} else {
				retval = "2:User not found";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (id.equals(userId)) {
				retval = updateProfile(userId, city, postCode, address, custodianName, logoFile);
				if (retval.equals("0")) {
					retval = fetchUserInfoProfileMod(userId).getJsonObject().toString();
				} else if (retval.equals("1")) {
					retval = "1:User with the email address or phone number or username  exists";
				}
			} else {
				// password didn't match
				retval = "2:User not found";
				LogWriter.LOGGER.info("user does not exists.");
			}
		} catch (SQLException e) {
			retval = "-2";
			LogWriter.LOGGER.severe("modifyProfileDB(): " + e.getMessage());
		}
		return retval;
	}

	public JsonEncoder fetchUserInfoProfileMod(String id) {
		JsonEncoder jsonEncoder = new JsonEncoder();
		String errorCode = "-1";// default errorCode
		// String sql="SELECT u.user_id, u.user_name, o.organization_name, u.user_email,
		// u.user_type, u.phone, u.status,
		// o.custodian_email,o.custodian_name,o.custodian_phone,o.organization_type,o.address,o.city,o.postcode
		// FROM users u left join organizations o on u.user_id=o.user_id where
		// u.<mode>=?";
		String sql = "SELECT u.id, u.custodian_name,u.address, u.organization_name, u.username,u.email, if(u.flag=5,'Admin','Customer') as user_type,u.flag, u.phone, u.postcode,u.city,u.logo_file FROM tbl_users u where u.id=?";

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
				if (!rs.getString("user_type").equals("Admin")) {
					jsonEncoder.addElement("custodian_name", rs.getString("custodian_name"));
					jsonEncoder.addElement("address", rs.getString("address"));
					jsonEncoder.addElement("organization_name", rs.getString("organization_name"));
					jsonEncoder.addElement("postcode", rs.getString("postcode"));
					jsonEncoder.addElement("city", rs.getString("city"));
					jsonEncoder.addElement("logoFileName", rs.getString("logo_file"));
				}

				errorCode = "0";
			} else {
				errorCode = "-9:User details could not be retrieved";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	/**
	 * 
	 * @param userId
	 * @param customerId
	 * @param Status
	 * @return
	 */
	public String modifyCustomerStatus(String userId, String customerId, String Status) {
		String retval = "-1";
		try {
			if (getUserTypeCustomerList(userId).equals("5")) {
				// proceed to change passwd
				if (!getUserTypeCustomerList(customerId).equals("5")) {
					if (setNewStatus(userId, customerId, Status)) {
						retval = "0:Customer Status Updated";
						LogWriter.LOGGER.info("New password");
					} else {
						retval = "3:Error encountered while Updating Status";
						LogWriter.LOGGER.info("Error encountered while updating Customer Status.");
					}
				} else {
					retval = "-8: User not Authorized update Admin status";
				}
			} else {
				// password didn't match
				if (retval.startsWith("-1")) {
					retval = "-7: User not Authorized to perform this action";
					LogWriter.LOGGER.info("Current password did not match.");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			LogWriter.LOGGER.info("Exception bubble.fyi.UserDBOperation.modifyCustomerStatus.");
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param customerId
	 * @param groupId
	 * @param Status
	 * @return
	 */
	public String modifyBulksmsPStatus(String userId, String customerId,String groupId, String Status){
		double smsPrice=getCustomerChargingDetailUDB(customerId,1);
		String organization=getOrganizationName(customerId);
		String adminName=getUserName(userId);
		String retval="-1";	
		try {
			if(getUserTypeCustomerList(userId).equals("5") ) {
				//proceed to change passwd
				if(!getUserTypeCustomerList(customerId).equals("5") ) {
					if(setNewBulksmsPStatus(customerId,groupId,"-1",Status,smsPrice,userId)) {
						retval="0:Customer Status Updated";
						// SEND EMAIL
						String repStatus="unknown";
						if(Status.equalsIgnoreCase("0")) {
							repStatus="Approved";
						}else{
							repStatus="Rejected";
						}
						String mailBody=repStatus+" by "+adminName+".\n"
								+ " Broadcast ID:"+groupId
								+ " \n  Company name: "+organization+".";

						new EmailSender(bubbleDS).sendEmailToGroup("1",mailBody);	
						//new EmailSender(bubbleDS).sendEmailToGroup("1",emailSubject,mailBody);	
					}else {
						retval="3:Error encountered while Updating Status";
						LogWriter.LOGGER.info("Error encountered while updating Customer Status.");
					}
				} else {
					retval = "-8: User not Authorized update Admin status";
				}
			} else {
				if (retval.startsWith("-1")) {
					retval = "-7: User not Authorized to perform this action";
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * finally{ } if(bubbleDS.getConnection() != null){ try {
		 * //bubbleDS.getConnection().close(); } catch (SQLException e) {
		 * retval="-4:connection close Exception";
		 * LogWriter.LOGGER.severe(e.getMessage()); } } }/
		 **/
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param recordId
	 * @param Status
	 * @param totalCost
	 * @param totalReach
	 * @return json ErrorCode:<code>, ErrorResponce:<Responce> 0 means successfully
	 *         updated -ve failed
	 */
	public JsonEncoder modifyTargetsmsPStatus(String userId, String recordId, String status, String totalCost,
			String totalReach) {
		String errorCode = "-1";
		String errorRes = "Customer Status Update failed";
		JsonEncoder jsonEncoder = new JsonEncoder();

		try {
			if (getUserTypeCustomerList(userId).equals("5")) {
				// TODO check if totalCost or totalReach is empty or not
				// ((expireTime.equals("null")||expireTime.startsWith("sysdate"))?expireTime:"to_date('"+expireTime+"','yyyymmddhh24miss')")
				if (checkIfCostOrReachEmpty(totalCost, totalReach)) {
					if (setNewTargetsmsPStatus(userId, recordId, status, totalCost, totalReach)) {
						errorCode = "0";
						errorRes = "Customer Status Updated";
					} else {
						errorCode = "3";
						errorRes = "Error encountered while Updating Status";
						LogWriter.LOGGER.info("Error encountered while updating Customer Status.");
					}
				} else {
					errorCode = "2";
					errorRes = "Total Reach or Total Count is empty or invalid";
				}
			} else {
				if (errorCode.startsWith("-1")) {
					errorCode = "-7";
					errorRes = "User not Authorized to perform this action";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponce", errorRes);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	/**
	 * 
	 * @param totalCost
	 * @param totalReach
	 * @return Boolean True if both totalCost & totalReach is not null or empty else
	 *         False
	 */
	public boolean checkIfCostOrReachEmpty(String totalCost, String totalReach) {
		boolean retval = false;
		double totalCostDbl = -1.00;
		int totalReachInt = -1;
		try {
			totalCostDbl = Double.parseDouble(totalCost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			totalReachInt = Integer.parseInt(totalReach);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!totalCost.isEmpty() && !totalCost.contains(null) && totalCostDbl > 0.0) {
			if (!totalReach.isEmpty() && !totalReach.contains(null) && totalReachInt > 0)
				retval = true;
		} else {
			retval = false;
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param recordId
	 * @param status
	 * @return
	 */
	public JsonEncoder modifyTargetsmsPStatusCustomer(String userId, String recordId, String status) {
		String errorCode = "-1";
		String errorRes = "Customer Status Update failed";
		JsonEncoder jsonEncoder = new JsonEncoder();
		String oldstatus = "1";
		if (status.equalsIgnoreCase("2")) {
			// Check balance deduct balance
			double customerbalance = getCustomerBalance(userId);
			double targetSMSCost = getTargetSMSCost(recordId);
			if (customerbalance >= targetSMSCost) {
				if (updateBalanceBulkSMS(userId, 1, targetSMSCost)) {
					if (setNewTragetsmsPStatusCustomer(userId, recordId, status, oldstatus)) {
						errorCode = "0";
						errorRes = "Customer Status Updated";
						transaction_logger(userId, targetSMSCost, 2, 1, "targetSMS:" + recordId);
					} else {
						updateBalanceBulkSMS(userId, -1, targetSMSCost);
						errorCode = "3";
						errorRes = "target SMS request update failed";
					}
				} else {
					errorCode = "-3";
					errorRes = "Customer balance deduction failed";
				}
			} else {
				double temp = targetSMSCost - customerbalance;
				errorCode = "6";
				errorRes = "low balance to send this SMS Broadcast. Please recharge " + temp + " or more and try again";
			}

		} else {
			try {
				if (setNewTragetsmsPStatusCustomer(userId, recordId, status, oldstatus)) {
					errorCode = "0";
					errorRes = "Customer Status Updated";
				} else {
					errorCode = "3";
					errorRes = "target SMS request update failed";
					LogWriter.LOGGER.info(
							"Error encountered while updating Customer Status. bubblefyi.UserDBOperation.modifyTargetsmsPStatusCustomer");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponce", errorRes);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	public String getTargetSMSPList(String id) {
		String retval = "";
		String errorCode = "-1";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);
			// needs to change to some other flag for sales rep now admin
			if (userFlag.equals("5")) {
				// String sql="SELECT t.user_id,t.group_id,u.custodian_name,u.organization_name,
				// t.`aparty`,(select COUNT(gsi.msisdn) from groupsms_sender gsi where
				// t.group_id=gsi.group_id) as msCount, t.`insert_date`,
				// t.`scheduled_date`,t.`flag`, t.`message`, t.`sms_count` FROM
				// `groupsms_sender_info` t, tbl_users u WHERE t.user_id=u.id and t.`flag`=?
				// order by t.`scheduled_date` asc";
				String sql = "SELECT t.id,u.custodian_name,u.organization_name,geo_target,demography_target,psychographic_target,behavioural_target,message,sms_count,budget,target_msisdn_count,DATE_FORMAT(t.schedule_date, \"%d-%m-%Y %H:%i\") as schedule_date FROM smsdb.targetSMS t,smsdb.tbl_users u WHERE t.user_id=u.id and t.flag=? order by t.id desc limit 0,20";
				try {
					bubbleDS.prepareStatement(sql);

					bubbleDS.getPreparedStatement().setInt(1, 0);
					// LogWriter.LOGGER.severe(" after prepared Statement");
					ResultSet rs = bubbleDS.executeQuery();
					while (rs.next()) {

						// tmpMsisdnCount=getMsisdnCountInGroup(rs.getString("group_id"));
						retval += "\"" + rs.getString("id") + "\"" + ",";
						retval += "\"" + rs.getString("custodian_name") + "\"" + ",";
						retval += "\"" + rs.getString("organization_name") + "\"" + ",";
						retval += "\"" + rs.getString("geo_target") + "\"" + ",";
						retval += "\"" + rs.getString("demography_target") + "\"" + ",";
						retval += "\"" + rs.getString("psychographic_target") + "\"" + ",";
						retval += "\"" + rs.getString("behavioural_target") + "\"" + ",";
						retval += "\"" + rs.getString("message") + "\"" + ",";
						retval += "\"" + rs.getString("sms_count") + "\"" + ",";
						retval += "\"" + rs.getString("budget") + "\"" + ",";
						retval += rs.getString("target_msisdn_count") + ",";
						retval += "\"" + rs.getString("schedule_date") + "\"" + ",";
						retval += "|";
					}
					bubbleDS.closeResultSet();
					bubbleDS.closePreparedStatement();
					if (NullPointerExceptionHandler.isNullOrEmpty(retval))
						retval = "0";
					int lio = retval.lastIndexOf("|");
					if (lio > 0)
						retval = retval.substring(0, lio);
					errorCode = "0";
					LogWriter.LOGGER.info("MapList : " + retval);
				} catch (SQLException e) {
					errorCode = "-2";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				} catch (Exception e) {
					errorCode = "-3";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			} else {
				errorCode = "-7: User not Authorized to perform this action";
			}
			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	public String getTargetSMSPListCustomer(String id) {
		String retval = "";
		String errorCode = "-1";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);
			// TODO needs to change to some other flag for sales rep now admin
			String sql = "SELECT id,geo_target,demography_target,psychographic_target,behavioural_target,message,sms_count,budget,target_msisdn_count,DATE_FORMAT(t.schedule_date, \"%d-%m-%Y %H:%i\") as schedule_date,actualCost,actualReach,flag FROM smsdb.targetSMS t WHERE t.user_id=? order by id desc limit 0,5";
			try {
				bubbleDS.prepareStatement(sql);

				bubbleDS.getPreparedStatement().setString(1, id);
				// TODO
				// LogWriter.LOGGER.severe(" after prepared Statement");
				ResultSet rs = bubbleDS.executeQuery();
				while (rs.next()) {

					// tmpMsisdnCount=getMsisdnCountInGroup(rs.getString("group_id"));
					retval += "\"" + rs.getString("id") + "\"" + ",";
					retval += "\"" + rs.getString("geo_target") + "\"" + ",";
					retval += "\"" + rs.getString("demography_target") + "\"" + ",";
					retval += "\"" + rs.getString("psychographic_target") + "\"" + ",";
					retval += "\"" + rs.getString("behavioural_target") + "\"" + ",";
					retval += "\"" + rs.getString("message") + "\"" + ",";
					retval += "\"" + rs.getString("sms_count") + "\"" + ",";
					retval += "\"" + rs.getString("budget") + "\"" + ",";
					retval += "\"" + rs.getString("target_msisdn_count") + "\"" + ",";
					retval += "\"" + rs.getString("schedule_date") + "\"" + ",";
					retval += "\"" + rs.getString("actualCost") + "\"" + ",";
					retval += "\"" + rs.getString("actualReach") + "\"" + ",";
					retval += "\"" + rs.getString("flag") + "\"";
					retval += "|";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
					retval = "0";
				int lio = retval.lastIndexOf("|");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
			}

			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	public JsonEncoder BulksmsLowBalanceStatusChange(String customerId, String groupId) {
		String errorCode = "-1";
		String newStatus = "-1";
		// TODO needs to change
		double smsPrice = 0.25;
		JsonEncoder jsonEncoder = new JsonEncoder();
		try {
			// check if group associated with user //also check the status //Check balance
			// //Update payment
			if (isGroupAssociatedWIthUserGTFSMS(customerId, groupId)) {
				int listMsCount = getSenderGroupMsisdnCount(groupId);
				LogWriter.LOGGER.info(" msisdn count: " + listMsCount);
				double customerBalance = getCustomerBalance(customerId);
				if (customerBalance >= (listMsCount * smsPrice)) {
					if (updateBalanceBulkSMS(customerId, listMsCount, smsPrice)) {
						if (getUserTypeCustomerList(customerId).equalsIgnoreCase("10")) {
							newStatus = "0";
						}
						if (updateBulksmsFromLowBalnceStatus(customerId, groupId, "5", newStatus, smsPrice)) {
							errorCode = "0";// :Customer Status Updated
						} else {
							errorCode = "3";// :Error encountered while Updating Status
							LogWriter.LOGGER.info("Error encountered while updating Customer Status.");
						}
					} else {
						errorCode = "-16";// payment deduction failed
					}
				} else {
					errorCode = "5"; // low balance
				}
			} else {
				errorCode = "1"; // group not associated with User
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;

	}

	public boolean isGroupAssociatedWIthUserGTFSMS(String userId, String groupId) {
		boolean retval = false;
		String output = "-1";
		String sql = "SELECT count(*) as counter FROM `groupsms_sender_info` t  where t.user_id=? and t.group_id=? and flag=5;";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.getPreparedStatement().setString(2, groupId);
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
				e.printStackTrace();
			}

		}
		if (output.matches("1"))
			retval = true;

		return retval;
	}

	public String addMsisdnToList(String msisdn, String listId, String field2,String field3) {

		String errorCode = "-1";
		String sql = "INSERT INTO group_msisdn_list" + " (list_id, msisdn,field2,field3) " + "VALUES (?, ?,?,?)";
		try {
			// json: name,email,phone,password
			bubbleDS.prepareStatement(sql, true);
			bubbleDS.getPreparedStatement().setString(1, listId);
			bubbleDS.getPreparedStatement().setString(2, msisdn);
			bubbleDS.getPreparedStatement().setString(3, field2);
			bubbleDS.getPreparedStatement().setString(4, field3);
			errorCode = "0:successfully Inserted";
			bubbleDS.execute();

		} catch (SQLIntegrityConstraintViolationException de) {
			errorCode = "1: Same listname Already exists";
			LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
		} catch (SQLException e) {
			errorCode = "11:Inserting user credentials failed";
			LogWriter.LOGGER.severe("SQLException" + e.getMessage());
		} catch (Exception e) {
			errorCode = "10:other Exception";
			e.printStackTrace();
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
				} catch (SQLException e) {
					errorCode = "-4:connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return errorCode;
	}

	public String deleteMsisdnFromList(String msisdn, String listId) {
		String errorCode = "-1";
		String sql = "delete FROM smsdb.group_msisdn_list where list_id=? and msisdn=? and id>0;";
		//TODO
		//String sql = "delete FROM smsdb.group_msisdn_list where list_id=? and msisdn like '%?%' and id>0;";
		try {
			// json: name,email,phone,password
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, listId);
			bubbleDS.getPreparedStatement().setString(2, msisdn);
			errorCode = "0:successfully Deleted";
			bubbleDS.execute();
		} catch (Exception e) {
			errorCode = "-1:Exception occured While Deleting";
			e.printStackTrace();
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
				} catch (SQLException e) {
					errorCode = "-4:connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		//TODO remove later . used for tracing 
		return errorCode;
	}

	public String deleteGroupFromList(String listId) {
		/*
		 * String sqlDelete="DELETE FROM students WHERE id=?";
		 * fsDS.prepareStatement(sqlDelete); fsDS.getPreparedStatement().setString(1,
		 * id); fsDS.execute(); fsDS.closePreparedStatement();
		 * LogWriter.LOGGER.info("Student deleted."); retval="0";
		 */

		String errorCode = "-1";
		String sql = "delete from smsdb.group_list where list_id=?;";
		try {
			// json: name,email,phone,password
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, listId);
			errorCode = "0:successfully Deleted";
			bubbleDS.execute();

		} catch (Exception e) {
			errorCode = "-1:Exception occured While Deleting";
			e.printStackTrace();
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					//// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode = "-4:connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return errorCode;
	}

	public boolean isMsisdnExistsInList(String msisdn, String listId) {
		boolean retval = false;
		int output = -1;

		String sql = "SELECT count(*) as counter FROM smsdb.group_msisdn_list t where t.msisdn=? and t.list_id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, msisdn);
			bubbleDS.getPreparedStatement().setString(2, listId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				//output = bubbleDS.getResultSet().getString(1);
				output = bubbleDS.getResultSet().getInt(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			output = -2;
		} finally {
			try {
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	//	if (output.matches("1"))
		//TODO
		if (output>0) {
			retval = true;
		}
		return retval;
	}

	public boolean ifListAssociatedWIthUser(String userId, String listId) {
		boolean retval = false;
		String output = "-1";
		String sql = "SELECT count(*) as counter FROM smsdb.group_list t where t.user_id=? and t.list_id=?;";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.getPreparedStatement().setString(2, listId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				output = bubbleDS.getResultSet().getString(1);
			} /*
			 * else { retval="1:User not found"; }/
			 **/
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
				e.printStackTrace();
			}
		}
		if (output.matches("1"))
			retval = true;

		return retval;
	}

	public String uploadLogoFileDB(String logoFile, String userid) {
		String retval = "-1";
		String sqlUpdateUser = "UPDATE tbl_users u set u.logo_file=? WHERE u.id=? ";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, logoFile);
			bubbleDS.getPreparedStatement().setString(2, userid);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = "0: successfully uploaded";
		} catch (SQLException e) {
			e.printStackTrace();
			LogWriter.LOGGER.severe("uploadLogoFileDB(): " + e.getMessage());
		}
		return retval;
	}
/**
 * 
 * @param userId
 * @param msisdn
 * @param flag
 * @param listId
 * @return flag=1 add flag=2 delete flag=3 edit
 */
	public String modifyGrouplistDB(String userId, String msisdn, String editedMsisdn,String flag, String listId,String field2,String field3) {

		// msisdn=msisdnNormalize(msisdn);
		String retval = "-1:Action failed";
		String output = "-1";
		try {
			if (flag.equals("1")) {
				if (ifListAssociatedWIthUser(userId, listId)) {
					//Normalize msisdn yet to be decided since format shown on web is same as stored in database
					if (!isMsisdnExistsInList(msisdn, listId)) {
						output = addMsisdnToList(msisdn, listId,field2,field3);
						if (output.startsWith("0")) {
							retval = "0:successfully Added";
						} else {
							retval = "-2:msisdn Add failed";
						}
					} else {
						retval = "2:Msisdn already exists";
					}
				} else {
					retval = "3:List not associated With User";
				}
			} else if (flag.equals("2")) {
				//  DELETE
				if (ifListAssociatedWIthUser(userId, listId)) {
					if (isMsisdnExistsInList(msisdn, listId)) {
						output = deleteMsisdnFromList(msisdn, listId);
						if (output.startsWith("0")) {
							//TODO insert into modified_number_list
							retval = "0:successfully Deleted";
						} else {
							retval = "-2: msisdn Delete failed";
						}
					} else {
						retval = "2: Msisdn does not exists in list";
					}
				} else {
					retval = "-3: List not associated With User";
				}
			} else if (flag.equals("3")) {
					//  Edit number in list 
					if (ifListAssociatedWIthUser(userId, listId)) {
						if (isMsisdnExistsInList(msisdn, listId)) {
							//editedMsisdn
							output = deleteMsisdnFromList(msisdn, listId);							
							if (output.startsWith("0")) {
								output =addMsisdnToList(editedMsisdn, listId,field2,field3);
								if (output.startsWith("0")) {
									retval = "0:successfully updated";
									//TODO insert into modified_number_list
								} else {
									retval = "-2:msisdn update failed";
								}
							} else {
								retval = "-2:msisdn Delete failed";
							}
						} else {
							retval = "2:Msisdn does not exists in list";
						}
					} else {
						retval = "-3:List not associated With User";
					}
				}else {
				retval = "-1: invalid request";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * TODO step 1: check if add/delete if add than check if msisdn already exists
		 * or not if no then add for delete if number exists then delete other wise
		 * reply
		 */
		/*
		 * if(getUserTypeCustomerList(userId).equals("5") ) { //proceed to change passwd
		 * if(!getUserTypeCustomerList(customerId).equals("5") ) {
		 * if(setNewBulksmsPStatus(customerId,groupId,"-1",Status)) {
		 * retval="0:Customer Status Updated"; LogWriter.LOGGER.info("New password");
		 * }else { retval="3:Error encountered while Updating Status";
		 * LogWriter.LOGGER.info("Error encountered while updating Customer Status."); }
		 * }else { retval="-8: User not Authorized update Admin status"; } }else {
		 * //password didn't match if(retval.startsWith("-1")) {
		 * retval="-7: User not Authorized to perform this action";
		 * LogWriter.LOGGER.info("Current password did not match."); }
		 * 
		 * }/
		 **/
		return retval;
	}

	public String deleteGrouplistDB(String userId, String listId) {

		String retval = "-1:Action failed";
		String output = "-1";
		// TODO DELETE
		if (ifListAssociatedWIthUser(userId, listId)) {
			output = deleteGroupFromList(listId);
			if (output.startsWith("0")) {
				retval = "0:successfully Deleted";
			} else {
				retval = "-2:Delete failed";
			}
		} else {
			retval = "-3:List not associated With User";
		}

		/**
		 * TODO step 1: check if add/delete if add than check if msisdn already exists
		 * or not if no then add for delete if number exists then delete other wise
		 * reply
		 */
		/*
		 * if(getUserTypeCustomerList(userId).equals("5") ) { //proceed to change passwd
		 * if(!getUserTypeCustomerList(customerId).equals("5") ) {
		 * if(setNewBulksmsPStatus(customerId,groupId,"-1",Status)) {
		 * retval="0:Customer Status Updated"; LogWriter.LOGGER.info("New password");
		 * }else { retval="3:Error encountered while Updating Status";
		 * LogWriter.LOGGER.info("Error encountered while updating Customer Status."); }
		 * }else { retval="-8: User not Authorized update Admin status"; } }else {
		 * //password didn't match if(retval.startsWith("-1")) {
		 * retval="-7: User not Authorized to perform this action";
		 * LogWriter.LOGGER.info("Current password did not match."); }
		 * 
		 * }/
		 **/
		return retval;
	}

	public String createGroupInfo(String userId, String listName) {
		String errorCode = "-1";
		String sql = "INSERT INTO group_list" + " (list_name, user_id) " + "VALUES (?, ?)";
		try {
			// json: name,email,phone,password
			bubbleDS.prepareStatement(sql, true);
			bubbleDS.getPreparedStatement().setString(1, listName);
			bubbleDS.getPreparedStatement().setString(2, userId);
			errorCode = "0:Successfully Inserted";
			bubbleDS.execute();
		} catch (SQLIntegrityConstraintViolationException de) {
			errorCode = "1: Same listname Already exists";
			LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
		} catch (SQLException e) {
			errorCode = "11:Inserting user credentials failed";
			LogWriter.LOGGER.severe("SQLException" + e.getMessage());
		} catch (Exception e) {
			errorCode = "10:other Exception";
			e.printStackTrace();
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode = "-4:connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}

		// LogWriter.LOGGER.info("UserID:"+userId);
		/*
		 * try { bubbleDS.closePreparedStatement(); } catch (SQLException e) { 
		 * Auto-generated catch block e.printStackTrace(); }/
		 **/

		return errorCode;
	}

	public String getBulkSMSRequestStatus(String groupId) {
		String retval = "-1";
		String sql = "SELECT flag FROM `groupsms_sender_info` WHERE group_id=?";
		if (retval.startsWith("-1")) {
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, groupId);
				bubbleDS.executeQuery();
				if (bubbleDS.getResultSet().next()) {
					retval = bubbleDS.getResultSet().getString(1);
				} /*
				 * else { retval="1:User not found"; }/
				 **/
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				retval = "-2";
				LogWriter.LOGGER.severe("getUserType(): " + e.getMessage());
			}
		}
		return retval;
	}

	public JsonEncoder GetSMSBulkCounter(String id, String listId) {
		JsonEncoder jsonEncoder = new JsonEncoder();

		String errorCode = "-1";// default errorCode
		String allSMSCount = "-1";
		String processedCount = "-1";
		try {
			String tempUid = isListTaggedUid(listId);

			if (tempUid.equals(id)) {
				String requestStatus = getBulkSMSRequestStatus(listId);

				String sql = "select p.counter1 as TotalCount, q.counter2  as Completed from ((SELECT count(*) as counter1  FROM `groupsms_sender` t1 WHERE t1.group_id=?) p \r\n"
						+ ",  (SELECT count(*) as counter2  FROM `groupsms_sender` t2 WHERE t2.group_id=? and t2.flag=1) q);";
				try {
					bubbleDS.prepareStatement(sql);
					bubbleDS.getPreparedStatement().setString(1, listId);
					bubbleDS.getPreparedStatement().setString(2, listId);
					ResultSet rs = bubbleDS.executeQuery();
					if (rs.next()) {
						allSMSCount = rs.getString(1);
						processedCount = rs.getString(2);
					}
					errorCode = "0";
					rs.close();
				} catch (SQLException e) {
					errorCode = "-2";
					LogWriter.LOGGER.severe(e.getMessage());
				} catch (Exception e) {
					errorCode = "-3";
					LogWriter.LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
				if (errorCode.equals("0")) {
					try {
						jsonEncoder.addElement("totalCount", allSMSCount);
						jsonEncoder.addElement("processed", processedCount);
						jsonEncoder.addElement("requestStatus", requestStatus);
						errorCode = "0";
					} catch (Exception e) {
						errorCode = "-3";
						LogWriter.LOGGER.severe(e.getMessage());
						e.printStackTrace();
					}
				}
			} else {
				errorCode = "9:User is not associated with requested list";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public JsonEncoder GetSMSCounter(String id) {
		JsonEncoder jsonEncoder = new JsonEncoder();
		String allSMSCount = getTotalSMSCount(id);
		String sTotalCount = getTotalSuccessCount(id);
		String smsRemaining = getCustomersRemainingBalance(id);
		String sLast7daysCount = "-1";// getLast7DaysSMSCount(id);
		String sLast30daysCount = "-1";// getLast30DaysSMSCount(id);
		String sTodaysSMSCount = "-1";// getTodaysSMSCount(id);
		String customerbalance = "" + getCustomerBalance(id);
		String totalContact = "0";
		String todayAllSMSCount = getTodayAllSMSCount(id);
		String errorCode = "-1";// default errorCode
		try {
			jsonEncoder.addElement("allSMSCount", allSMSCount);
			jsonEncoder.addElement("allSuccessCount", sTotalCount);
			jsonEncoder.addElement("todayAllSMSCount", todayAllSMSCount);
			jsonEncoder.addElement("todaySuccessSMSCount", sTodaysSMSCount);
			jsonEncoder.addElement("last7daysCount", sLast7daysCount);
			jsonEncoder.addElement("last30daysCount", sLast30daysCount);
			jsonEncoder.addElement("smsRemaining", smsRemaining);
			jsonEncoder.addElement("customerBalance", customerbalance);
			jsonEncoder.addElement("totalContact", totalContact);
			errorCode = "0";
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} /*
		 * finally{ if(bubbleDS.getConnection() != null){ try {
		 * bubbleDS.getConnection().close(); } catch (SQLException e) { errorCode="-4";
		 * LogWriter.LOGGER.severe(e.getMessage()); } } }/
		 **/
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	public String getTotalSMSCount(String userid) {
		String count = "-1";
		/*
		 * String sql="select ifnull(sum(p.counter),0) as sumC from (\r\n" +
		 * "select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n" +
		 * "where t.userid=? and t.sms_count is not null\r\n" +
		 * " group by t.userid,t.sms_count ) p;";/
		 **/
		/*
		 * String sql="select ifnull(sum(s.sumC+r.sumBulk),0) as sumAll from \r\n" +
		 * "(select \r\n" + "ifnull(sum(q.bulkCounter),0) as sumBulk from\r\n" +
		 * "(SELECT p.msisdn_count*p.sms_count as bulkCounter FROM smsdb.groupsms_sender_info p where p.user_id=? and p.flag=1\r\n"
		 * + "and p.msisdn_count is not null group by p.group_id,p.sms_count) q) r,\r\n"
		 * + "(select \r\n" + "ifnull(sum(p.counter),0) as sumC from \r\n" +
		 * "(select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n" +
		 * "where t.userid=? and t.sms_count is not null group by t.userid,t.sms_count ) p) s;"
		 * ;/
		 **/

		String sql = "select ifnull(sum(s.sumC+r.sumBulk+sdump.sumCdump),0) as sumAll from \r\n" + "(select \r\n"
				+ "ifnull(sum(q.bulkCounter),0) as sumBulk from\r\n"
				+ "(SELECT p.msisdn_count*p.sms_count as bulkCounter FROM smsdb.groupsms_sender_info p where p.user_id=? and p.flag=1\r\n"
				+ "and p.msisdn_count is not null group by p.group_id,p.sms_count) q) r,\r\n" + "(select \r\n"
				+ "ifnull(sum(p.counter),0) as sumC from \r\n"
				+ "(select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n"
				+ "where t.userid=? and t.sms_count is not null group by t.userid,t.sms_count ) p) s, \r\n"
				+ "(select ifnull(sum(q.counter),0) as sumCdump from \r\n "
				+ "(select count(*)*t.sms_count as counter  from smsdb.smsinfo_dump t \r\n"
				+ "where t.userid=? and t.sms_count is not null group by t.userid,t.sms_count ) q \r\n" + " ) sdump;";

		// TODO need to add smsinfo_dump
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			bubbleDS.getPreparedStatement().setString(2, userid);
			bubbleDS.getPreparedStatement().setString(3, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				count = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			count = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			count = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	/**
	 * 
	 * @param userid
	 * @return remaining sms count
	 */
	public String getCustomersRemainingBalance(String userid) {
		String count = "-1";
		String balance = "-1";

		double smsPrice = getCustomerChargingDetailUDB(userid, 1);
		String sql = "SELECT balance FROM `customer_balance` where user_id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				balance = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			balance = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			balance = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			double value = Double.parseDouble(balance);
			double countt = value / smsPrice;
			countt = Math.floor(countt);
			count = countt + "";
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return count;
	}

	public String getTotalSuccessCount(String userid) {
		String count = "-1";
		/*
		 * String sql="select ifnull(sum(p.counter),0) as sumC from (\r\n" +
		 * "select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n" +
		 * "where t.responseCode=0 and t.userid=? and t.sms_count is not null\r\n" +
		 * " group by t.userid,t.sms_count ) p;";/
		 **/

		String sql = "select ifnull(sum(s.sumC+r.sumBulk),0) as sumAll from \r\n" + "(select \r\n"
				+ "ifnull(sum(q.bulkCounter),0) as sumBulk from\r\n"
				+ "(SELECT (SELECT count(*) FROM smsdb.groupsms_sender gs where gs.response_code=0 and gs.group_id=p.group_id)*p.sms_count as bulkCounter FROM smsdb.groupsms_sender_info p where p.user_id=? and p.flag=1\r\n"
				+ "and p.msisdn_count is not null group by p.group_id,p.sms_count) q) r,\r\n" + "(select \r\n"
				+ "ifnull(sum(p.counter),0) as sumC from \r\n"
				+ "(select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n"
				+ "where t.responseCode=0 and  t.userid=? and t.sms_count is not null group by t.userid,t.sms_count ) p) s;";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			bubbleDS.getPreparedStatement().setString(2, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				count = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			count = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			count = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public String getTodaysSMSCount(String userid) {
		String count = "-1";
		/*
		 * String sql="select ifnull(sum(p.counter),0) as sumC from (\r\n" +
		 * "select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n" +
		 * "where DATE(t.insert_date) = CURDATE() and t.responseCode=0 and t.userid=? and t.sms_count is not null\r\n"
		 * + " group by t.userid,t.sms_count ) p;";/
		 */

		String sql = "select ifnull(sum(p.counter),0) as sumC from (\r\n"
				+ "select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n"
				+ "where (t.insert_date >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)) and t.responseCode=0 and t.userid=? and t.sms_count is not null\r\n"
				+ " group by t.userid,t.sms_count ) p;";
		/*
		 * select count(*)from smsdb.smsinfo t where t.responseCode=0 and t.userid=1;
		 */
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				count = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			count = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			count = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		return count;
	}

	public String getTodayAllSMSCount(String userid) {
		String count = "-1";
		String sql = "select ifnull(sum(s.sumC+r.sumBulk),0) as sumAll from \r\n" + "(select  \r\n"
				+ "ifnull(sum(q.bulkCounter),0) as sumBulk from \r\n"
				+ "(SELECT p.msisdn_count*p.sms_count as bulkCounter FROM smsdb.groupsms_sender_info p where (p.done_date >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)) and p.user_id=? and p.flag=1 \r\n"
				+ "and p.msisdn_count is not null group by p.group_id,p.sms_count) q) r, \r\n"
				+ "(select ifnull(sum(p.counter),0) as sumC from \r\n"
				+ "(select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n"
				+ "where (t.insert_date >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)) and t.userid=? and t.sms_count is not null group by t.userid,t.sms_count ) p) s;";
		/*
		 * select count(*)from smsdb.smsinfo t where t.responseCode=0 and t.userid=1;
		 */
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);

			bubbleDS.getPreparedStatement().setString(2, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				count = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			count = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			count = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public String getLast7DaysSMSCount(String userid) {
		String count = "-1";
		String sql = "select ifnull(sum(p.counter),0) as sumC from (\r\n"
				+ "select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n"
				+ "where (t.insert_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)) and t.responseCode=0 and t.userid=? and t.sms_count is not null\r\n"
				+ " group by t.userid,t.sms_count ) p;";
		/*
		 * select count(*)from smsdb.smsinfo t where t.responseCode=0 and t.userid=1;
		 */
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				count = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			count = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			count = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		return count;
	}

	public String getLast30DaysSMSCount(String userid) {
		String count = "-1";
		String sql = "select ifnull(sum(p.counter),0) as sumC from (\r\n"
				+ "select count(*)*t.sms_count as counter from smsdb.smsinfo t \r\n"
				+ "where (t.insert_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) and t.responseCode=0 and t.userid=? and t.sms_count is not null\r\n"
				+ " group by t.userid,t.sms_count ) p;";
		/*
		 * select count(*)from smsdb.smsinfo t where t.responseCode=0 and t.userid=1;
		 */
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userid);
			ResultSet rs = bubbleDS.executeQuery();
			if (rs.next()) {
				count = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			count = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			count = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		return count;
	}

	public String isListTaggedUid(String listId) {
		String retval = "-1";
		String sql = "SELECT `user_id` FROM `groupsms_sender_info` WHERE `group_id`=?";
		if (retval.startsWith("-1")) {
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, listId);
				bubbleDS.executeQuery();
				if (bubbleDS.getResultSet().next()) {
					retval = bubbleDS.getResultSet().getString(1);
				} /*
				 * else { retval="1:User not found"; }/
				 **/
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				retval = "-2";
				LogWriter.LOGGER.severe("isListTaggedUid(): " + e.getMessage());
			}
		}
		return retval;
	}

	public String isListTaggedUid_v2(String listId) {
		String retval = "-1";
		String sql = "SELECT `user_id` FROM `group_list` WHERE `list_id`=?";
		if (retval.startsWith("-1")) {
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, listId);
				bubbleDS.executeQuery();
				if (bubbleDS.getResultSet().next()) {
					retval = bubbleDS.getResultSet().getString(1);
				} /*
				 * else { retval="1:User not found"; }/
				 **/
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				retval = "-2";
				LogWriter.LOGGER.severe("isListTaggedUid(): " + e.getMessage());
			}
		}
		return retval;
	}

	public int getListMsisdnCount(String listId) {
		int retval = -1;
		String sql = "SELECT count(*) FROM `group_msisdn_list` WHERE `list_id`=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, listId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				retval = bubbleDS.getResultSet().getInt(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			retval = -2;
			LogWriter.LOGGER.severe("getListMsisdnCount(): " + e.getMessage());
		}

		return retval;
	}

	public int getSenderGroupMsisdnCount(String groupId) {
		// used for both from list/group bulksms and instant bulk sms ..it will count
		// from sender
		int retval = -1;
		String sql = "SELECT count(*) FROM `groupsms_sender` WHERE `group_id`=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, groupId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				retval = bubbleDS.getResultSet().getInt(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			retval = -2;
			LogWriter.LOGGER.severe("getListMsisdnCount(): " + e.getMessage());
		}

		return retval;
	}

	public double getGroupMsisdnCost(String groupId) {
		// used for both from list/group bulksms and instant bulk sms ..it will count
		// from sender
		double retval = -1;
		String sql = "SELECT cost FROM `groupsms_sender_info` WHERE `group_id`=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, groupId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				retval = bubbleDS.getResultSet().getDouble(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			retval = -2;
			LogWriter.LOGGER.severe("getListMsisdnCount(): " + e.getMessage());
		}

		return retval;
	}

	public double getCustomerBalance(String userId) {
		// TODO needs to update with price
		double retval = -1;
		String sql = "SELECT balance FROM `customer_balance` WHERE user_id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				retval = bubbleDS.getResultSet().getDouble(1);
			} else {
				retval = -2;
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval = -3;
			LogWriter.LOGGER.severe("getUserType(): " + e.getMessage());
		}
		return retval;
	}
	/*
	public String getEmailSubject(String id,String subject_id ) {
		//TODO needs to update with price
		String retval="";
		//double retval=-1;
		String sql="SELECT subject1 FROM smsdb.emailSenderDetail where id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				retval=bubbleDS.getResultSet().getString(1);
			}else {
				retval=-2;
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval=-3;
			LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
		}
		return retval;
	}/**/

	/**
	 * 
	 * @param userId String
	 * @param amount Double
	 * @param type   Int (single sms=0, bulkSMS=1, TargetSMS=2)
	 * @param action Int (charge=1 , refund=-1)
	 * @param remark String
	 */
	public void transaction_logger(String userId, Double amount, int type, int action, String remark) {

		LogWriter.LOGGER.info("transaction_logger- userId: " + userId + " amount:" + amount + " remark: " + remark);

		String sql = "INSERT INTO transaction_log" + " (user_id,amount,type,action,remark) " + "VALUES (?,?,?,?,?)";
		try {
			bubbleDS.prepareStatement(sql, true);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.getPreparedStatement().setDouble(2, amount);
			bubbleDS.getPreparedStatement().setInt(3, type);
			bubbleDS.getPreparedStatement().setInt(4, action);
			bubbleDS.getPreparedStatement().setString(5, remark);
			bubbleDS.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public double getTargetSMSCost(String recordId) {
		// TODO needs to update with price
		double retval = -1;
		String sql = "SELECT actualCost FROM smsdb.targetSMS where id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, recordId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				retval = bubbleDS.getResultSet().getDouble(1);
			} else {
				retval = -2;
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval = -3;
			LogWriter.LOGGER.severe("getUserType(): " + e.getMessage());
		}

		return retval;
	}

	/**
	 * 
	 * @param String userId
	 * @param String groupId
	 * @return String SMS text for group sms
	 */
	public String getGroupSMSText(String userId,String groupId) {
		//TODO needs to update with price
		String retval="";
		String sql="SELECT message FROM smsdb.groupsms_sender_info where user_id=? and group_id=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.getPreparedStatement().setString(1, groupId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				retval=bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {			
			LogWriter.LOGGER.severe("getGroupSMSText(): "+e.getMessage());
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param val
	 * @param smsPrice
	 * @return boolean after updating customer balance
	 */

	private boolean updateBalanceBulkSMS(String userId,int val,double smsPrice) {
		//TODO needs to update with cost
		boolean retval=false;
		//TODO needs to change for variable price
		double tmpAmount=smsPrice*val;
		//double amount=(double)Math.round(tmpAmount * 100d) / 100d;
		double amount=smsCostConverionCalculator(tmpAmount);

		LogWriter.LOGGER.info("amount(): "+amount);
		String sqlUpdateUser="UPDATE `customer_balance` SET balance = balance-(?) WHERE user_id=?";

		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setDouble(1, amount);
			bubbleDS.getPreparedStatement().setString(2, userId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("userId(): " + e.getMessage());
		}
		return retval;
	}

	/**
	 * 
	 * @param int msisdnCount
	 * @param int groupID
	 * @param int flag
	 * @return boolean true if updated successfully
	 * @throws SQLException
	 */
	public boolean updateGroupSMSinfoMsisdnCount(int msisdnCount, String groupID, int flag) {
		boolean retval = false;
		// TODO needs to use Prepared Statement

		String sqlUpdateUser = ("UPDATE `groupsms_sender_info` SET `msisdn_count`=?,flag=? WHERE `group_id` =?");
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setInt(1, msisdnCount);
			bubbleDS.getPreparedStatement().setInt(2, flag);
			bubbleDS.getPreparedStatement().setString(3, groupID);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = true;
		} catch (Exception e) {
			LogWriter.LOGGER.severe("updateGroupSMSinfoMsisdnCount(): " + e.getMessage());
		}
		return retval;
	}

	/**
	 * 
	 * @param cost
	 * @param groupID
	 * @return true if update successful 
	 */
	public boolean updateGroupSMSinfoCost(double cost, String groupID) {
		boolean retval = false;
		// needs to use Prepared Statement
		double amount=smsCostConverionCalculator(cost);

		String sqlUpdateUser = ("UPDATE `groupsms_sender_info` SET `cost`=? WHERE `group_id` =?");
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setDouble(1, amount);
			bubbleDS.getPreparedStatement().setString(2, groupID);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = true;
		} catch (Exception e) {
			LogWriter.LOGGER.severe("updateGroupSMSinfoMsisdnCount(): " + e.getMessage());
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @param chargeFor 1= regular Charge (non masking), 2= masking charge, 3=
	 *                  targetBased charge less than 3selection 4= targetBased
	 *                  charge more than 3 selection 5= inbox change 6=custom charge
	 * @return customer charged value for respected selection
	 */
	public double getCustomerChargingDetailUDB(String userId, int chargeFor) {
		// 1= regular Charge (non masking), 2= masking charge, 3= targetBased charge
		// less than 3selection
		// 4= targetBased charge more than 3 selection 5= inbox change 6=custom charge
		double retval = -1;
		double regular_charge = -1;
		double masking = -1;
		double targetBased_3 = -1;
		double targetBased_5 = -1;
		double inbox = -1;
		double custom = -1;

		String sql = "SELECT regular_charge,masking,targetBased_3,targetBased_5,inbox,custom FROM tbl_charging where user_id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				regular_charge = bubbleDS.getResultSet().getDouble(1);
				masking = bubbleDS.getResultSet().getDouble(2);
				targetBased_3 = bubbleDS.getResultSet().getDouble(3);
				targetBased_5 = bubbleDS.getResultSet().getDouble(4);
				inbox = bubbleDS.getResultSet().getDouble(5);
				custom = bubbleDS.getResultSet().getDouble(6);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			retval = -3;
			LogWriter.LOGGER.severe("getUserType(): " + e.getMessage());
		}
		if (chargeFor == 1)
			retval = regular_charge;
		else if (chargeFor == 2)
			retval = masking;
		else if (chargeFor == 3)
			retval = targetBased_3;
		else if (chargeFor == 4)
			retval = targetBased_5;
		else if (chargeFor == 5)
			retval = inbox;
		else if (chargeFor == 6)
			retval = custom;
		else
			retval = regular_charge;

		LogWriter.LOGGER.info("customer Charge: "+retval);
		return retval;
	}
	/**
	 * 
	 * @param userId
	 * @return String organization attached with the user id
	 * if organization not found it returns custodian name
	 */
	public String getOrganizationName(String userId) {
		String organization="";
		String custodian="";
		String retval="";
		String sql="SELECT custodian_name,organization_name FROM smsdb.tbl_users where id =?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				custodian=bubbleDS.getResultSet().getString(1);
				organization=bubbleDS.getResultSet().getString(2);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {			
			LogWriter.LOGGER.severe("getOrganizationName(): "+e.getMessage());
		}
		if(organization.isEmpty()) {
			retval=custodian;
		}else {
			retval=organization;
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @return custodian name from user table
	 */
	public String getUserName(String userId) {
		String custodian="";
		String sql="SELECT custodian_name FROM smsdb.tbl_users where id =?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.executeQuery();
			if(bubbleDS.getResultSet().next()) {
				custodian=bubbleDS.getResultSet().getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {			
			LogWriter.LOGGER.severe("getUserName(): "+e.getMessage());
		}

		return custodian;
	}

	/**
	 * 
	 * @param userId
	 * @return telco partner name RT BP BL GP TT
	 */
	public String getTelcoPartner(String userId) {
		String retval="";
		String sql="SELECT telco FROM smsdb.customer_balance where user_id =?";
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
			LogWriter.LOGGER.severe("getTelcoPartner(): "+e.getMessage());
		}

		return retval;
	}



	public JsonEncoder sendSMSFromList(String userId, String sch_date, String message, String listId) {
		String errorCode = "-1";
		String userFlag = "-1";
		String aparty = "";
		String telcoDetail = "";
		String updated_by = "-1";
		double smsPrice = getCustomerChargingDetailUDB(userId, 1);
		String organization=getOrganizationName(userId);
		String emailTemp="Approval Pending";

		int statusFlag = -1;
		JsonEncoder jsonEncoder = new JsonEncoder();
		// userID check with listID in group_list table
		userFlag = getUserTypeCustomerList(userId);

		// check if message is preApproved or not
		/*
		if (checkIfPreApproved(userId, message)) {
			statusFlag = 0;
			updated_by = "9999";
		}/**/
		// High value user segment
		if (userFlag.equalsIgnoreCase("10")) {
			statusFlag = 0;
			emailTemp="Auto Approved";
		}
		SMSSender ss = new SMSSender(bubbleDS);
		aparty = ss.getAparty(userId);
		if (!aparty.isEmpty())
			telcoDetail = ss.getTelcoDetail(aparty);
		/**/
		try {
			String tempUid = isListTaggedUid_v2(listId);

			if (tempUid.equals(userId)) {
				int listMsCount = getListMsisdnCount(listId);

				if(listMsCount>0) {
					//String emailSubject="BulkSMS Broadcast Request Approval Pending";

					//String mailBody="just a test email";
					int smsCount=getSMSSize(message);
					double customerBalance=getCustomerBalance(userId);
					double tmpSMSCost=listMsCount*smsPrice*smsCount;
					double smsCost = smsCostConverionCalculator(tmpSMSCost);

					if(customerBalance>=(smsCost)) {
						if(updateBalanceBulkSMS(userId,listMsCount*smsCount,smsPrice)) {	
							//count LIST maisdn, check with balance then deduct 

							if(sch_date.isEmpty()) { //INSTANT SMS	

								String sql="INSERT INTO groupsms_sender_info"
										+ " (user_id,message,sms_count,flag,msisdn_count,telco_partner,aparty,cost,update_by) "
										+ "VALUES (?,?,?,?,?,?,?,?,?)";						
								try {
									bubbleDS.prepareStatement(sql,true);			
									bubbleDS.getPreparedStatement().setString(1,userId);
									bubbleDS.getPreparedStatement().setString(2,message);
									bubbleDS.getPreparedStatement().setInt(3,smsCount);
									bubbleDS.getPreparedStatement().setInt(4,statusFlag);
									bubbleDS.getPreparedStatement().setInt(5,listMsCount);
									bubbleDS.getPreparedStatement().setString(6,telcoDetail);
									bubbleDS.getPreparedStatement().setString(7,aparty);
									bubbleDS.getPreparedStatement().setDouble(8,smsCost);
									bubbleDS.getPreparedStatement().setString(9,updated_by);
									bubbleDS.execute();
									String groupid=getNewGroupId();

									if(insertToGroupSMSSenderFromList(listId,groupid)) {
										LogWriter.LOGGER.info("insertToGroupSMSSenderFromList:"+listId +" :: groupid: "+groupid);
										errorCode="0";
										transaction_logger(userId,smsCost,1,1,"listId:"+listId);


										String mailBody=emailTemp+"\n Broadcast ID:"+groupid
												+ "\n Company name: "+organization+"\n Scheduled Date: instant SMS(approve now) \n Message text: "+message+" \n"
												+ "Number of Recipient:"+listMsCount;

										//new EmailSender(bubbleDS).sendEmailToGroup("1",emailSubject,mailBody);	
										new EmailSender(bubbleDS).sendEmailToGroup("1",mailBody);
									}else {
										updateBalanceBulkSMS(userId,(-1)*listMsCount*smsCount,smsPrice);
									}
									jsonEncoder.addElement("listId", groupid);

								} catch (SQLIntegrityConstraintViolationException de) {
									errorCode = "1";
									updateBalanceBulkSMS(userId, (-1) * listMsCount * smsCount, smsPrice);
									LogWriter.LOGGER
									.severe("SQLIntegrityConstraintViolationException:" + de.getMessage());
								} catch (SQLException e) {
									errorCode = "11";
									updateBalanceBulkSMS(userId, (-1) * listMsCount * smsCount, smsPrice);
									e.printStackTrace();
									LogWriter.LOGGER.severe("SQLException" + e.getMessage());
								} catch (Exception e) {
									e.printStackTrace();
									updateBalanceBulkSMS(userId, (-1) * listMsCount * smsCount, smsPrice);
									errorCode = "10";
									e.printStackTrace();
								}
								try {
									bubbleDS.closePreparedStatement();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}else { //SCHEDULED SMS
								/*String mailBody="SMS Broadcast Request is pending for approval.\n"
										+ "\n Company name: "+organization+" \n Scheduled Date:"+sch_date+" \n  Message text: "+message+" \n"
												+ " Number of Recipient:"+listMsCount+" \n SMS GROUP ID:"+groupId;; */
								String sql="INSERT INTO groupsms_sender_info"
										+ " (user_id,scheduled_date,message,sms_count,flag,msisdn_count,telco_partner,aparty,cost,update_by) "
										+ "VALUES (?,?,?,?,?,?,?,?,?,?)";

								// SELECT `group_id`, `user_id`, `aparty`, `msisdn_count`, `insert_date`,
								// `scheduled_date`, `flag`, `message`, `sms_count`, `done_date` FROM
								// `groupsms_sender_info` WHERE 1
								// get group id max of the table
								//  flag=0 means approved will automatically approved for trusted HV users

								try {
									// json: name,email,phone,password
									bubbleDS.prepareStatement(sql, true);
									bubbleDS.getPreparedStatement().setString(1, userId);
									bubbleDS.getPreparedStatement().setString(2, sch_date);
									bubbleDS.getPreparedStatement().setString(3, message);
									bubbleDS.getPreparedStatement().setInt(4, smsCount);
									bubbleDS.getPreparedStatement().setInt(5, statusFlag);
									bubbleDS.getPreparedStatement().setInt(6, listMsCount);
									bubbleDS.getPreparedStatement().setString(7, telcoDetail);
									bubbleDS.getPreparedStatement().setString(8, aparty);
									bubbleDS.getPreparedStatement().setDouble(9, smsCost);
									bubbleDS.getPreparedStatement().setString(10, updated_by);
									bubbleDS.execute();

									String groupid = getNewGroupId();
									if (insertToGroupSMSSenderFromList(listId, groupid)) {

										errorCode="0";
										String mailBody=emailTemp+"\n Broadcast ID:"+groupid
												+ "\n Company name: "+organization+"\n Scheduled Date:"+sch_date+" \n  Message text: "+message+" \n"
												+ "Number of Recipient:"+listMsCount;

										transaction_logger(userId,smsCost,1,1,"listId:"+listId);
										new EmailSender(bubbleDS).sendEmailToGroup("1",mailBody);	
									}else {
										updateBalanceBulkSMS(userId,(-1)*listMsCount*smsCount,smsPrice);
									}
									jsonEncoder.addElement("listId", groupid);

								} catch (SQLIntegrityConstraintViolationException de) {
									errorCode = "1";
									updateBalanceBulkSMS(userId, (-1) * listMsCount * smsCount, smsPrice);
									LogWriter.LOGGER
									.severe("SQLIntegrityConstraintViolationException:" + de.getMessage());
								} catch (SQLException e) {
									errorCode = "11";
									updateBalanceBulkSMS(userId, (-1) * listMsCount * smsCount, smsPrice);
									e.printStackTrace();
									LogWriter.LOGGER.severe("SQLException" + e.getMessage());
								} catch (Exception e) {
									e.printStackTrace();
									updateBalanceBulkSMS(userId, (-1) * listMsCount * smsCount, smsPrice);
									errorCode = "10";
									e.printStackTrace();
								}
								try {
									bubbleDS.closePreparedStatement();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						} else {
							errorCode = "-16";// : Payment Deduction Failed
						}
					} else {
						errorCode = "5";// :low Balance
					}
				} else {
					errorCode = "7"; // : Empty List
				}
			} else {
				errorCode = "9";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		// errorCode=jsonEncoder;
		return jsonEncoder;
	}

	public String smsDistributionProcessor(String userId, String groupId) {
		String retval = "";
		int flag;

		int msisdnCount = getSenderGroupMsisdnCount(groupId);
		double smsPrice = getCustomerChargingDetailUDB(userId, 1);
		String message = getGroupSMSText(userId, groupId);
		int smsCount = getSMSSize(message);
		double customerBalance = getCustomerBalance(userId);
		double tmpSMSCost = msisdnCount * smsPrice * smsCount;
		double smsCost = smsCostConverionCalculator(tmpSMSCost);

		// Check customer balance
		if (customerBalance >= (smsCost)) {
			// flag=-1 = new flag=0 =approved (only trusted HV users status=10) flag=5 for
			// low balance
			/*********************************/
			if (updateBalanceBulkSMS(userId, msisdnCount, smsPrice)) {
				if(updateGroupSMSinfoCost(smsCost, groupId)){			
					if (getUserTypeCustomerList(userId).equalsIgnoreCase("10")) {
						flag = 0;
						updateGroupSMSinfoMsisdnCount(msisdnCount, groupId, flag);
					} else {
						flag = -1;
						updateGroupSMSinfoMsisdnCount(msisdnCount, groupId, flag);
					}
				}else {
					LogWriter.LOGGER.info("updateGroupSMSinfoCost failed = groupId "+groupId);
				}				
			} else {
				flag = 5;
				updateGroupSMSinfoMsisdnCount(msisdnCount, groupId, flag);
			}
		} else {
			flag = 5;
			updateGroupSMSinfoMsisdnCount(msisdnCount, groupId, flag);
		}

		/*********************************/
		return retval;
	}

	/**
	 * 
	 * @param double tmpSMSCost
	 * @return double value till two decimal place; used for avoiding bizarre double
	 *         conversion
	 */
	public double smsCostConverionCalculator(double tmpSMSCost) {
		double d = -0.0;
		try {
			d = (double) Math.round(tmpSMSCost * 100d) / 100d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogWriter.LOGGER.info("smsCostConverionCalculator :input:output = "+tmpSMSCost+" : "+d);
		return d;
	}

	public JsonEncoder requestTargetBasedSMS(String userId, String geotarget, String demotarget, String psychotarget,
			String behaviourTarget, String sch_date, String message, String budget, String targetCount) {
		String errorCode = "-1";
		String errorResponse = "";
		JsonEncoder jsonEncoder = new JsonEncoder();
		// TODO userID check with listID in group_list table
		try {
			int smsCount = getSMSSize(message);
			String sql = "INSERT INTO targetSMS (user_id,schedule_date,message,geo_target,demography_target,psychographic_target,behavioural_target,budget,target_msisdn_count,sms_count) VALUES (?,?,?,?,?,?,?,?,?,?)";

			try {
				bubbleDS.prepareStatement(sql, true);
				bubbleDS.getPreparedStatement().setString(1, userId);
				bubbleDS.getPreparedStatement().setString(2, sch_date);
				bubbleDS.getPreparedStatement().setString(3, message);
				bubbleDS.getPreparedStatement().setString(4, geotarget);
				bubbleDS.getPreparedStatement().setString(5, demotarget);
				bubbleDS.getPreparedStatement().setString(6, psychotarget);
				bubbleDS.getPreparedStatement().setString(7, behaviourTarget);
				bubbleDS.getPreparedStatement().setString(8, budget);
				bubbleDS.getPreparedStatement().setString(9, targetCount);
				bubbleDS.getPreparedStatement().setInt(10, smsCount);
				bubbleDS.execute();
				errorCode = "0";
				errorResponse = "Successfully insterted";
			} catch (SQLException e) {
				errorCode = "11";
				errorResponse = "SQL Exception";
				// updateBalanceBulkSMS(userId,(-1)*totalBudget.intValue(),1.0);
				e.printStackTrace();
				LogWriter.LOGGER.severe("SQLException" + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				// updateBalanceBulkSMS(userId,(-1)*totalBudget.intValue(),1.0);
				errorCode = "10";
				errorResponse = "General Exception";
				e.printStackTrace();
			}
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorResponse);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	public JsonEncoder sendSMStargetBased(String userId, String targetDiv, String targetDis, String targetTUpazila,
			String sch_date, String message, String amount) {
		String errorCode = "-1";
		String errorResponse = "";
		String receiverCount = "-1";
		// String userFlag="-1";
		double smsPrice = 0.60;
		// int statusFlag=-1;
		JsonEncoder jsonEncoder = new JsonEncoder();
		// TODO userID check with listID in group_list table
		try {
			Double totalBudget = Double.parseDouble(amount);
			int smsCount = getSMSSize(message);
			int TotalSMS = (int) Math.floor(totalBudget / (smsPrice * smsCount));
			double customerBalance = getCustomerBalance(userId);
			if (customerBalance >= totalBudget) {
				if (updateBalanceBulkSMS(userId, totalBudget.intValue(), 1.0)) {
					// (int)Math.floor(1000/smsPrice)
					String sql = "INSERT INTO target_based_sms (user_id,schedule_date,message,target_division,budget,unit_cost,target_district,target_upazila) VALUES (?,?,?,?,?,?,?,?)";
					try {
						bubbleDS.prepareStatement(sql, true);
						bubbleDS.getPreparedStatement().setString(1, userId);
						bubbleDS.getPreparedStatement().setString(2, sch_date);
						bubbleDS.getPreparedStatement().setString(3, message);
						bubbleDS.getPreparedStatement().setString(4, targetDiv);
						bubbleDS.getPreparedStatement().setString(5, amount);
						bubbleDS.getPreparedStatement().setDouble(6, smsPrice);
						bubbleDS.getPreparedStatement().setString(7, targetDis);
						bubbleDS.getPreparedStatement().setString(8, targetTUpazila);
						bubbleDS.execute();
						errorCode = "0";
						receiverCount = "" + TotalSMS;
						errorResponse = "Successfully insterted";
					} catch (SQLException e) {
						errorCode = "11";
						errorResponse = "SQL Exception";
						updateBalanceBulkSMS(userId, (-1) * totalBudget.intValue(), 1.0);
						e.printStackTrace();
						LogWriter.LOGGER.severe("SQLException" + e.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
						updateBalanceBulkSMS(userId, (-1) * totalBudget.intValue(), 1.0);
						errorCode = "10";
						errorResponse = "General Exception";
						e.printStackTrace();
					}
					try {
						bubbleDS.closePreparedStatement();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					errorCode = "-16";
					errorResponse = "Payment Deduction Failed";
				}
			} else {
				errorCode = "5";
				errorResponse = "low Balance";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorResponse);
		jsonEncoder.addElement("ReceiverCount", receiverCount);

		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	public boolean insertToGroupSMSSenderFromList(String listId, String groupId) {
		boolean retval = false;
		try {
			PreparedStatement prep = bubbleDS.prepareStatement("insert into groupsms_sender (group_id,msisdn)\r\n"
					+ "SELECT  ?, `msisdn` FROM `group_msisdn_list` t WHERE  t.list_id=?");

			prep.setString(1, groupId);
			prep.setString(2, listId);
			prep.execute();
			retval = true;
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 
	 * @param aparty
	 * @return String telco name based on sender prefix RT BP
	 */
	// public String getTelcoDetail(String userId,String aparty) {
	/*
	 * public String getTelcoDetail(String aparty) { String retval="-1"; //TODO
	 * needs to change after mobile number portability comes
	 * if(aparty.startsWith("035") || aparty.startsWith("88035") ||
	 * aparty.startsWith("+88035")) { retval= "BP"; }else
	 * if(aparty.startsWith("+880444") || aparty.startsWith("880444") ) { retval=
	 * "RT"; }else { retval= "RT"; } return retval; }/
	 **/

	public JsonEncoder createGroupSMSInfo(String userId, String sch_date, String message, String filename) {
		JsonEncoder jsonEncoder = new JsonEncoder();

		// NEED to incorporate cost
		String errorCode = "-1";
		int smsCount = -1;
		smsCount = getSMSSize(message);
		String aparty = "";
		String telcoDetail = "";

		SMSSender ss = new SMSSender(bubbleDS);
		aparty = ss.getAparty(userId);
		if (!aparty.isEmpty())
			telcoDetail = ss.getTelcoDetail(aparty);
		try {
			if (sch_date.isEmpty()) {

				String sql = "INSERT INTO groupsms_sender_info" + " (user_id,message,sms_count,telco_partner,aparty) "
						+ "VALUES (?,?,?,?,?)";

				try {
					// json: name,email,phone,password
					bubbleDS.prepareStatement(sql, true);
					bubbleDS.getPreparedStatement().setString(1, userId);
					bubbleDS.getPreparedStatement().setString(2, message);
					bubbleDS.getPreparedStatement().setInt(3, smsCount);
					bubbleDS.getPreparedStatement().setString(4, telcoDetail);
					bubbleDS.getPreparedStatement().setString(5, aparty);
					errorCode = "0";// :Successfully Inserted
					boolean insertSuccess = false;

					bubbleDS.execute();
					String groupid = getNewGroupId();
					String retval = bubbleFileInsertInstant(filename, userId, groupid);
					if (!retval.equals("0")) {
						errorCode = retval;
					} else {
						jsonEncoder.addElement("id", userId);
						jsonEncoder.addElement("filename", filename);
						jsonEncoder.addElement("listId", groupid);
					}
					LogWriter.LOGGER.severe("groupid : " + groupid);
					insertSuccess = true;
				} catch (SQLIntegrityConstraintViolationException de) {
					errorCode = "1";// : Same listname Already exists
					LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
				} catch (SQLException e) {
					errorCode = "11";// :Inserting parameters failed
					e.printStackTrace();
					LogWriter.LOGGER.severe("SQLException" + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					errorCode = "10"; // :other Exception
					e.printStackTrace();
				}
			} else {
				String sql = "INSERT INTO groupsms_sender_info"
						+ " (user_id,scheduled_date,message,sms_count,telco_partner,aparty) " + "VALUES (?,?,?,?,?,?)";

				// SELECT `group_id`, `user_id`, `aparty`, `msisdn_count`, `insert_date`,
				// `scheduled_date`, `flag`, `message`, `sms_count`, `done_date` FROM
				// `groupsms_sender_info` WHERE 1
				// TODO get group id max of the table
				try {
					bubbleDS.prepareStatement(sql, true);
					bubbleDS.getPreparedStatement().setString(1, userId);
					bubbleDS.getPreparedStatement().setString(2, sch_date);
					bubbleDS.getPreparedStatement().setString(3, message);
					bubbleDS.getPreparedStatement().setInt(4, smsCount);
					bubbleDS.getPreparedStatement().setString(5, telcoDetail);
					bubbleDS.getPreparedStatement().setString(6, aparty);

					errorCode = "0";// :Successfully Inserted
					boolean insertSuccess = false;

					bubbleDS.execute();
					String groupid = getNewGroupId();
					String retval = bubbleFileInsertInstant(filename, userId, groupid);
					if (!retval.equals("0")) {
						errorCode = retval;
					} else {
						jsonEncoder.addElement("id", userId);
						jsonEncoder.addElement("filename", filename);
						jsonEncoder.addElement("listId", groupid);
					}
					LogWriter.LOGGER.severe("groupid : " + groupid);
					insertSuccess = true;
				} catch (SQLIntegrityConstraintViolationException de) {
					errorCode = "1"; // : Same listname Already exists
					LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
				} catch (SQLException e) {
					errorCode = "11"; // :Inserting parameters failed
					e.printStackTrace();
					LogWriter.LOGGER.severe("SQLException" + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					errorCode = "10"; // :other Exception
					e.printStackTrace();
				}
			}
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode = "-4"; // :connection close Exception
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		// LogWriter.LOGGER.info("UserID:"+userId);
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		// errorCode=jsonEncoder;

		return jsonEncoder;
	}
	/**
	 * 
	 * @param userid
	 * @param trxID
	 * @param price
	 * @param status =1 success -1 failed 0= new
	 * @return String "0: successfully updated" failed otherwise 
	 */
	public String updatePaymentInfoDB(String userid,String trxID,String price,String status) {
		String retval="-1";
		if(isUserAssociatedWithTrxID(userid,trxID)) {
			if(isPriceAssociatedwithTrxID(trxID,price)){
				String purchase_status=checkPaymentStatus(trxID);
				if(purchase_status.equalsIgnoreCase("0")) {		
					String sqlUpdateUser="UPDATE `payment_log` SET `purchase_status`=?,`process_date`= CURRENT_TIMESTAMP WHERE `id`=? and user_id=? and `price`=? and purchase_status=0";

					try {
						bubbleDS.prepareStatement(sqlUpdateUser);
						bubbleDS.getPreparedStatement().setString(1, status);
						bubbleDS.getPreparedStatement().setString(2, trxID);
						bubbleDS.getPreparedStatement().setString(3, userid);
						bubbleDS.getPreparedStatement().setString(4, price);
						bubbleDS.execute();
						bubbleDS.closePreparedStatement();
						retval="0: successfully updated";
						String output=updatePaymentInfoToCustomerBalance(userid,trxID,price,status);
						//TODO update payment log also update status
						//email for successfull purchase

						String organization=getOrganizationName(userid);
						String custodianName=getUserName(userid);
						String telcoPartner=getTelcoPartner(userid);
						if(output.equals("0")) {
							String mailBody="new Payment has been done by "+custodianName+" of "+organization+"\n"
									+ "\n Amount: "+price+" \n"
									+ " Sender Prefix :"+telcoPartner;
							new EmailSender(bubbleDS).sendEmailToGroup("3",mailBody);
						}

					}catch (SQLException e) {
						retval="-1: update failed";
						e.printStackTrace();
						LogWriter.LOGGER.severe("updatePaymentinfoDB(): " + e.getMessage());
					}
				} else {
					retval = "-2: purchase status could not be updated";
				}
			} else {
				retval = "-4: price does not associated with transaction ID";
			}
		} else {
			retval = "-3: user not associated with transaction ID";
		}
		return retval;
	}

	public boolean updatePaymentLog(String userID, String trxID, String price, String purchase_status, String oldFlag,
			String newFlag) {
		boolean retval = false;
		int output = -1;
		// LogWriter.LOGGER.info("userID:"+userID+" trxID:"+trxID+" price:"+price+"
		// oldFlag:"+oldFlag+" newFlag:"+newFlag);
		String sqlUpdateUser = "UPDATE `payment_log` SET `flag`=? WHERE `id`=? and user_id=? and `price`=? and purchase_status=? and flag=?";

		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, newFlag);
			bubbleDS.getPreparedStatement().setString(2, trxID);
			bubbleDS.getPreparedStatement().setString(3, userID);
			bubbleDS.getPreparedStatement().setString(4, price);
			bubbleDS.getPreparedStatement().setString(5, purchase_status);
			bubbleDS.getPreparedStatement().setString(6, oldFlag);
			output = bubbleDS.executeUpdate_v2();

			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			output = -1;
			e.printStackTrace();
			LogWriter.LOGGER.severe("updatePaymentinfoDB(): " + e.getMessage());
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
				} catch (SQLException e) {
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		if (output > 0)
			retval = true;
		return retval;
	}
	/**
	 * 
	 * @param userid
	 * @param trxID
	 * @param price
	 * @param status
	 * @return String 0 if payment success -1 if failed to update 
	 */
	public String updatePaymentInfoToCustomerBalance(String userid, String trxID, String price, String status) {
		String retval = "-1";
		if (status.equalsIgnoreCase("1")) {
			if (updatePaymentLog(userid, trxID, price, status, "0", "1")) {
				String sqlUpdateUser = "UPDATE `customer_balance` SET `balance`= balance +? WHERE user_id=?";
				try {
					bubbleDS.prepareStatement(sqlUpdateUser);
					bubbleDS.getPreparedStatement().setString(1, price);
					bubbleDS.getPreparedStatement().setString(2, userid);
					bubbleDS.execute();
					bubbleDS.closePreparedStatement();
					retval = "0";
					// update payment log also update status
				} catch (SQLException e) {
					retval = "-1: update failed";
					updatePaymentLog(userid, trxID, price, status, "-1", "1");
					e.printStackTrace();
					LogWriter.LOGGER.severe("updatePaymentinfoDB(): " + e.getMessage());
				}
			}
		} else {
			updatePaymentLog(userid, trxID, price, status, "0", "1");
		}
		return retval;
	}

	public boolean checkPackageName(String package_name) {
		boolean retval = false;
		String output = "";
		String sql = "select count(*) as counter from package_detail where package_name=? and status=1";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, package_name);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				output = rs.getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (Exception e) {
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (output.matches("1"))
			retval = true;
		return retval;

	}

	public JsonEncoder packagePurchaseRequester(String userId, String package_name, String price) {
		// needs to stop purchasing smse package within a timelimit
		// package not available check active packages
		JsonEncoder jsonEncoder = new JsonEncoder();
		String errorCode = "-1";

		try {
			if (checkPackageName(package_name)) {

				String sql = "INSERT INTO payment_log (`user_id`,`package_name`, `price`) VALUES (?,?,?);";

				try {
					bubbleDS.prepareStatement(sql, true);
					bubbleDS.getPreparedStatement().setString(1, userId);
					bubbleDS.getPreparedStatement().setString(2, package_name);
					bubbleDS.getPreparedStatement().setString(3, price);
					errorCode = "0:Successfully Inserted";
					bubbleDS.execute();
					String trxID = getNewGroupId();
					jsonEncoder.addElement("id", userId);
					jsonEncoder.addElement("trxID", trxID);

				} catch (SQLException e) {
					errorCode = "11:Inserting parameters failed";
					e.printStackTrace();
					LogWriter.LOGGER.severe("SQLException" + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					errorCode = "10:other Exception";
					e.printStackTrace();
				} finally {
					if (bubbleDS.getConnection() != null) {
						try {
							bubbleDS.closePreparedStatement();
							// bubbleDS.getConnection().close();
						} catch (SQLException e) {
							errorCode = "-4:connection close Exception";
							e.printStackTrace();
							LogWriter.LOGGER.severe(e.getMessage());
						}
					}
				}
			} else {
				errorCode = "21: Package does not exists";
			}
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode = "-4:connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}

		// LogWriter.LOGGER.info("UserID:"+userId);

		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();

		return jsonEncoder;
	}

	public JsonEncoder downloadSingleSMSReport(String userId, String start_date, String end_date, String output_type) {
		// needs to stop purchasing smse package within a timelimit
		// package not available check active packages
		JsonEncoder jsonEncoder = new JsonEncoder();
		String errorCode = "-1";
		String errorResponse = "";
		// String query="select t.source_id as ID,t.bparty as Receiver,t.aparty as
		// Sender,t.message as SMS_TEXT,t.sms_count,DATE_FORMAT(t.insert_date,
		// \"%d-%m-%Y %H:%i\") as insert_date,DATE_FORMAT(t.exec_date, \"%d-%m-%Y
		// %H:%i\") as exec_date,IFNULL(ELT(FIELD(t.responseCode,0, -3, -1,
		// -500),'Success','Invalid Receiver','Sending Error','Timed Out'),'failed') AS
		// Response FROM smsinfo t where t.userid="+userId+" and t.insert_date between
		// '"+start_date+"' and '" +end_date+"' ORDER BY `ID` desc;";
		String query = "SELECT  t.bparty AS Receiver,t.aparty AS Sender,t.message AS SMS_Content,t.Sms_Count, t.source_id AS SID,\r\n"
				+ "    DATE_FORMAT(t.insert_date, '%d-%m-%Y %h:%i %p') AS Insert_Date,DATE_FORMAT(t.exec_date, '%d-%m-%Y %h:%i %p') AS Exec_Date, \r\n"
				+ "    IFNULL(ELT(FIELD(t.responseCode, 0, - 3, - 1, - 500),'Processed','Invalid Receiver','Sending Error','Timed Out'),'failed') AS Response,\r\n"
				+ "	 IFNULL(ELT(FIELD(dr.delivery_status, 1, 0),'Delivered', 'Not Delivered'),'Status Unknown') AS Delivery_Status, \r\n"
				+ "	 Case when dr.operator='RANKSTEL' \r\n"
				+ "    then IFNULL(ELT(FIELD(dr.delivery_status, 0,1),'No Record',DATE_FORMAT(DATE_ADD(dr.delivery_time, INTERVAL 4 HOUR),'%d-%m-%Y %h:%i %p')),'No Record') \r\n"
				+ "    else IFNULL(DATE_FORMAT(dr.delivery_time,'%d-%m-%Y %h:%i %p') ,'No Record') \r\n"
				+ "    end AS Delivery_Time\r\n"
				+ "FROM  smsinfo t  left JOIN   delivery_reports dr ON t.ID = dr.message_id\r\n" + "WHERE  t.userid = '"
				+ userId + "'  AND t.insert_date between '" + start_date + "' and '" + end_date + "' \r\n"
				+ "ORDER BY t.ID DESC";
		try {

			String sql = "INSERT INTO file_dump_query (`user_id`,`query`, `output_type`) VALUES (?,?,?);";

			try {
				bubbleDS.prepareStatement(sql, true);
				bubbleDS.getPreparedStatement().setString(1, userId);
				bubbleDS.getPreparedStatement().setString(2, query);
				bubbleDS.getPreparedStatement().setString(3, output_type);
				errorCode = "0";
				errorResponse = "Successfully Inserted";
				bubbleDS.execute();

			} catch (SQLException e) {
				errorCode = "11";
				errorResponse = "Inserting parameters failed";
				e.printStackTrace();
				LogWriter.LOGGER.severe("SQLException" + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				errorCode = "10";
				errorResponse = "other Exception";
				e.printStackTrace();
			} finally {
				if (bubbleDS.getConnection() != null) {
					try {
						bubbleDS.closePreparedStatement();
						//// bubbleDS.getConnection().close();
					} catch (SQLException e) {
						errorCode = "-4";
						errorResponse = "Connection close Exception";
						e.printStackTrace();
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}
			}
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode = "-4";
					errorResponse = "Connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		// LogWriter.LOGGER.info("UserID:"+userId);

		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorResponse);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	public boolean isUserAssociatedWithTrxID(String userid, String trxID) {
		boolean retval = false;
		String output = "";
		String sql = "SELECT user_id FROM `payment_log` where id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, trxID);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				output = rs.getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (Exception e) {
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (output.equalsIgnoreCase(userid))
			retval = true;
		return retval;

	}

	public boolean isPriceAssociatedwithTrxID(String trxID, String price) {
		boolean retval = false;
		String output = "";
		String sql = "SELECT price FROM `payment_log` where id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, trxID);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				output = rs.getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (Exception e) {
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (output.equalsIgnoreCase(price))
			retval = true;
		return retval;

	}

	public String checkPaymentStatus(String trxId) {
		String retval = "-2";
		String sql = "SELECT purchase_status FROM `payment_log` where id=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, trxId);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval = rs.getString(1);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (Exception e) {
			LogWriter.LOGGER.severe(e.getMessage());
		}
		return retval;

	}

	public String bubbleFileInsertInstant(String filename, String id, String groupid) {
		String errorCode = "-1";// default errorCode
		String sqlInsert = "INSERT INTO bubble_file_info(file_name,user_id,groupID) VALUES(?,?,?)";
		int gId = Integer.parseInt(groupid);
		try {
			// json: file_name,school_id
			bubbleDS.prepareStatement(sqlInsert);
			bubbleDS.getPreparedStatement().setString(1, filename);
			bubbleDS.getPreparedStatement().setString(2, id);
			bubbleDS.getPreparedStatement().setInt(3, gId);
			try {
				bubbleDS.execute();
			} catch (SQLIntegrityConstraintViolationException de) {
				errorCode = "-1:duplicate filename";
				LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
			} catch (SQLException e) {
				errorCode = "-11:Inserting failed";
				e.printStackTrace();
				LogWriter.LOGGER.severe("SQLException" + e.getMessage());
			}
			// if(bubbleDS.getConnection() != null) bubbleDS.closePreparedStatement();
			if (errorCode.equals("-1"))
				errorCode = "0";
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		return errorCode;
	}

	private boolean setNewStatus(String userId, String customerId, String newflag) {
		boolean retval = false;
		String sqlUpdateUser = "UPDATE tbl_users u set u.flag=?,u.updated_by=?,u.updated_on= CURRENT_TIMESTAMP WHERE u.id=? and u.flag!='5'";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, newflag);
			bubbleDS.getPreparedStatement().setString(2, userId);
			bubbleDS.getPreparedStatement().setString(3, customerId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewStatus(): " + e.getMessage());
		}
		return retval;
	}

	/**
	 * 
	 * @param customerId
	 * @param groupId
	 * @param oldflag flag =0 approved 2 rejected by admin -1=new 1=completed
	 * @param newflag
	 * @param smsPrice
	 * @param userId
	 * @return boolean
	 * 
	 */
	private boolean setNewBulksmsPStatus(String customerId, String groupId, String oldflag, String newflag,
			double smsPrice, String userId) {
		boolean retval = false;
		// String message=getGroupSMSText(customerId,groupId);
		// int smsCount=getSMSSize(message);
		String sqlUpdateUser = "UPDATE `groupsms_sender_info` t SET `flag`= ? , update_by =? ,action_date= CURRENT_TIMESTAMP WHERE t.flag=? and t.`group_id`=? and t.`user_id`=?";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, newflag);
			bubbleDS.getPreparedStatement().setString(2, userId);
			bubbleDS.getPreparedStatement().setString(3, oldflag);
			bubbleDS.getPreparedStatement().setString(4, groupId);
			bubbleDS.getPreparedStatement().setString(5, customerId);
			bubbleDS.execute();
			retval=true;
			//groupId send email 
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewStatus(): " + e.getMessage());
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
				} catch (SQLException e) {
					retval = false;
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		// if rejected refund customer
		if (retval && newflag.equalsIgnoreCase("2")) {
			// int groupMsCount=getSenderGroupMsisdnCount(groupId);
			// updateBalanceBulkSMS(customerId,(-1)*groupMsCount,smsPrice);
			double cost = getGroupMsisdnCost(groupId);
			updateBalanceBulkSMS(customerId, (-1) * 1, cost);
		}

		return retval;
	}

	/**
	 * 
	 * @param customerId
	 * @param groupId
	 * @param oldflag
	 * @param newflag
	 * @param smsPrice
	 * @return boolean
	 */
	private boolean updateBulksmsFromLowBalnceStatus(String customerId, String groupId, String oldflag, String newflag,
			double smsPrice) {
		boolean retval = false;
		String sqlUpdateUser = "UPDATE `groupsms_sender_info` t SET `flag`= ? WHERE t.flag=? and t.`group_id`=? and t.`user_id`=?";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, newflag);
			bubbleDS.getPreparedStatement().setString(2, oldflag);
			bubbleDS.getPreparedStatement().setString(3, groupId);
			bubbleDS.getPreparedStatement().setString(4, customerId);
			bubbleDS.execute();

			retval = true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewStatus(): " + e.getMessage());
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					//// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					retval = false;
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		// if rejected refund customer
		if (retval && newflag.equalsIgnoreCase("2")) {
			int groupMsCount = getSenderGroupMsisdnCount(groupId);
			updateBalanceBulkSMS(customerId, (-1) * groupMsCount, smsPrice);
		}
		return retval;
	}

	private boolean setNewTargetsmsPStatus(String userID, String recordId, String status, String cost, String reach) {
		boolean retval = false;
		// String sqlUpdateUser="UPDATE `groupsms_sender_info` t SET `flag`= ? WHERE
		// t.flag=? and t.`group_id`=? and t.`user_id`=?";
		String sqlUpdate = "update targetSMS  set  actualCost=?, actualReach=?, approvedBy=?, approvedAt=CURRENT_TIMESTAMP,flag=? where id=? and flag=0";
		try {
			bubbleDS.prepareStatement(sqlUpdate);
			bubbleDS.getPreparedStatement().setString(1, cost);
			bubbleDS.getPreparedStatement().setString(2, reach);
			bubbleDS.getPreparedStatement().setString(3, userID);
			bubbleDS.getPreparedStatement().setString(4, status);
			bubbleDS.getPreparedStatement().setString(5, recordId);
			long recordCount = bubbleDS.executeUpdate();
			if (recordCount > 0)
				retval = true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewStatus(): " + e.getMessage());
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
				} catch (SQLException e) {
					retval = false;
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return retval;
	}

	private boolean setNewTragetsmsPStatusCustomer(String userID, String recordId, String status, String oldStatus) {
		boolean retval = false;
		// String sqlUpdateUser="UPDATE `groupsms_sender_info` t SET `flag`= ? WHERE
		// t.flag=? and t.`group_id`=? and t.`user_id`=?";
		String sqlUpdate = "update targetSMS  set  flag=? where id=? and user_id=? and flag =?";
		try {
			bubbleDS.prepareStatement(sqlUpdate);
			bubbleDS.getPreparedStatement().setString(1, status);
			bubbleDS.getPreparedStatement().setString(2, recordId);
			bubbleDS.getPreparedStatement().setString(3, userID);
			bubbleDS.getPreparedStatement().setString(4, oldStatus);
			long recordCount = bubbleDS.executeUpdate();
			if (recordCount > 0)
				retval = true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewTragetsmsPStatusCustomer(): " + e.getMessage());
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
				} catch (SQLException e) {
					retval = false;
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return retval;
	}

	private String updateProfile(String userId, String city, String postCode, String address, String custodianName,
			String logoFile) {
		String retval = "-1";

		String sqlUpdateUser = "UPDATE tbl_users SET custodian_name=?,address=?," + "postcode=?,city=?,logo_file=?"
				+ " WHERE id =?";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, custodianName);
			bubbleDS.getPreparedStatement().setString(2, address);
			bubbleDS.getPreparedStatement().setString(3, postCode);
			bubbleDS.getPreparedStatement().setString(4, city);
			bubbleDS.getPreparedStatement().setString(5, logoFile);
			bubbleDS.getPreparedStatement().setString(6, userId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = "0";
			;
		} catch (SQLIntegrityConstraintViolationException de) {
			retval = "1";
			LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());

		} catch (SQLException e) {
			retval = "2";
			LogWriter.LOGGER.severe("Profile Updated error general: " + e.getMessage());
		}
		return retval;
	}

	private String updateSMSApprovalStatus(String userID, String textID, String flag) {
		String retval = "-1";
		String sqlUpdateUser = "UPDATE tbl_preapproved_texts t SET t.flag=?,t.approved_by=?, t.approval_time=current_timestamp WHERE t.id =?";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, flag);
			bubbleDS.getPreparedStatement().setString(2, userID);
			bubbleDS.getPreparedStatement().setString(3, textID);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = "0";
			;
		} catch (SQLIntegrityConstraintViolationException de) {
			retval = "-1";
			LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());

		} catch (SQLException e) {
			retval = "-2";
			LogWriter.LOGGER.severe("Approval Status Update error general: " + e.getMessage());
		}
		return retval;
	}

	private boolean setNewPassword(String userId, String newPass) {
		boolean retval = false;
		String sqlUpdateUser = "UPDATE tbl_users u set u.password=? WHERE u.id=?";
		try {
			bubbleDS.prepareStatement(sqlUpdateUser);
			bubbleDS.getPreparedStatement().setString(1, newPass);
			bubbleDS.getPreparedStatement().setString(2, userId);
			bubbleDS.execute();
			bubbleDS.closePreparedStatement();
			retval = true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewPassword(): " + e.getMessage());
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @return String user type for customer 5= Admin, 10 =HV 1=Regular customer 0=not approved customer
	 */
	public String getUserTypeCustomerList(String userId) {
		String retval = "-1";
		String sql = "select flag from tbl_users where id=?";
		if (retval.startsWith("-1")) {
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, userId);
				bubbleDS.executeQuery();
				if (bubbleDS.getResultSet().next()) {
					retval = bubbleDS.getResultSet().getString(1);
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				retval = "-2";
				LogWriter.LOGGER.severe("getUserType(): " + e.getMessage());
			}
		}
		return retval;
	}

	/**
	 * 
	 * @param String userId
	 * @param String message
	 * @return boolean true if requested text is pre approved
	 */
	/*public boolean checkIfPreApproved(String userId, String message) {
		boolean retval = false;
		String sql = "select count(*) from tbl_preapproved_texts where userID=? and smsText=?";

		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.getPreparedStatement().setString(2, message);
			bubbleDS.executeQuery();
			if (bubbleDS.getResultSet().next()) {
				if (bubbleDS.getResultSet().getInt(1) > 0)
					retval = true;
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			LogWriter.LOGGER.severe("getUserType(): " + e.getMessage());
		}

		return retval;
	}/**/

	/**
	 * 
	 * 
	 * @param listType Admin/Parents/SpiderAdmin/all
	 * @param x        to distinguish from string return overload
	 * @return List of Parents and SpiderAdmin:
	 *         userId,username,email,phone,userType,status List of Admins:
	 *         userId,username,email,phone,userType,status,organizationName,custodianName,custodianEmail,custodianPhone,organizationType,city,postcode,address
	 *         <br>
	 *         u.user_id, u.user_name, u.user_email, u.phone, u.user_type, u.status,
	 *         o.organization_name,o.custodian_name,
	 *         o.custodian_email,o.custodian_phone,o.organization_type,o.city,o.postcode,o.address
	 */
	// for parents id,phone,email,name,otp,otp_expire_time,status
	public String getList(String id, String userType) {

		String retval = "";
		String errorCode = "-1";
		// String sql="SELECT u.user_id, u.user_name, u.user_email, u.phone,
		// u.user_type, u.status, o.organization_name,o.custodian_name,
		// o.custodian_email,o.custodian_phone,o.organization_type,o.city,o.postcode,o.address
		// FROM users u left join organizations o on u.user_id=o.user_id where
		// user_type=? order by user_id asc";
		String sqlAdmin = "SELECT t.aparty,t.bparty,t.message,t.sms_count,DATE_FORMAT(t.insert_date, \"%d-%m-%Y %h:%i %p\") as insert_date,t.flag,t.userid,DATE_FORMAT(t.exec_date, \"%d-%m-%Y %h:%i %p\") as exec_date,t.responseCode,t.source_id FROM smsinfo t ORDER BY `ID` ";
		String sql = "SELECT t.aparty,t.bparty,t.message,t.sms_count,DATE_FORMAT(t.insert_date, \"%d-%m-%Y %h:%i %p\") as insert_date,t.flag,t.userid,DATE_FORMAT(t.exec_date, \"%d-%m-%Y %h:%i %p\") as exec_date,t.responseCode,t.source_id FROM smsinfo t where t.userid=? ORDER BY `ID` desc limit 0,500";
		try {
			if (userType.equals("Admin")) {
				bubbleDS.prepareStatement(sqlAdmin);
				// ResultSet rs = bubbleDS.executeQuery();
			} else {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, id);
			}

			ResultSet rs = bubbleDS.executeQuery();

			while (rs.next()) {
				if (userType.equalsIgnoreCase("Admin")) {
					retval += rs.getString("aparty") + ",";
					// retval+="\""+rs.getString("organization_name")+"\""+",";
					retval += rs.getString("userid") + ",";
				}
				retval += rs.getString("bparty") + ",";
				// retval+="\""+rs.getString("message")+"\""+",";
				retval += "\"" + "" + "\"" + ",";
				retval += "\"" + rs.getString("insert_date") + "\"" + ",";
				retval += rs.getString("sms_count") + ",";
				retval += "\"" + rs.getString("exec_date") + "\"" + ",";
				retval += rs.getString("flag") + ",";
				retval += rs.getString("source_id") + ",";
				retval += rs.getString("responseCode");
				retval += "|";
			}
			rs.close();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get list --> " + retval);
		return retval;
	}

	public String getPaymentRecords(String id) {

		String retval = "";
		String errorCode = "-1";
		String sql = "SELECT DATE_FORMAT(insert_date, \"%d-%m-%Y %h:%i %p\") as insert_date,package_name,price,purchase_status,process_date FROM smsdb.payment_log where user_id=? ORDER BY `ID` desc limit 0,25";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			ResultSet rs = bubbleDS.executeQuery();

			while (rs.next()) {
				retval += "\"" + rs.getString("insert_date") + "\"" + ",";
				retval += rs.getString("package_name") + ",";
				retval += rs.getString("price") + ",";
				retval += rs.getString("purchase_status") + ",";
				retval += "\"" + rs.getString("process_date") + "\"";
				retval += "|";
			}
			rs.close();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get list --> " + retval);
		return retval;
	}

	/**
	 * 
	 * @param id
	 * @return String CSV
	 */
	public String getReportRecords(String id) {
		String retval = "";
		String errorCode = "-1";
		String sql = "SELECT IFNULL(FILE_NAME,'File Generation in Progress') as file_name,DATE_FORMAT(insert_date, \"%d-%m-%Y %h:%i %p\") as insert_date,status,DATE_FORMAT(dump_time, \"%d-%m-%Y %h:%i %p\") as dump_time,output_type FROM smsdb.file_dump_query where user_id=? and  (insert_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) order by id desc";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval += "\"" + rs.getString("file_name") + "\"" + ",";
				retval += "\"" + rs.getString("insert_date") + "\"" + ",";
				retval += rs.getString("status") + ",";
				retval += rs.getString("output_type") + ",";
				retval += "\"" + rs.getString("dump_time") + "\"";
				retval += "|";
			}
			rs.close();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get list --> " + retval);
		return retval;
	}

	public JsonEncoder getBulkSMSSummary(String id, String start_date, String end_date, String output_type) {

		JsonEncoder jsonEncoder = new JsonEncoder();
		String errorCode = "-1";
		String errorResponse = "";
		/*
		String query = "SELECT message as SMS_TEXT, sms_count as NumberOfSMS, IFNULL(ELT(FIELD(flag, -1,0,1,2,3,-2,5),'Approval Pending','Approved','SMS Sent','Rejected By admin','Cancelled By user','File not uploaded','low Balance'),'failed') AS Status, DATE_FORMAT(scheduled_date, '%d-%m-%Y %h:%i %p') AS scheduled_date, IFNULL(DATE_FORMAT(done_date, '%d-%m-%Y %h:%i %p'), 'Not Applicable') done_date,\r\n"
				+ "(SELECT COUNT(*) FROM  groupsms_sender t  WHERE t.group_id = u.group_id) TotalNumber,\r\n"
				+ "(SELECT COUNT(*) FROM groupsms_sender t WHERE t.group_id = u.group_id  AND t.response_code = 0) TotalSuccessCount\r\n"
				+ " FROM groupsms_sender_info u WHERE  user_id = " + id + " and scheduled_date between '" + start_date
				+ "' and '" + end_date + "' ORDER BY group_id DESC LIMIT 0 , 50;";
		/**/
		String query="SELECT   gs.msisdn AS Receiver,t.aparty AS Sender,t.message AS SMS_Content,t.Sms_Count,\r\n" + 
				"      DATE_FORMAT(t.insert_date, '%d-%m-%Y %h:%i %p') AS Insert_Date,DATE_FORMAT(t.done_date, '%d-%m-%Y %h:%i %p') AS Bubble_Process_Date,DATE_FORMAT(t.scheduled_date, '%d-%m-%Y %h:%i %p') AS Scheduled_Date,\r\n" + 
				"    IFNULL(ELT(FIELD(gs.response_code, 0, - 3, - 1, - 500),'Processed','Invalid Receiver','Sending Error','Timed Out'),'failed') AS Response,\r\n" + 
				"    IFNULL(ELT(FIELD(dr.delivery_status, 1, 0),'Delivered', 'Not Delivered'),'Status Unknown') AS Delivery_Status, \r\n" + 
				"     Case when dr.operator='RANKSTEL' \r\n" + 
				"    then IFNULL(ELT(FIELD(dr.delivery_status, 0,1),'No Record',DATE_FORMAT(DATE_ADD(dr.delivery_time, INTERVAL 4 HOUR),'%d-%m-%Y %h:%i %p')),'No Record') \r\n" + 
				"     else IFNULL(DATE_FORMAT(dr.delivery_time,'%d-%m-%Y %h:%i %p') ,'No Record') \r\n" + 
				"      end AS Delivery_Time\r\n" + 
				"  FROM  smsdb.groupsms_sender gs left JOIN smsdb.delivery_reports dr ON concat('B',gs.ID) = dr.message_id left JOIN smsdb.groupsms_sender_info t on t.group_id=gs.group_id \r\n" + 
				"  WHERE  t.user_id = "+ id + " AND t.insert_date between '" + start_date + "' and '" + end_date + "'  \r\n" + 
				"    ORDER BY gs.ID DESC;";
		
		try {

			String sql = "INSERT INTO file_dump_query (`user_id`,`query`, `output_type`) VALUES (?,?,?);";

			try {
				bubbleDS.prepareStatement(sql, true);
				bubbleDS.getPreparedStatement().setString(1, id);
				bubbleDS.getPreparedStatement().setString(2, query);
				bubbleDS.getPreparedStatement().setString(3, output_type);
				errorCode = "0";
				errorResponse = "Successfully Inserted";
				bubbleDS.execute();

			} catch (SQLException e) {
				errorCode = "11";
				errorResponse = "Inserting parameters failed";
				e.printStackTrace();
				LogWriter.LOGGER.severe("SQLException" + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				errorCode = "10";
				errorResponse = "other Exception";
				e.printStackTrace();
			} finally {
				if (bubbleDS.getConnection() != null) {
					try {
						bubbleDS.closePreparedStatement();
						// bubbleDS.getConnection().close();
					} catch (SQLException e) {
						errorCode = "-4";
						errorResponse = "Connection close Exception";
						e.printStackTrace();
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}
			}
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					bubbleDS.closePreparedStatement();
					// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					errorCode = "-4";
					errorResponse = "Connection close Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		// LogWriter.LOGGER.info("UserID:"+userId);

		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorResponse);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

	public String getListV2(String id, String userType, String msidn) {
		msidn = msisdnNormalize(msidn);
		LogWriter.LOGGER.info(" going to get list --> getList id:userType ::" + id + ":" + userType);
		String retval = "";
		String errorCode = "-1";
		// String sql="SELECT u.user_id, u.user_name, u.user_email, u.phone,
		// u.user_type, u.status, o.organization_name,o.custodian_name,
		// o.custodian_email,o.custodian_phone,o.organization_type,o.city,o.postcode,o.address
		// FROM users u left join organizations o on u.user_id=o.user_id where
		// user_type=? order by user_id asc";
		String sqlAdmin = "SELECT t.aparty,t.bparty,SUBSTRING(t.message, 1, 100) as msg,t.sms_count,t.insert_date,t.flag,t.userid,t.exec_date,t.responseCode,t.source_id FROM smsinfo t where t.bparty=? ORDER BY `ID` desc limit 0,100";
		String sql = "SELECT t.aparty,t.bparty,SUBSTRING(t.message, 1, 100) as msg,t.sms_count,t.insert_date,t.flag,t.userid,t.exec_date,t.responseCode,t.source_id FROM smsinfo t where t.bparty=? and t.userid=? ORDER BY `ID` desc limit 0,100";
		try {
			if (userType.equals("Admin")) {
				bubbleDS.prepareStatement(sqlAdmin, true);
				bubbleDS.getPreparedStatement().setString(1, msidn);
				// ResultSet rs = bubbleDS.executeQuery();
			} else {
				bubbleDS.prepareStatement(sql, true);
				bubbleDS.getPreparedStatement().setString(1, msidn);
				bubbleDS.getPreparedStatement().setString(2, id);
			}

			ResultSet rs = bubbleDS.executeQuery();
			LogWriter.LOGGER.info("executed");
			while (rs.next()) {

				if (userType.equalsIgnoreCase("Admin")) {

					// retval+="\""+rs.getString("organization_name")+"\""+",";
					retval += rs.getString("userid") + ",";
				}
				retval += rs.getString("aparty") + ",";
				retval += rs.getString("bparty") + ",";
				retval += "\"" + rs.getString("msg") + "\"" + ",";

				// retval+="\""+""+"\""+",";
				retval += "\"" + rs.getString("insert_date") + "\"" + ",";
				retval += rs.getString("sms_count") + ",";
				retval += "\"" + rs.getString("exec_date") + "\"" + ",";
				retval += rs.getString("flag") + ",";
				retval += rs.getString("source_id") + ",";
				retval += rs.getString("responseCode");
				retval += "|";
			}
			LogWriter.LOGGER.info("after execution ");
			rs.close();
			// bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get list --> " + retval);
		return retval;
	}

	public String getTexts(String id, String userType, String msidn) {
		msidn = msisdnNormalize(msidn);
		LogWriter.LOGGER.info(" going to get list --> getList id:userType ::" + id + ":" + userType);
		String retval = "";
		String errorCode = "-1";
		// String sql="SELECT u.user_id, u.user_name, u.user_email, u.phone,
		// u.user_type, u.status, o.organization_name,o.custodian_name,
		// o.custodian_email,o.custodian_phone,o.organization_type,o.city,o.postcode,o.address
		// FROM users u left join organizations o on u.user_id=o.user_id where
		// user_type=? order by user_id asc";
		String sqlAdmin = "SELECT t.aparty,t.bparty,SUBSTRING(t.message, 1, 100) as msg,t.sms_count,t.insert_date,t.flag,t.userid,t.exec_date,t.responseCode,t.source_id FROM smsinfo t where t.bparty=? ORDER BY `ID` desc limit 0,100";
		String sql = "SELECT t.aparty,t.bparty,SUBSTRING(t.message, 1, 100) as msg,t.sms_count,t.insert_date,t.flag,t.userid,t.exec_date,t.responseCode,t.source_id FROM smsinfo t where t.bparty=? and t.userid=? ORDER BY `ID` desc limit 0,100";
		try {
			if (userType.equals("Admin")) {
				bubbleDS.prepareStatement(sqlAdmin, true);
				bubbleDS.getPreparedStatement().setString(1, msidn);
				// ResultSet rs = bubbleDS.executeQuery();
			} else {
				bubbleDS.prepareStatement(sql, true);
				bubbleDS.getPreparedStatement().setString(1, msidn);
				bubbleDS.getPreparedStatement().setString(2, id);
			}

			ResultSet rs = bubbleDS.executeQuery();
			LogWriter.LOGGER.info("executed");
			while (rs.next()) {

				if (userType.equalsIgnoreCase("Admin")) {

					// retval+="\""+rs.getString("organization_name")+"\""+",";
					retval += rs.getString("userid") + ",";
				}
				retval += rs.getString("aparty") + ",";
				retval += rs.getString("bparty") + ",";
				retval += "\"" + rs.getString("msg") + "\"" + ",";

				// retval+="\""+""+"\""+",";
				retval += "\"" + rs.getString("insert_date") + "\"" + ",";
				retval += rs.getString("sms_count") + ",";
				retval += "\"" + rs.getString("exec_date") + "\"" + ",";
				retval += rs.getString("flag") + ",";
				retval += rs.getString("source_id") + ",";
				retval += rs.getString("responseCode");
				retval += "|";
			}
			LogWriter.LOGGER.info("after execution ");
			rs.close();
			// bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get list --> " + retval);
		return retval;
	}

	public String getBulkSMSDetailOfCustomer(String id) {
		// if ever Admin list needs to be added
		// getUserTypeCustomerList(id);
		String retval = "";
		String errorCode = "-1";

		String sql = "SELECT group_id, message, sms_count,flag,DATE_FORMAT(scheduled_date, \"%d-%m-%Y %H:%i\") as scheduled_date,ifnull(DATE_FORMAT(done_date, \"%d-%m-%Y %H:%i\"),\"Processing\") done_date,  (select count(*) from groupsms_sender t where t.group_id=u.group_id) msisdnCount\r\n"
				+ " FROM groupsms_sender_info  u \r\n" + " where user_id = ? ORDER BY group_id desc";/**/
		/*
		 * String
		 * sql="SELECT group_id, CONVERT(message USING utf8) as message, sms_count,flag,DATE_FORMAT(scheduled_date, \"%d-%m-%Y %H:%i\") as scheduled_date,ifnull(DATE_FORMAT(done_date, \"%d-%m-%Y %H:%i\"),\"Processing\") done_date,  (select count(*) from groupsms_sender t where t.group_id=u.group_id) msisdnCount\r\n"
		 * + " FROM groupsms_sender_info  u \r\n" +
		 * " where user_id = ? ORDER BY group_id desc";/
		 **/
		// CONVERT(message USING utf8) as message
		try {

			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);

			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				// String tempListId=rs.getString("list_id");
				// String msisdnCount=rs.getString("msisdnCount");
				retval += rs.getString("group_id") + ",";
				retval += "\"" + rs.getString("message") + "\"" + ",";
				retval += rs.getString("flag") + ",";
				retval += "\"" + rs.getString("scheduled_date") + "\"" + ",";
				retval += "\"" + rs.getString("done_date") + "\"" + ",";
				retval += rs.getString("msisdnCount");
				retval += "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	public String getCustomerGroupListInfo(String id) {
		// if ever Admin list needs to be added
		// getUserTypeCustomerList(id);
		String retval = "";
		String errorCode = "-1";
		String sql = "SELECT list_id, list_name,DATE_FORMAT(created, \"%d-%m-%Y %h:%i %p\") as created,status, (select count(*) from group_msisdn_list t where t.list_id=u.list_id) msisdnCount\r\n"
				+ " FROM group_list  u \r\n" + " where user_id = ? ORDER BY list_id desc limit 0,25";
		try {

			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);

			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				// String tempListId=rs.getString("list_id");
				// String msisdnCount=rs.getString("msisdnCount");
				retval += rs.getString("list_id") + ",";
				retval += "\"" + rs.getString("list_name") + "\"" + ",";
				retval += "\"" + rs.getString("created") + "\"" + ",";
				retval += rs.getString("status") + ",";
				retval += rs.getString("msisdnCount");
				retval += "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	public String getGeoDetail(String geoTarget) {
		String retval = "";
		String errorCode = "-1";
		String sql = "";
		if (geoTarget.equalsIgnoreCase("DIVISION")) {
			sql = "SELECT Division_name FROM divisions";
		} else if (geoTarget.equalsIgnoreCase("DISTRICT")) {
			sql = "SELECT District_name FROM districts";
		} else if (geoTarget.equalsIgnoreCase("UPAZILA")) {
			sql = "SELECT Upazila_name FROM upazilas";
		} else {
			retval = "-1";
		}
		if (!retval.equalsIgnoreCase("-1")) {
			try {

				bubbleDS.prepareStatement(sql);

				ResultSet rs = bubbleDS.executeQuery();
				while (rs.next()) {
					// String tempListId=rs.getString("list_id");
					// String msisdnCount=rs.getString("msisdnCount");
					retval += "\"" + rs.getString(1) + "\"" + ",";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
					retval = "0";
				int lio = retval.lastIndexOf(",");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				LogWriter.LOGGER.severe(e.getMessage());
			}
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	public String getActivePackageList(String id) {
		// if ever Admin list needs to be added
		String usertype = getUserTypeCustomerList(id);
		String retval = "";
		String errorCode = "-1";
		String packUserGroup = "0";
		String status = "1";
		double smsPrice = getCustomerChargingDetailUDB(id, 1);
		if (usertype.equalsIgnoreCase("10") || usertype.equalsIgnoreCase("5")) {// HV user
			packUserGroup = "\"0\",\"1\"";
		}

		/*
		 * if(usertype.equalsIgnoreCase("5")) { //Admin User
		 * packUserGroup="\"0\",\"1\""; status="\"0\",\"1\""; }/
		 **/
		// remove after
		LogWriter.LOGGER.info("status:" + status + " packUserGroup: " + packUserGroup + " usertype:" + usertype);

		String sql = "SELECT id,package_name,price,volume,validity,status FROM package_detail WHERE user_group in (?) and status in (?) order by id";
		try {

			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, packUserGroup);
			bubbleDS.getPreparedStatement().setString(2, status);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				// String tempListId=rs.getString("list_id");
				// String msisdnCount=rs.getString("msisdnCount");
				retval += rs.getString("id") + ",";
				retval += "\"" + rs.getString("package_name") + "\"" + ",";
				retval += rs.getString("price") + ",";

				String count = "0";
				try {
					double value = Double.parseDouble(rs.getString("price"));
					double countt = Math.floor(value / smsPrice);
					count = countt + "";
				} catch (Exception e) {
					e.printStackTrace();
				}
				retval += count + ",";
				retval += rs.getString("validity") + ",";
				retval += rs.getString("status");
				retval += "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	public String getAddressBookInfo(String id) {
		// if ever Admin list needs to be added
		// getUserTypeCustomerList(id);
		String retval = "";
		String errorCode = "-1";
		String sql = "SELECT msisdn,field2,field3 FROM smsdb.group_msisdn_list where list_id in (SELECT t.list_id FROM smsdb.group_list t where t.user_id=?);";
		try {

			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);

			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval += rs.getString("msisdn") + ",";
				retval += rs.getString("field2") + ",";
				retval += rs.getString("field3") + ",";
				retval += "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	public JsonEncoder getAddressBookCount(String id) {
		// if ever Admin list needs to be added
		// getUserTypeCustomerList(id);

		JsonEncoder jsonEncoder = new JsonEncoder();
		String retval = "-1";
		String errorCode = "-1";
		String sql = "SELECT count(msisdn) as counter FROM smsdb.group_msisdn_list where list_id in (SELECT t.list_id FROM smsdb.group_list t where t.user_id=?);";
		try {

			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);

			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval = rs.getString("counter");
			}
			jsonEncoder.addElement("totalContactCount", retval);
			errorCode = "0";

			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				errorCode = "-1";

			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return jsonEncoder;
	}

	public String getListMsisdnFunc(String id, String listId) {
		// if ever Admin list needs to be added
		// getUserTypeCustomerList(id);
		String retval = "";
		String errorCode = "-1";
		//String sql = "SELECT msisdn,field2,field3 FROM smsdb.group_msisdn_list where list_id in (SELECT t.list_id FROM smsdb.group_list t where t.user_id=? and t.list_id=?);";
		String sql = "SELECT id,list_id,msisdn,field2,field3 FROM smsdb.group_msisdn_list where list_id in (SELECT t.list_id FROM smsdb.group_list t where t.user_id=? and t.list_id=?);";
		
		try {

			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			bubbleDS.getPreparedStatement().setString(2, listId);

			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval += rs.getString("id") + ",";
				retval += rs.getString("list_id") + ",";
				retval += rs.getString("msisdn") + ",";
				retval += rs.getString("field2") + ",";
				retval += rs.getString("field3");
				retval += "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	/**
	 * 
	 * @param id
	 * @param listId
	 * @return comaSeparated row 
	 */
	public String getListMsisdnRowFunc(String id, String rowId) {
		String retval = "";
		String errorCode = "-1";
		//String sql = "SELECT msisdn,field2,field3 FROM smsdb.group_msisdn_list where list_id in (SELECT t.list_id FROM smsdb.group_list t where t.user_id=? and t.list_id=?);";
		String sql = "SELECT p.id,p.list_id,p.msisdn,p.field2,p.field3 FROM smsdb.group_msisdn_list p where p.list_id in (SELECT t.list_id FROM smsdb.group_list t where t.user_id=?) and p.id=?;";
		
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, id);
			bubbleDS.getPreparedStatement().setString(2, rowId);

			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval += rs.getString("id") + ",";
				retval += rs.getString("list_id") + ",";
				retval += rs.getString("msisdn") + ",";
				retval += rs.getString("field2") + ",";
				retval += rs.getString("field3");
				retval += "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				retval = "0";
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			LogWriter.LOGGER.info("MapList : " + retval);
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	public String getAllCustomerList(String id) {
		String retval = "";
		String errorCode = "-1";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);
			// String userFlag="5";
			if (userFlag.equals("5")) {
				String sql = "SELECT t.id,t.custodian_name,t.username,t.email,t.phone,t.organization_name,t.city,t.address,t.postcode,DATE_FORMAT(t.insert_date, \"%d-%m-%Y %H:%i\") as insert_date,t.flag FROM  tbl_users t where t.flag in (0,1,10) and  t.flag !=?";

				try {
					bubbleDS.prepareStatement(sql);
					bubbleDS.getPreparedStatement().setString(1, userFlag);
					ResultSet rs = bubbleDS.executeQuery();
					while (rs.next()) {
						retval += "\"" + rs.getString("custodian_name") + "\"" + ",";
						retval += "\"" + rs.getString("username") + "\"" + ",";
						retval += "\"" + rs.getString("organization_name") + "\"" + ",";
						retval += rs.getString("city") + ",";
						retval += "\"" + rs.getString("address") + "\"" + ",";
						retval += rs.getString("postcode") + ",";
						retval += "\"" + rs.getString("insert_date") + "\"" + ",";
						retval += rs.getString("flag") + ",";
						retval += "\"" + rs.getString("email") + "\"" + ",";
						retval += rs.getString("phone") + ",";
						retval += rs.getString("id");
						retval += "|";
					}
					bubbleDS.closeResultSet();
					bubbleDS.closePreparedStatement();
					if (NullPointerExceptionHandler.isNullOrEmpty(retval))
						retval = "0";
					int lio = retval.lastIndexOf("|");
					if (lio > 0)
						retval = retval.substring(0, lio);
					errorCode = "0";
					LogWriter.LOGGER.info("MapList : " + retval);
				} catch (SQLException e) {
					errorCode = "-2";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				} catch (Exception e) {
					errorCode = "-3";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			} else {
				errorCode = "-7: User not Authorized to perform this action";
			}
			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogWriter.LOGGER.severe(" return from  get Customer list --> userFlag : " + userFlag + ":" + retval);
		return retval;
	}

	// NiharekahS
	/**
	 * 
	 * @param id      of user
	 * @param smsText
	 * @return action status
	 */
	public String requestFormulaApproval(String id, String smsText) {
		String errorCode = "-1";// default errorCode
		String errorRes = "Request for approval Failed";
		String sqlTextCheck = "SELECT id FROM tbl_preapproved_texts where userID=? and userInputText=? limit 1";
		String sqlInsert = "INSERT INTO tbl_preapproved_texts(" + "userID,textFormula,userInputText,request_time" + ") VALUES"
				+ "(?,?,?,current_timestamp)";
		ResultSet rs = null;
		String escapedSMSText = "";
		if(validator(smsText)) {
			escapedSMSText = escapeMetaCharacters(smsText);
			try {
				bubbleDS.prepareStatement(sqlTextCheck);
				bubbleDS.getPreparedStatement().setString(1, id);
				bubbleDS.getPreparedStatement().setString(2, smsText);
				rs = bubbleDS.executeQuery();
				if (rs.next()) {
					// entry exists
					if (errorCode.equals("-1"))
						errorCode = "20";
					errorRes = "Text already submitted for approval.";
				} else {
					bubbleDS.prepareStatement(sqlInsert);
					bubbleDS.getPreparedStatement().setString(1, id);
					bubbleDS.getPreparedStatement().setString(2, escapedSMSText);
					bubbleDS.getPreparedStatement().setString(3, smsText);

					try {
						bubbleDS.execute();
						errorCode = "0";
						errorRes = "Successfully sent for approval.";
					} catch (SQLIntegrityConstraintViolationException de) {
						errorCode = "-1";
						errorRes = "Request for approval Failed";
						LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
					} catch (SQLException e) {
						errorCode = "-2";
						errorRes = "SQLException";
						LogWriter.LOGGER.severe("SQLException" + e.getMessage());
					}
					if (bubbleDS.getConnection() != null)
						bubbleDS.closePreparedStatement();
					if (errorCode.equals("-1"))
						errorCode = "0";

				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				errorCode = "-2";
				errorRes = "SQLException";
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				errorRes = "General Exception";
				LogWriter.LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}
		}else {
			errorCode = "2";
			errorRes = "Formula Text is Invalid";
		}
		JsonEncoder jsonEncoder = new JsonEncoder();
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorRes);
		jsonEncoder.buildJsonObject();
		errorCode = jsonEncoder.getJsonObject().toString();

		return errorCode;
	}

	/**
	 * 
	 * @param id of admin
	 * @return all the pending SMS for approval list
	 */
	public String getAllPendingFormulaTextApprovalList(String id) {
		String retval = "";
		String errorCode = "-1";
		String errorRes = "Fetching allPendingFormulaTextApproval Failed";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);
			// String userFlag="5";
			if (userFlag.equals("5")) {
				String sql = "SELECT t.id AS textID,u.id,u.username,t.userInputText,u.organization_name,DATE_FORMAT(t.request_time, \"%d-%m-%Y %H:%i\") as request_time,t.flag FROM  tbl_users u INNER JOIN tbl_preapproved_texts t ON t.userID = u.id where t.flag =-1";
				try {
					bubbleDS.prepareStatement(sql);

					// bubbleDS.getPreparedStatement().setString(1, userFlag);
					// bubbleDS.getPreparedStatement().setString(1, userFlag);
					// LogWriter.LOGGER.severe(" after prepared Statement");
					ResultSet rs = bubbleDS.executeQuery();
					while (rs.next()) {
						retval += rs.getString("textID") + ",";
						retval += rs.getString("id") + ",";
						retval += "\"" + rs.getString("username") + "\"" + ",";
						retval += "\"" + rs.getString("organization_name") + "\"" + ",";
						retval += "\"" + rs.getString("request_time") + "\"" + ",";
						retval += "\"" + rs.getString("userInputText") + "\"" + ",";
						retval += rs.getString("flag");
						retval += "|";
					}
					bubbleDS.closeResultSet();
					bubbleDS.closePreparedStatement();
					if (NullPointerExceptionHandler.isNullOrEmpty(retval))
					{
						retval = "null";
						errorRes = "No FormulaText  Pending For Approval";
					}
					else 
					{
						errorRes = "Successfully Fetched All Pending FormulaText Approval";
					}
					int lio = retval.lastIndexOf("|");
					if (lio > 0)
						retval = retval.substring(0, lio);
					errorCode = "0";
					LogWriter.LOGGER.info("MapList : " + retval);
				} catch (SQLException e) {
					errorCode = "-2";
					errorRes = "SQLException";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				} catch (Exception e) {
					errorCode = "-3";
					errorRes = "General Exception";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			} else {
				errorCode = "-7";
				errorRes = "User not Authorized to perform this action";
			}
		} catch (Exception e) {
			errorCode = "-3";
			errorRes = "General Exception";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		LogWriter.LOGGER.info(" return from  get All Pending FormulaText Approval --> userFlag : " + userFlag + ":" + retval);
		if (retval.startsWith("null") || !errorCode.startsWith("0")) 
		{
			JsonEncoder jsonEncoder = new JsonEncoder();
			jsonEncoder.addElement("ErrorCode", errorCode);
			jsonEncoder.addElement("ErrorResponse", errorRes);
			jsonEncoder.buildJsonObject();
			errorCode = jsonEncoder.getJsonObject().toString();
			retval = errorCode;
		}
		return retval;
	}

	/**
	 * 
	 * @param userID of Admin
	 * @param textID for the Target SMS
	 * @param flag   for approval Status
	 * @return action status
	 */
	public String updateFormulaTextApprovalList(String userID, String textID, String flag) {
		String errorCode = "-1";
		String errorRes = "Updating pendingApprovalList Failed";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(userID);
			// String userFlag="5";
			if (userFlag.equals("5")) {
				errorCode = updateSMSApprovalStatus(userID, textID, flag);
			} else {
				errorCode = "-7";
				errorRes = "User not Authorized to perform this action";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogWriter.LOGGER.info(" return from  Update Pending FormulaText Approval --> userFlag : " + userFlag + ":" + errorCode);
		if (errorCode.startsWith("0")) {
			errorRes = "Successfully Updated Pending Approval List";
		} else if (errorCode.startsWith("-1")) {
			errorRes = "Updating Pending Approval List Failed";
		} else if (errorCode.startsWith("-2")) {
			errorRes = "SQLException";
		} else if (errorCode.startsWith("-3")) {
			errorRes = "General Exception";
		}
		JsonEncoder jsonEncoder = new JsonEncoder();
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorRes);
		jsonEncoder.buildJsonObject();
		errorCode = jsonEncoder.getJsonObject().toString();

		return errorCode;
	}

	/**
	 * 
	 * @param id of user
	 * @return status of all the requests made by the user
	 */
	public String getUserFormulaTextApprovalRequestStatus(String id) {
		String retval = "";
		String errorCode = "-1";
		String errorRes = "Fetching userFormulaTextApprovalStatus Failed";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);

			String sql = "SELECT t.id,t.userInputText,DATE_FORMAT(t.request_time, \"%d-%m-%Y %H:%i\") as request_time,t.flag FROM  tbl_users u INNER JOIN tbl_preapproved_texts t ON t.userID = u.id where t.userID=?";
			try {
				bubbleDS.prepareStatement(sql);

				bubbleDS.getPreparedStatement().setString(1, id);
				// bubbleDS.getPreparedStatement().setString(1, userFlag);
				// LogWriter.LOGGER.severe(" after prepared Statement");
				ResultSet rs = bubbleDS.executeQuery();
				while (rs.next()) {
					retval += rs.getString("id") + ",";
					retval += "\"" + rs.getString("request_time") + "\"" + ",";
					retval += "\"" + rs.getString("userInputText") + "\"" + ",";
					retval += rs.getString("flag");
					retval += "|";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				{
					retval = "null";
					errorRes = "No Formula Text For Approval";
				}
				int lio = retval.lastIndexOf("|");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				errorRes = "SQLException";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				errorRes = "General Exception";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
			}
		} catch (Exception e) {
			errorCode = "-3";
			errorRes = "General Exception";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}
		LogWriter.LOGGER.info(" return from get User FormulaText Approval Status --> userFlag : " + userFlag + ":" + retval);
		if (!errorCode.startsWith("0")) {
			JsonEncoder jsonEncoder = new JsonEncoder();
			jsonEncoder.addElement("ErrorCode", errorCode);
			jsonEncoder.addElement("ErrorResponse", errorRes);
			jsonEncoder.buildJsonObject();
			errorCode = jsonEncoder.getJsonObject().toString();
			retval = errorCode;
		}
		return retval;
	}

	/**
	 * 
	 * @param id of user
	 * @return all the approved requests made by the user
	 */
	public String getAllApprovedFormulaText(String id) {
		String retval = "";
		String errorCode = "-1";
		String errorRes = "Fetching approvedUserFormulaText Failed";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);

			String sql = "SELECT t.id,t.userInputText FROM tbl_preapproved_texts t where t.userID=? and t.flag=0 ORDER BY approval_time DESC limit 10";
			try {
				bubbleDS.prepareStatement(sql);

				bubbleDS.getPreparedStatement().setString(1, id);
				// bubbleDS.getPreparedStatement().setString(1, userFlag);
				// LogWriter.LOGGER.severe(" after prepared Statement");
				ResultSet rs = bubbleDS.executeQuery();
				while (rs.next()) {
					retval += rs.getString("id") + ",";
					retval += "\"" + rs.getString("userInputText") + "\"";
					retval += "|";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
				{
					retval = "null";
					errorRes = "No Formula Text Found";
				}
				int lio = retval.lastIndexOf("|");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				errorRes = "SQLException";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				errorRes = "General Exception";
				e.printStackTrace();
				LogWriter.LOGGER.severe(e.getMessage());
			}
		} catch (Exception e) {
			errorCode = "-3";
			errorRes = "General Exception";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}
		LogWriter.LOGGER.info(" return from  get All Approved FormulaText --> userFlag : " + userFlag + ":" + retval);
		if (!errorCode.startsWith("0")) {
			JsonEncoder jsonEncoder = new JsonEncoder();
			jsonEncoder.addElement("ErrorCode", errorCode);
			jsonEncoder.addElement("ErrorResponse", errorRes);
			jsonEncoder.buildJsonObject();
			errorCode = jsonEncoder.getJsonObject().toString();
			retval = errorCode;
		}
		return retval;
	}

	/**
	 * @param id
	 * @param textFormula
	 * @param smsText
	 * @return
	 */
	public String checkFormulaText(String id, String textFormula, String smsText) {
		String errorCode = "-1";
		String errorRes = "checkFormulaText Failed";
		String userFlag = "-1";
		if(validator(textFormula)) {
			textFormula = escapeMetaCharacters(textFormula);

			try {
				userFlag = getUserTypeCustomerList(id);
				System.setProperty("file.encoding", "UTF-8");
				Field charset = Charset.class.getDeclaredField("defaultCharset");
				charset.setAccessible(true);
				charset.set(null, null);
				boolean retval = false;

				retval = match(textFormula, smsText);
				LogWriter.LOGGER.info("Formula : " + textFormula);
				LogWriter.LOGGER.info("text : " + smsText);
				if (retval) {
					errorCode = "0";
					errorRes = "text matched";
				} else {
					errorCode = "1";
					errorRes = "text did not match";
				}
			} catch (NoSuchFieldException ex) {
				LogWriter.LOGGER.severe("checkFormulaText --> NoSuchFieldException");
			} catch (SecurityException ex) {
				LogWriter.LOGGER.severe("checkFormulaText --> SecurityException");
			} catch (IllegalArgumentException ex) {
				LogWriter.LOGGER.severe("checkFormulaText --> IllegalArgumentException");
			} catch (IllegalAccessException ex) {
				LogWriter.LOGGER.severe("checkFormulaText --> IllegalAccessException");
			}
		}else {
			errorCode = "2";
			errorRes = "Formula text is Invalid";
		}
		JsonEncoder jsonEncoder = new JsonEncoder();
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorRes);
		jsonEncoder.buildJsonObject();
		errorCode = jsonEncoder.getJsonObject().toString();

		LogWriter.LOGGER.info(" return from  checkFormulaText --> userFlag : " + userFlag + ":" + errorCode);

		return errorCode;
	}

	/**
	 * 
	 * @param id
	 * @param smsText
	 * @return
	 */
	public String checkFormulaTextFromDBbyText(String id, String smsText, String extendedRes) {
		String errorCode = "-1";
		String errorRes = "checkFormulaTextFromDBbyText";
		String userFlag = "-1";
		String approvedTextID = "-1";
		String textFormula = "-1";
		String userInputText = "-1";

		boolean retVal = false;
		try {
			userFlag = getUserTypeCustomerList(id);
			String sql = "SELECT t.id,t.textFormula FROM tbl_preapproved_texts t where t.userID=? and t.flag=0 ORDER BY approval_time DESC";

			bubbleDS.prepareStatement(sql);

			bubbleDS.getPreparedStatement().setString(1, id);
			boolean result = false;
			ArrayList<String> frmtInfo = new ArrayList<String>();
			ResultSet rs = bubbleDS.executeQuery();

			while (rs.next()) {
				approvedTextID = rs.getString("id");
				textFormula = rs.getString("textFormula");
				frmtInfo.add(approvedTextID+"|"+textFormula);
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			errorCode = "1";
			errorRes = "text did not match";
			for (String f : frmtInfo) {
				approvedTextID = f.substring(0, f.indexOf('|'));
				textFormula = f.substring(f.indexOf('|') + 1);
				result = match(textFormula, smsText);
				LogWriter.LOGGER.info("Formula : " + textFormula);
				LogWriter.LOGGER.info("text : " + smsText);
				if (result) {
					errorCode = "0";
					errorRes = "text matched";
					if(extendedRes.equals("1")) {
						sql = "SELECT t.userInputText FROM tbl_preapproved_texts t where t.id=? ORDER BY approval_time DESC";

						bubbleDS.prepareStatement(sql);

						bubbleDS.getPreparedStatement().setString(1, approvedTextID);
						ResultSet rs2 = bubbleDS.executeQuery();
						if (rs2.next()) {
							userInputText = rs2.getString("userInputText");
						}
						bubbleDS.closeResultSet();
						bubbleDS.closePreparedStatement();
					}
					break;
				}
				else 
				{	
					errorCode = "1";
					errorRes = "text did not match";
					approvedTextID = "-1";
					textFormula = "-1";
					userInputText = "-1";
				}
			}
		} catch (SQLException e) {
			errorCode = "-2";
			errorRes = "SQLException";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			errorRes = "General Exception";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}

		JsonEncoder jsonEncoder = new JsonEncoder();
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorRes);
		if(extendedRes.equals("1")) {
			jsonEncoder.addElement("textID", approvedTextID);
			jsonEncoder.addElement("textFormula", textFormula);
			jsonEncoder.addElement("userInputText", userInputText);
		}
		jsonEncoder.buildJsonObject();
		errorCode = jsonEncoder.getJsonObject().toString();

		LogWriter.LOGGER
		.info(" return from  checkFormulaTextFromDBbyText --> userFlag : " + userFlag + ":" + errorCode);

		return errorCode;
	}

	/**
	 * 
	 * @param id
	 * @param smsText
	 * @return
	 */
	public String checkFormulaTextFromDBbyID(String id, String textID, String smsText, String extendedRes) {
		String errorCode = "-1";
		String errorRes = "checkFormulaTextFromDBbyText";
		String userFlag = "-1";
		String textFormula = "-1";
		String userInputText = "-1";

		boolean retVal = false;
		try {
			userFlag = getUserTypeCustomerList(id);
			String sql = "SELECT t.id,t.textFormula, t.userInputText FROM tbl_preapproved_texts t where t.userID=? and t.flag=0 and t.id=? ORDER BY approval_time DESC";

			bubbleDS.prepareStatement(sql);

			bubbleDS.getPreparedStatement().setString(1, id);
			bubbleDS.getPreparedStatement().setString(2, textID);
			boolean result = false;
			ResultSet rs = bubbleDS.executeQuery();

			if (rs.next()) {
				textFormula = rs.getString("textFormula");
				userInputText = rs.getString("userInputText");
				result = match(textFormula, smsText);
				LogWriter.LOGGER.info("Formula : " + textFormula);
				LogWriter.LOGGER.info("text : " + smsText);
				if (result) {
					errorCode = "0";
					errorRes = "text matched";
				}
				else 
				{
					errorCode = "1";
					errorRes = "text did not match";
					textFormula = "-1";
				}
			}
			else 
			{
				errorCode = "2";
				errorRes = "no enrty for given textID";
				textID = "-1";
				textFormula = "-1";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();

		} catch (SQLException e) {
			errorCode = "-2";
			errorRes = "SQLException";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			errorRes = "General Exception";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}

		JsonEncoder jsonEncoder = new JsonEncoder();
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.addElement("ErrorResponse", errorRes);
		if(extendedRes.equals("1")) {
			jsonEncoder.addElement("textID", textID);
			jsonEncoder.addElement("textFormula", textFormula);
			jsonEncoder.addElement("userInputText", userInputText);
		}
		jsonEncoder.buildJsonObject();
		errorCode = jsonEncoder.getJsonObject().toString();

		LogWriter.LOGGER
		.info(" return from  checkFormulaTextFromDBbyText --> userFlag : " + userFlag + ":" + errorCode);

		return errorCode;
	}

	public boolean validator(String str) {
		String findStr = "^^";
		int lastIndex = 0;
		int count = 0;
		boolean validity = false;
		while(lastIndex != -1){

			lastIndex = str.indexOf(findStr,lastIndex);

			if(lastIndex != -1){
				count ++;
				lastIndex += findStr.length();
			}
		}
		//System.out.println("counter : "+count);

		if(count%2==0)
			validity = true;
		else
			validity = false;

		return validity;
	}

	public String escapeMetaCharacters(String str){

		// splitting by ^^
		List<String> tokens = Arrays.asList(str.split("\\^\\^"));
		//System.out.println("number of tokens : "+tokens.size());
		String escapedString = "";
		final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};
		for(int c=0;c<tokens.size();c++) {
			String token = tokens.get(c);
			if(c%2==0) {
				// escaping metaCharacters
				for (int i = 0 ; i < metaCharacters.length ; i++){
					if(token.contains(metaCharacters[i])){
						token = token.replace(metaCharacters[i],"\\"+metaCharacters[i]);
					}
				}
			}
			escapedString = escapedString + token;
		}
		System.out.println("escaped : "+escapedString);

		return escapedString;
	}

	// NiharekahS END

	public String getPendingBulksmsList(String id) {
		String retval = "";
		String errorCode = "-1";
		// String tmpMsisdnCount="-1";
		String userFlag = "-1";
		try {
			userFlag = getUserTypeCustomerList(id);
			// String userFlag="5";
			if (userFlag.equals("5")) {
				// String sql="SELECT t.user_id,t.group_id,u.custodian_name,u.organization_name,
				// t.`aparty`,t.`msisdn_count`, t.`insert_date`, t.`scheduled_date`,t.`flag`,
				// t.`message`, t.`sms_count` FROM `groupsms_sender_info` t, tbl_users u WHERE
				// t.user_id=u.id and t.`flag`=? order by t.`scheduled_date` asc";
				String sql = "SELECT t.user_id,t.group_id,u.custodian_name,u.organization_name, t.`aparty`,(select COUNT(gsi.msisdn) from groupsms_sender gsi where  t.group_id=gsi.group_id) as msCount, t.`insert_date`, t.`scheduled_date`,t.`flag`, t.`message`, t.`sms_count` FROM `groupsms_sender_info` t, tbl_users u WHERE t.user_id=u.id and t.`flag`=? order by t.`scheduled_date` asc";
				try {
					bubbleDS.prepareStatement(sql);

					// bubbleDS.getPreparedStatement().setString(1, userFlag);
					bubbleDS.getPreparedStatement().setInt(1, -1);
					// LogWriter.LOGGER.severe(" after prepared Statement");
					ResultSet rs = bubbleDS.executeQuery();
					while (rs.next()) {

						// tmpMsisdnCount=getMsisdnCountInGroup(rs.getString("group_id"));

						retval += "\"" + rs.getString("custodian_name") + "\"" + ",";
						retval += "\"" + rs.getString("user_id") + "\"" + ",";
						retval += "\"" + rs.getString("organization_name") + "\"" + ",";
						retval += "\"" + rs.getString("group_id") + "\"" + ",";
						retval += rs.getString("aparty") + ",";
						retval += "\"" + rs.getString("message") + "\"" + ",";
						retval += "\"" + rs.getString("insert_date") + "\"" + ",";
						retval += "\"" + rs.getString("scheduled_date") + "\"" + ",";
						retval += rs.getString("msCount") + ",";
						retval += rs.getString("flag") + ",";
						retval += rs.getString("sms_count");
						retval += "|";
					}
					bubbleDS.closeResultSet();
					bubbleDS.closePreparedStatement();
					if (NullPointerExceptionHandler.isNullOrEmpty(retval))
						retval = "0";
					int lio = retval.lastIndexOf("|");
					if (lio > 0)
						retval = retval.substring(0, lio);
					errorCode = "0";
					LogWriter.LOGGER.info("MapList : " + retval);
				} catch (SQLException e) {
					errorCode = "-2";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				} catch (Exception e) {
					errorCode = "-3";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			} else {
				errorCode = "-7: User not Authorized to perform this action";
			}
			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogWriter.LOGGER
		.severe(" return from  get getPendingBulksmsList list --> userFlag : " + userFlag + ":" + retval);
		return retval;
	}

	/**
	 * If msisdn starts with 0, prepends 88. If msisdn starts with 880 or any other
	 * number, returns the String
	 * 
	 * @param msisdn
	 * @return msisdn of the format 8801xx
	 */
	private String msisdnNormalize(String msisdn) {
		if (msisdn.startsWith("0")) {
			msisdn = "88" + msisdn;
		} /*
		 * else if(msisdn.startsWith("80")) { msisdn="8"+msisdn; }else
		 * if(msisdn.startsWith("880")) { msisdn=msisdn; }else { msisdn="880"+msisdn; }/
		 **/
		return msisdn;
	}

	/**
	 * 
	 * @return String groupID
	 * @throws SQLException
	 */
	private String getNewGroupId() throws SQLException {
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
	 * @return
	 */
	public String getFileList(String userId, String userType) {
		String retval = "-1";
		String errorCode = "-1";
		String sql = "select u.custodian_name,u.organization_name,t.message,t.sms_count,date_format(t.insert_date,'%Y-%m-%d %H:%i:%s') as created, (select COUNT(msisdn) from smsdb.groupsms_sender gsi where t.group_id=gsi.group_id) as msisdnCount, t.flag \r\n"
				+ ",t.action_date from groupsms_sender_info t , tbl_users u WHERE t.user_id=u.id order by t.insert_date asc where user_id=?";

		String sqlAdmin = "select u.custodian_name,u.organization_name,t.message,t.sms_count,date_format(t.insert_date,'%Y-%m-%d %H:%i:%s') as created, (select COUNT(msisdn) from smsdb.groupsms_sender gsi where t.group_id=gsi.group_id) as msisdnCount, t.flag \r\n"
				+ ",t.action_date from groupsms_sender_info t , tbl_users u WHERE t.user_id=u.id order by t.insert_date asc limit 0,50";
		try {

			if (userType.equalsIgnoreCase("Admin")) {// Admin
				bubbleDS.prepareStatement(sqlAdmin);
			} else {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, userId);
			}
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval += "\"" + rs.getString("custodian_name") + "\"" + ",";
				retval += "\"" + rs.getString("organization_name") + "\"" + ",";
				retval += "\"" + rs.getString("message") + "\"" + ",";
				retval += "\"" + rs.getString("sms_count") + "\"" + ",";
				retval += "\"" + rs.getString("created") + "\"" + ",";
				retval += "\"" + rs.getString("msisdnCount") + "\"" + ",";
				retval += "\"" + rs.getString("flag") + "\"" + ",";
				retval += "\"" + rs.getString("action_date") + "\"" + "|";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			// retval=retval.substring(0,retval.lastIndexOf("|"));
		} catch (Exception e) {
			retval = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	/**
	 * 
	 * @param filename
	 * @return filename,user_id,created,uploaded,status,estimated_upload_time,comments
	 *         -ve integer is error
	 */
	public String getUploadStatus(String filename) {
		String retval = "-1";
		String errorCode = "-1";
		String sql = "select file_name,user_id,date_format(created,'%Y-%m-%d %H:%i:%s') as created,date_format(uploaded,'%Y-%m-%d %H:%i:%s') as uploaded,CASE status WHEN 0 THEN 'new' WHEN 1 THEN 'uploading' WHEN 2 THEN 'uploaded' WHEN 3 THEN 'error' ELSE 'invalid' end as status,estimated_upload_time,comments from bubble_file_info where file_name=?";
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, filename);
			ResultSet rs = bubbleDS.executeQuery();
			while (rs.next()) {
				retval += "\"" + rs.getString("file_name") + "\"" + ",";
				retval += "\"" + rs.getString("user_id") + "\"" + ",";
				retval += "\"" + rs.getString("created") + "\"" + ",";
				retval += "\"" + rs.getString("uploaded") + "\"" + ",";
				retval += "\"" + rs.getString("status") + "\"" + ",";
				retval += "\"" + rs.getString("estimated_upload_time") + "\"" + ",";
				retval += "\"" + rs.getString("comments") + "\"" + "|";
				errorCode = "0";
			}
			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			retval = retval.substring(0, retval.lastIndexOf("|"));
		} catch (Exception e) {
			retval = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	// bubbleFileInsert
	/**
	 * 
	 * @param jsonDecoder filename,schoolId,month
	 * @return 0 is successfully inserted Anything -ve is error.
	 */
	public String bubbleFileInsert(JsonDecoder jsonDecoder) {
		String errorCode = "-1";// default errorCode
		String sqlInsert = "INSERT INTO bubble_file_info(" + "file_name,user_id,listId" + ") VALUES" + "(?,?,?)";
		try {
			// json: file_name,school_id
			bubbleDS.prepareStatement(sqlInsert);
			bubbleDS.getPreparedStatement().setString(1, jsonDecoder.getEString("filename"));
			bubbleDS.getPreparedStatement().setString(2, jsonDecoder.getEString("id"));
			bubbleDS.getPreparedStatement().setString(3, jsonDecoder.getEString("listId"));
			try {
				bubbleDS.execute();
			} catch (SQLIntegrityConstraintViolationException de) {
				errorCode = "-1:duplicate filename";
				LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
			} catch (SQLException e) {
				errorCode = "-11:Inserting failed";
				LogWriter.LOGGER.severe("SQLException" + e.getMessage());
			}
			if (bubbleDS.getConnection() != null)
				bubbleDS.closePreparedStatement();
			if (errorCode.equals("-1"))
				errorCode = "0";
		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} /*
		 * finally{ if(bubbleDS.getConnection() != null){ try {
		 * bubbleDS.getConnection().close(); } catch (SQLException e) { errorCode="-4";
		 * LogWriter.LOGGER.severe(e.getMessage()); } } }/
		 **/
		return errorCode;
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	public int getSMSSize(String message) {
		int retval = 1;
		int tempSize = smsLength(message);
		if (isUnicode(message)) {
			if (isLongSmsUNICODE(tempSize)) {
				retval = smsCountUNICODE(tempSize);
			} else {
				retval = 1;
				;
			}
		} else {
			if (isLongSmsASCII(tempSize)) {
				retval = smsCountASCII(tempSize);
			} else {
				retval = 1;
			}
		}
		return retval;
	}

	public boolean isUnicode(String text) {
		boolean RETVAL = false;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) >= 128) {
				RETVAL = true;
				break;
			}
		}
		return RETVAL;
	}

	public boolean isLongSmsASCII(int textSize) {
		boolean RETVAL = false;
		if (textSize > 160)
			RETVAL = true;
		return RETVAL;
	}

	public boolean isLongSmsUNICODE(int textSize) {
		boolean RETVAL = false;
		if (textSize > 67)
			RETVAL = true;
		return RETVAL;
	}

	public int smsCountUNICODE(int textSize) {
		int count = -100;
		count = (int) Math.ceil(textSize / 67.00);
		return count;
	}

	public int smsCountASCII(int textSize) {
		int count = -100;
		count = (int) Math.ceil(textSize / 153.00);
		return count;
	}

	public int smsLength(String text) {
		System.out.println("Length of TEXT : " + text.length());
		return text.length();
	}

	public boolean match(String patternString, String text) {
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);
		boolean matches = matcher.matches();
		return matches;
	}

	/**
	 * 
	 * @param userId
	 * @param lastIndex
	 * @param isDescending
	 * @return list in counts of 100 in ascending or descending order from the index
	 */
	public String getInbox(String userId, long lastIndex, boolean isDescending) {
		String retval = "";
		String errorCode = "-1";
		String sql = "select i.id, sender,OriginTime,text,BPLNumber as inbox_number from inbound_sms i,inbox_numbers n where n.user_id=? and n.inbox_msisdn = i.BPLNumber and i.id <condition> ? order by i.OriginTime <order>,i.id <order> limit 100";
		if (isDescending) {
			sql = sql.replaceAll("<order>", "desc");
			if (lastIndex == 0)
				sql = sql.replace("and i.id <condition> ? ", "");
			else
				sql = sql.replaceAll("<condition> ?", "<" + lastIndex);
		} else {
			sql = sql.replaceAll("<order>", "asc");
			sql = sql.replaceAll("<condition>", ">");
		}
		ResultSet rs = null;
		try {
			bubbleDS.prepareStatement(sql);
			bubbleDS.getPreparedStatement().setString(1, userId);

			rs = bubbleDS.executeQuery();
			if (rs.isBeforeFirst()) {
				while (rs.next()) {
					retval += "\"" + rs.getString("id") + "\"" + ",";
					retval += "\"" + rs.getString("sender") + "\"" + ",";
					retval += "\"" + rs.getString("OriginTime") + "\"" + ",";
					retval += "\"" + rs.getString("text") + "\"" + ",";
					retval += "\"" + rs.getString("inbox_number") + "\"" + "|";
				}
			} else {
				retval = "0";
			}

			bubbleDS.closeResultSet();
			bubbleDS.closePreparedStatement();
			int lio = retval.lastIndexOf("|");
			if (lio > 0)
				retval = retval.substring(0, lio);
			errorCode = "0";
			// retval=retval.substring(0,retval.lastIndexOf("|"));
		} catch (Exception e) {
			retval = "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		} finally {
			if (bubbleDS.getConnection() != null) {
				try {
					if (rs != null)
						if (!rs.isClosed())
							rs.close();
					if (!bubbleDS.isResultSetClosed())
						bubbleDS.closeResultSet();
					if (!bubbleDS.isPreparedStatementClosed())
						bubbleDS.closePreparedStatement();
					// bubbleDS.getConnection().close();
				} catch (SQLException e) {
					retval = "-3";
					LogWriter.LOGGER.severe(e.getMessage());
					// this.logWriter.appendLog("s:FSE");
					// this.logWriter.appendAdditionalInfo("UDO.getList():"+e.getMessage());
				}
			}
		}
		if (!errorCode.startsWith("0")) {
			retval = errorCode;
		}
		return retval;
	}

	/**
	 * 
	 * @param country
	 * @return division id,name|id,name|...
	 */
	public String getGeoLocation(String country) {
		String retval = "";
		String errorCode = "-1";
		String sql;
		if (country.equalsIgnoreCase("1") || country.equalsIgnoreCase("Bangladesh")) {
			sql = "SELECT id,Division_name FROM divisions";
			ResultSet rs = null;
			try {
				bubbleDS.prepareStatement(sql);
				rs = bubbleDS.executeQuery();
				while (rs.next()) {
					retval += "\"" + rs.getString(1) + "\"" + ",";
					retval += "\"" + rs.getString(2) + "\"" + "|";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
					retval = "0";
				int lio = retval.lastIndexOf("|");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				LogWriter.LOGGER.severe(e.getMessage());
			} finally {
				if (bubbleDS.getConnection() != null) {
					try {
						if (rs != null)
							if (!rs.isClosed())
								rs.close();
						if (!bubbleDS.isResultSetClosed())
							bubbleDS.closeResultSet();
						if (!bubbleDS.isPreparedStatementClosed())
							bubbleDS.closePreparedStatement();
						// bubbleDS.getConnection().close();
					} catch (SQLException e) {
						retval = "-3";
						LogWriter.LOGGER.severe(e.getMessage());
						// this.logWriter.appendLog("s:FSE");
						// this.logWriter.appendAdditionalInfo("UDO.getList():"+e.getMessage());
					}
				}
			}
			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} else {
			retval = "-1:Country " + country + " is not available";
		}
		return retval;
	}

	/**
	 * 
	 * @param country
	 * @param division
	 * @return district id,name|id,name|...
	 */
	public String getGeoLocation(String country, String division) {
		String retval = "";
		String errorCode = "-1";
		String sql;
		if (!NullPointerExceptionHandler.isNullOrEmpty(division)) {
			sql = "SELECT id,District_name FROM districts where division_id=?";
			ResultSet rs = null;
			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, division);
				rs = bubbleDS.executeQuery();
				// if before first then ok otherwise division is not found
				while (rs.next()) {
					retval += "\"" + rs.getString(1) + "\"" + ",";
					retval += "\"" + rs.getString(2) + "\"" + "|";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
					retval = "0";
				int lio = retval.lastIndexOf("|");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				LogWriter.LOGGER.severe(e.getMessage());
			} finally {
				if (bubbleDS.getConnection() != null) {
					try {
						if (rs != null)
							if (!rs.isClosed())
								rs.close();
						if (!bubbleDS.isResultSetClosed())
							bubbleDS.closeResultSet();
						if (!bubbleDS.isPreparedStatementClosed())
							bubbleDS.closePreparedStatement();
						// bubbleDS.getConnection().close();
					} catch (SQLException e) {
						retval = "-3";
						LogWriter.LOGGER.severe(e.getMessage());
						// this.logWriter.appendLog("s:FSE");
						// this.logWriter.appendAdditionalInfo("UDO.getList():"+e.getMessage());
					}
				}
			}
			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} else {
			retval = "-1:Division cannot be null";
		}
		return retval;
	}

	/**
	 * 
	 * @param country
	 * @param division
	 * @param district
	 * @return upazilla id,name|id,name|...
	 */
	public String getGeoLocation(String country, String division, String district) {
		String retval = "";
		String errorCode = "-1";
		String sql;
		// if(!NullPointerExceptionHandler.isNullOrEmpty(division) &&
		// !NullPointerExceptionHandler.isNullOrEmpty(district) ) {
		if (!NullPointerExceptionHandler.isNullOrEmpty(district)) {
			sql = "SELECT id,Upazila_name FROM upazilas where district_id=?";
			ResultSet rs = null;
			try {
				bubbleDS.prepareStatement(sql);
				// bubbleDS.getPreparedStatement().setString(1, division);
				bubbleDS.getPreparedStatement().setString(1, district);
				rs = bubbleDS.executeQuery();
				// if before first then ok otherwise division is not found
				while (rs.next()) {
					retval += "\"" + rs.getString(1) + "\"" + ",";
					retval += "\"" + rs.getString(2) + "\"" + "|";
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
				if (NullPointerExceptionHandler.isNullOrEmpty(retval))
					retval = "0";
				int lio = retval.lastIndexOf("|");
				if (lio > 0)
					retval = retval.substring(0, lio);
				errorCode = "0";
				LogWriter.LOGGER.info("MapList : " + retval);
			} catch (SQLException e) {
				errorCode = "-2";
				LogWriter.LOGGER.severe(e.getMessage());
			} catch (Exception e) {
				errorCode = "-3";
				LogWriter.LOGGER.severe(e.getMessage());
			} finally {
				if (bubbleDS.getConnection() != null) {
					try {
						if (rs != null)
							if (!rs.isClosed())
								rs.close();
						if (!bubbleDS.isResultSetClosed())
							bubbleDS.closeResultSet();
						if (!bubbleDS.isPreparedStatementClosed())
							bubbleDS.closePreparedStatement();
						// bubbleDS.getConnection().close();
					} catch (SQLException e) {
						retval = "-3";
						LogWriter.LOGGER.severe(e.getMessage());
						// this.logWriter.appendLog("s:FSE");
						// this.logWriter.appendAdditionalInfo("UDO.getList():"+e.getMessage());
					}
				}
			}
			if (!errorCode.startsWith("0")) {
				retval = errorCode;
			}
		} else {
			retval = "-1:District cannot be null";
			// retval="-1:Division or District cannot be null";
		}
		return retval;
	}
}
