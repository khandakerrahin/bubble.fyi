/**
 * 
 */
package org.bubble.fyi.Engine;

import org.bubble.fyi.DBOperations.UserDBOperations;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;

/**
 * @author hafiz
 *
 */
public class UserOperations {
	BubbleFyiDS bubbleDS;

	/**
	 * 
	 */
	public UserOperations(BubbleFyiDS bubbleDS) {
		this.bubbleDS = bubbleDS;
		// Auto-generated constructor stub
	}

	/**
	 * TODO transaction rollback in case of error
	 * 
	 * @param userId
	 * @return 0 success all negative values is error.
	 */
	public String deleteUser(String userId) {
		return new UserDBOperations(bubbleDS).deleteUser(userId);
	}
	/**
	 * 
	 * @param credential
	 * @param password
	 * @param mode
	 * @return 0:Password verified 1:User not found 2:Verificaion failed
	 *         3:Verificaion consistency error 11:Invalid mode. -1: General Error
	 *         -2: SQLException -3: SQLException while closing connection
	 */
	/*
	 * public String validatePassword(String credential, String password, String
	 * mode) { return new
	 * UserDBOperations().validatePassword(credential,password,mode); }/
	 **/
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return 0:Password verified 1:User not found 2:Verificaion failed
	 *         3:Verificaion consistency error 11:Invalid mode. -1: General Error
	 *         -2: SQLException -3: SQLException while closing connection E:JSON
	 *         string invalid
	 */

	/*
	 * public String validatePassword(String message, String messageBody) { String
	 * retval="E"; JsonDecoder credentials; if(messageBody.isEmpty()) {
	 * credentials=new JsonDecoder(message); }else { credentials=new
	 * JsonDecoder(messageBody); } if(credentials.getErrorCode().equals("0")) {
	 * retval=validatePassword(credentials.getNString("id"),
	 * credentials.getNString("password"), credentials.getNString("mode")); }else{
	 * retval="E:JSON string invalid"; } return retval; }/
	 **/
	/*
	 * public String getUserType(String userId) { return new
	 * UserDBOperations().getUserType(userId); }/
	 **/
	/*
	 * public String getUserId(String userId, String mode) { return new
	 * UserDBOperations().getUserId(userId,mode); }/
	 **/
	/**
	 * 
	 * @param userId
	 * @param oldPass
	 * @param newPass
	 * @return 0:Password set 1:User not found 2:Current password is invalid 3:Error
	 *         encountered while setting password -1: General Error -2: SQLException
	 *         -3: SQLException while closing connection
	 * 
	 */
	public String modifyPasswordFunc(String userId, String oldPass, String newPass) {
		return new UserDBOperations(bubbleDS).modifyPasswordDB(userId, oldPass, newPass);
	}

	public String modifyProfileFunc(String id, String city, String postCode, String address, String custodianName,
			String logoFile) {
		return new UserDBOperations(bubbleDS).modifyProfileDB(id, city, postCode, address, custodianName, logoFile);

	}

	public String updateCustomerStatus(String id, String customerid, String status) {
		return new UserDBOperations(bubbleDS).modifyCustomerStatus(id, customerid, status);
	}

	public String updateBulksmsStatus(String id, String customerid, String groupId, String status) {
		return new UserDBOperations(bubbleDS).modifyBulksmsPStatus(id, customerid, groupId, status);
	}

	public String updateTargetsmsStatus(String userId, String recordId, String status, String totalCost,
			String totalReach) {
		return new UserDBOperations(bubbleDS).modifyTargetsmsPStatus(userId, recordId, status, totalCost, totalReach)
				.getJsonObject().toString();
	}

	public String updateTargetsmsStatusCustomer(String userId, String recordId, String status) {
		return new UserDBOperations(bubbleDS).modifyTargetsmsPStatusCustomer(userId, recordId, status).getJsonObject()
				.toString();
	}

	public String updateTargetsmsStatus(String id, String customerid, String groupId, String status) {
		return new UserDBOperations(bubbleDS).modifyBulksmsPStatus(id, customerid, groupId, status);
	}

	public String getTargetSMSPendingList(String id) {
		return new UserDBOperations(bubbleDS).getTargetSMSPList(id);
	}

	public String getTargetSMSPendingListCustomer(String id) {
		return new UserDBOperations(bubbleDS).getTargetSMSPListCustomer(id);
	}

	public String retryBulksmsLowBalanceStatus(String customerid, String groupId) {
		return new UserDBOperations(bubbleDS).BulksmsLowBalanceStatusChange(customerid, groupId).getJsonObject()
				.toString();
	}

	public String updateGroupList(String id, String msisdn, String editedMsisdn, String flag, String listId,
			String field2, String field3) {
		return new UserDBOperations(bubbleDS).modifyGrouplistDB(id, msisdn, editedMsisdn, flag, listId, field2, field3);
	}

	public String deleteGroupList(String id, String listId) {
		return new UserDBOperations(bubbleDS).deleteGrouplistDB(id, listId);
	}

	public String uploadLogoFile(String logoFile, String id) {
		return new UserDBOperations(bubbleDS).uploadLogoFileDB(logoFile, id);
	}

	public String createGroupDetailDB(String id, String listname) {
		return new UserDBOperations(bubbleDS).createGroupInfo(id, listname);
	}

	public String GroupSMSDetailDB(String id, String sch_date, String message, String filename) {
		return new UserDBOperations(bubbleDS).createGroupSMSInfo(id, sch_date, message, filename).getJsonObject()
				.toString();
	}

	public String OneToOneSMSDetailDB(String id, String sch_date, String filename) {
		return new UserDBOperations(bubbleDS).createOneToOneSMSInfo(id, sch_date, filename).getJsonObject().toString();
	}

	public String getFailedMsisdn(String id, String fileName, String oneToOneID, String listID, String groupID) {
		return new UserDBOperations(bubbleDS).getFailedMsisdns(id, fileName, oneToOneID, listID, groupID).getJsonObject().toString();
	}

	public String RequestPackagePurchaseDB(String id, String package_name, String price) {
		return new UserDBOperations(bubbleDS).packagePurchaseRequester(id, package_name, price).getJsonObject()
				.toString();
	}

	public String PaymentRecordUpdateDB(String id, String trxID, String price, String status) {
		return new UserDBOperations(bubbleDS).updatePaymentInfoDB(id, trxID, price, status);
	}

	public String SendSMSFromListDB(String id, String sch_date, String message, String listId) {
		return new UserDBOperations(bubbleDS).sendSMSFromList(id, sch_date, message, listId).getJsonObject().toString();
	}

	public String smsDistributionPreProcessor(String id, String groupId) {
		return new UserDBOperations(bubbleDS).smsDistributionProcessor(id, groupId);
	}

	public String SendSMStargetBasedDB(String id, String targetDV, String targetDis, String targetUpz, String sch_date,
			String message, String amount) {
		return new UserDBOperations(bubbleDS)
				.sendSMStargetBased(id, targetDV, targetDis, targetUpz, sch_date, message, amount).getJsonObject()
				.toString();
	}

	public String RequestTargetBasedSMSDB(String id, String geoTarget, String demographyTarget,
			String psychographyTarget, String behaviourTarget, String sch_date, String message, String budget,
			String targetCount) {
		return new UserDBOperations(bubbleDS).requestTargetBasedSMS(id, geoTarget, demographyTarget, psychographyTarget,
				behaviourTarget, sch_date, message, budget, targetCount).getJsonObject().toString();
	}

	/**
	 * 
	 * @param id
	 * @param sch_date
	 * @param message
	 * @param listFile
	 * @param mode
	 * @return
	 */
	public String SendScheduledSMS(String id, String sch_date, String message, String listFile, String mode) {
		if (mode.equalsIgnoreCase("1")) {
			return new UserDBOperations(bubbleDS).sendSMSFromList(id, sch_date, message, listFile).getJsonObject()
					.toString();
		} else {
			return new UserDBOperations(bubbleDS).createGroupSMSInfo(id, sch_date, message, listFile).getJsonObject()
					.toString();
		}
	}

	public String DownloadSMSReport(String id, String start_date, String end_date, String output_type) {
		return new UserDBOperations(bubbleDS).downloadSMSReport(id, start_date, end_date, output_type)
				.getJsonObject().toString();
	}
	
	public String DownloadSingleSMSReport(String id, String start_date, String end_date, String output_type) {
		return new UserDBOperations(bubbleDS).downloadSingleSMSReport(id, start_date, end_date, output_type)
				.getJsonObject().toString();
	}
	
	public String DownloadOneToOneSMSReport(String id, String start_date, String end_date, String output_type) {
		return new UserDBOperations(bubbleDS).downloadOneToOneSMSReport(id, start_date, end_date, output_type)
				.getJsonObject().toString();
	}
	
	public String DownloadFailedMsisdnReport(String user_id, String file_id) {
		return new UserDBOperations(bubbleDS).downloadFailedMsisdnReport(user_id, file_id)
				.getJsonObject().toString();
	}
	
	public String GetBulkSMSSummary(String id, String start_date, String end_date, String output_type) {
		return new UserDBOperations(bubbleDS).getBulkSMSSummary(id, start_date, end_date, output_type).getJsonObject()
				.toString();
	}

	public String GetSMSCounterDB(String id) {
		return new UserDBOperations(bubbleDS).GetSMSCounter(id).getJsonObject().toString();
	}

	public String GetSMSCounterBulkList(String id, String listId) {
		return new UserDBOperations(bubbleDS).GetSMSBulkCounter(id, listId).getJsonObject().toString();
	}

	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return 0:Password set 1:User not found 2:Current password is invalid 3:Error
	 *         encountered while setting password -1: General Error -2: SQLException
	 *         -3: SQLException while closing connection E:JSON string invalid
	 */
	public String modifyPassword(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = modifyPasswordFunc(credentials.getNString("id"), credentials.getNString("oldPass"),
					credentials.getNString("newPass"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String modifyProfile(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = modifyProfileFunc(credentials.getNString("id"), credentials.getNString("city"),
					credentials.getNString("postCode"), credentials.getNString("address"),
					credentials.getNString("custodianName"), credentials.getNString("logoFileName"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return String 0:Customer status updated 1:User not found 2:Current password
	 *         is invalid 3:Error encountered while setting password -1: General
	 *         Error -2: SQLException -8: User not Authorized update Admin status
	 *         -3: SQLException while closing connection E:JSON string invalid
	 */
	public String modifyCustomerStatus(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = updateCustomerStatus(credentials.getNString("id"), credentials.getNString("customerid"),
					credentials.getNString("status"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String modifyBulksmsPendingStatus(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = updateBulksmsStatus(credentials.getNString("id"), credentials.getNString("customerId"),
					credentials.getNString("groupId"), credentials.getNString("status"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String modifyTargetsmsStatus(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = updateTargetsmsStatus(credentials.getNString("userId"), credentials.getNString("id"),
					credentials.getNString("status"), credentials.getNString("totalCost"),
					credentials.getNString("totalReach"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String modifyTargetsmsStatusCustomer(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = updateTargetsmsStatusCustomer(credentials.getNString("userId"), credentials.getNString("id"),
					credentials.getNString("status"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String targetSMSPendingList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = getTargetSMSPendingList(credentials.getNString("id"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String targetSMSPendingListCustomer(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			// retval=getTargetSMSPendingList(credentials.getNString("id"));
			retval = getTargetSMSPendingListCustomer(credentials.getNString("id"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String lowBalanceBulkSMSResend(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = retryBulksmsLowBalanceStatus(credentials.getNString("customerId"),
					credentials.getNString("groupId"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String modifyGroupList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			String editedMsisdn = "";
			String field2 = "";
			String field3 = "";
			if (credentials.getNString("flag").equals("3")) {
				editedMsisdn = credentials.getNString("newMsisdn");
				field2 = credentials.getNString("field1");
				field3 = credentials.getNString("field2");
			} else if (credentials.getNString("flag").equals("1")) {
				field2 = credentials.getNString("field1");
				field3 = credentials.getNString("field2");
			}
			retval = updateGroupList(credentials.getNString("userId"), credentials.getNString("msisdn"), editedMsisdn,
					credentials.getNString("flag"), credentials.getNString("listId"), field2, field3);
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String deleteGroup(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = deleteGroupList(credentials.getNString("userId"), credentials.getNString("listId"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String uploadLogo(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = uploadLogoFile(credentials.getNString("logoFile"), credentials.getNString("userId"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String createGroupDetail(String message, String messageBody) {
		String retval = "E";
		JsonDecoder credentials;
		if (messageBody.isEmpty()) {
			credentials = new JsonDecoder(message);
		} else {
			credentials = new JsonDecoder(messageBody);
		}
		if (credentials.getErrorCode().equals("0")) {
			retval = createGroupDetailDB(credentials.getNString("id"), credentials.getNString("listName"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String instantGroupSMSDetail(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = GroupSMSDetailDB(json.getNString("id"), json.getNString("schedule_date"),
					json.getNString("smsText"), json.getNString("filename"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String instantOneToOneSMSDetail(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = OneToOneSMSDetailDB(json.getNString("id"), json.getNString("schedule_date"),
					json.getNString("filename"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String getFailedMsisdnList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = getFailedMsisdn(json.getNString("id"), (json.isParameterPresent("fileName")?json.getNString("fileName"):""), json.getNString("oneToOneID"), json.getNString("listID"), json.getNString("groupID"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String packagePurchaseRequester(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = RequestPackagePurchaseDB(json.getNString("id"), json.getNString("package_name"),
					json.getNString("price"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String paymentRecordUpdate(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = PaymentRecordUpdateDB(json.getNString("id"), json.getNString("trxId"), json.getNString("price"),
					json.getNString("paymentStatus"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String sentSMSFromList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = SendSMSFromListDB(json.getNString("id"), json.getNString("schedule_date"),
					json.getNString("smsText"), json.getNString("listId"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String smsDistributionInitiator(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = smsDistributionPreProcessor(json.getNString("id"), json.getNString("groupId"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String sentTargetBasedSMS(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = SendSMStargetBasedDB(json.getNString("id"), json.getNString("targetDivision"),
					json.getNString("targetDistrict"), json.getNString("targetUpazila"),
					json.getNString("schedule_date"), json.getNString("smsText"), json.getNString("amount"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String requestTargetBasedSMS(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = RequestTargetBasedSMSDB(json.getNString("id"), json.getNString("geoTarget"),
					json.getNString("demographyTarget"), json.getNString("psychographyTarget"),
					json.getNString("behaviourTarget"), json.getNString("scheduleDate"), json.getNString("smsText"),
					json.getNString("budget"), json.getNString("targetCount"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String sentSMSScheduled(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			// mode=1 list mode=2 file
			retval = SendScheduledSMS(json.getNString("id"), json.getNString("schedule_date"),
					json.getNString("smsText"), json.getNString("listFile"), json.getNString("mode"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String reportDownloadSinglesms(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			// mode=1 list mode=2 file
			retval = DownloadSingleSMSReport(json.getNString("id"), json.getNString("startDate"),
					json.getNString("endDate"), json.getNString("outputType"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String reportDownloadOneToOnesms(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			// mode=1 list mode=2 file
			retval = DownloadOneToOneSMSReport(json.getNString("id"), json.getNString("startDate"),
					json.getNString("endDate"), json.getNString("outputType"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String reportRequestSMS(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			String report_Type = json.getNString("reportId");
			
			if (report_Type.equals("0")) { // All Reports
				retval = DownloadSMSReport(json.getNString("id"), json.getNString("startDate"),
						json.getNString("endDate"), json.getNString("outputType"));
			}else if (report_Type.equals("1")) { // Single SMS Report
				// mode=1 list mode=2 file
				retval = DownloadSingleSMSReport(json.getNString("id"), json.getNString("startDate"),
						json.getNString("endDate"), json.getNString("outputType"));
			} else if (report_Type.equals("2")) { // Bulk Summary Report
				retval = GetBulkSMSSummary(json.getNString("id"), json.getNString("startDate"),
						json.getNString("endDate"), json.getNString("outputType"));
			}else if (report_Type.equals("3")) { // One-to-one Summary Report
				retval = DownloadOneToOneSMSReport(json.getNString("id"), json.getNString("startDate"),
						json.getNString("endDate"), json.getNString("outputType"));
			}else {
				retval = DownloadSingleSMSReport(json.getNString("id"), json.getNString("startDate"),
						json.getNString("endDate"), json.getNString("outputType"));
			}
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}
	
	public String requestFailedMsisdnReport(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = DownloadFailedMsisdnReport(json.getNString("user_id"), json.getNString("file_id"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}
	
	public String TotalSMSCounter(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {

			retval = GetSMSCounterDB(json.getNString("id"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String BulkSendStatus(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {

			retval = GetSMSCounterBulkList(json.getNString("id"), json.getNString("listId"));

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	// TODO modifyEmail
	// TODO modifyPhone
	// TODO modifyAddress
	// TODO modifyCustodian
	// TODO modifyUserType (SpiderAdmin,Admin[school],Parent)

	// addSpiderAdmin
	// TODO modifySpiderAdmin
	// deleteSpiderAdmin

	// TODO addParent
	// TODO modifyParentEmail
	// TODO modifyPassword
	// TODO addStudent

	// list Schools (Admins)
	// list Spider Admins
	// list Students from school
	// list students under parent
	public String getDashboard(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getList(Credentials.getNString("id"),
				Credentials.getNString("userType"));
		return retval;
	}

	public String getPaymentLog(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getPaymentRecords(Credentials.getNString("id"));
		return retval;
	}

	public String getReportInfo(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getReportRecords(Credentials.getNString("id"));
		return retval;
	}

	public String getBulkReportSummary(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		retval = GetBulkSMSSummary(json.getNString("id"), json.getNString("startDate"), json.getNString("endDate"),
				json.getNString("outputType"));
		return retval;
	}

	public String getSingleSMSReport(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getListV2(Credentials.getNString("id"),
				Credentials.getNString("userType"), Credentials.getNString("msisdn"));
		return retval;
	}
	
	public String getFailedUploadMsisdnList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getFailedMsisdnList(Credentials.getNString("id"));
		return retval;
	}
	
	public String getPreApprovedText(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getTexts(Credentials.getNString("id"),
				Credentials.getNString("userType"), Credentials.getNString("msisdn"));
		return retval;
	}

	public String getGroupList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getCustomerGroupListInfo(Credentials.getNString("id"));
		return retval;
	}

	public String getGeoList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getGeoDetail(json.getNString("targetGroup"));
		return retval;
	}

	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return
	 */
	public String getGeoLocation(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			if (json.isParameterPresent("country") && !json.isParameterPresent("division")) {
				// get division
				if (NullPointerExceptionHandler.isNullOrEmpty(json.getEString("country"))) {
					retval = "E:JSON string invalid. country value is invalid";
				} else {
					retval = new UserDBOperations(bubbleDS).getGeoLocation(json.getEString("country"));
				}
			} else if (json.isParameterPresent("division")) {
				if (json.isParameterPresent("district")) {
					// get upazilla
//					if(NullPointerExceptionHandler.isNullOrEmpty(json.getEString("division"))) {
//						retval="E:JSON string invalid. division value is invalid";
//					}else 
					if (NullPointerExceptionHandler.isNullOrEmpty(json.getEString("district"))) {
						retval = "E:JSON string invalid. district value is invalid";
					} else {
						retval = new UserDBOperations(bubbleDS).getGeoLocation("1", json.getEString("division"),
								json.getEString("district"));
					}
				} else {
					// get district
					if (NullPointerExceptionHandler.isNullOrEmpty(json.getEString("division"))) {
						retval = "E:JSON string invalid. division value is invalid";
					} else {
						retval = new UserDBOperations(bubbleDS).getGeoLocation("1", json.getEString("division"));
					}
				}
			} else {
				retval = "E:JSON string does not contain either country or division parameter";
			}

		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String getPackageList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getActivePackageList(Credentials.getNString("id"));
		return retval;
	}

	public String getBulksmsDetailC(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getBulkSMSDetailOfCustomer(Credentials.getNString("id"));
		return retval;
	}

	public String getAddressBook(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getAddressBookInfo(Credentials.getNString("id"));
		return retval;
	}

	public String getCountAddressBook(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getAddressBookCount(Credentials.getNString("id")).getJsonObject()
				.toString();
		;
		return retval;
	}

	public String getListMsisdn(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getListMsisdnFunc(Credentials.getNString("id"),
				Credentials.getNString("listId"));
		return retval;
	}

	public String getListMsisdnRow(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getListMsisdnRowFunc(Credentials.getNString("id"),
				Credentials.getNString("rowId"));
		return retval;
	}

	public String getCustomerList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getAllCustomerList(Credentials.getNString("id"));
		return retval;
	}

	// NiharekahS
	public String requestFormulaTextApproval(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).requestFormulaApproval(json.getNString("id"),
					json.getNString("smsText"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String getPendingFormulaTextApprovalList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).getAllPendingFormulaTextApprovalList(json.getNString("id"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String updatePendingFormulaTextApprovalList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).updateFormulaTextApprovalList(json.getNString("id"),
					json.getNString("textID"), json.getNString("flag"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String getFormulaTextApprovalRequestStatus(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).getUserFormulaTextApprovalRequestStatus(json.getNString("id"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String getApprovedFormulaTextList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).getAllApprovedFormulaText(json.getNString("id"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String matchFormulaText(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).checkFormulaText(json.getNString("id"),
					json.getNString("textFormula"), json.getNString("smsText"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String matchFormulaTextFromDBbyText(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).checkFormulaTextFromDBbyText(json.getNString("id"),
					json.getNString("smsText"), json.getNString("extendedRes"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String matchFormulaTextFromDBbyID(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).checkFormulaTextFromDBbyID(json.getNString("id"),
					json.getNString("textID"), json.getNString("smsText"), json.getNString("extendedRes"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}
	// NiharekahS end

	public String getPendingBulksmsList(String message, String messageBody) {
		String retval = "E";
		JsonDecoder Credentials;
		if (messageBody.isEmpty()) {
			Credentials = new JsonDecoder(message);
		} else {
			Credentials = new JsonDecoder(messageBody);
		}
		retval = new UserDBOperations(bubbleDS).getPendingBulksmsList(Credentials.getNString("id"));
		LogWriter.LOGGER.info("i am here . . . ");
		return retval;
	}

	/*
	 * public String getSpiderAdminList() { return new
	 * UserDBOperations().getList("SpiderAdmin"); }/
	 **/

	/**
	 * 
	 * @param phone
	 * @return An alpha numeric string of length 6 -1:General Error while generating
	 *         OTP -2:SQLException -3:SQLException when closing connection
	 */
	/*
	 * public String getNewOtp(String phone) {
	 * if(NullPointerExceptionHandler.isNullOrEmpty(phone)) return
	 * "-8:At least one parameter null or empty"; return new
	 * UserDBOperations().getNewOtp(phone); } public String getStoredOtp(String
	 * phone) { if(NullPointerExceptionHandler.isNullOrEmpty(phone)) return
	 * "-8:At least one parameter null or empty"; return new
	 * UserDBOperations().getStoredOtp(phone); }/
	 **/
	/*
	 * public String createSpiderAdmin(String message, String messageBody) { String
	 * retval="E"; JsonDecoder registrationInfo; if(messageBody.isEmpty()) {
	 * registrationInfo=new JsonDecoder(message); }else { registrationInfo=new
	 * JsonDecoder(messageBody); } if(registrationInfo.getErrorCode().equals("0")) {
	 * retval=new UserDBOperations().createSpiderAdmin(registrationInfo); }else{
	 * retval="E:JSON string invalid"; } return retval; }/
	 **/
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return Positive number indicates userId Anything else is error.
	 */
	/*
	 * public String createParent(String message, String messageBody) { String
	 * retval="E"; JsonDecoder registrationInfo; if(messageBody.isEmpty()) {
	 * registrationInfo=new JsonDecoder(message); }else { registrationInfo=new
	 * JsonDecoder(messageBody); } if(registrationInfo.getErrorCode().equals("0")) {
	 * retval=new UserDBOperations().createParent(registrationInfo); }else{
	 * retval="E:JSON string invalid"; } return retval; }/
	 **/
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return 0:OTP verified and user activated 1:OTP verification failed. -1:OTP
	 *         Error -2:SQLException for update query -3:SQLException for connection
	 *         close -11:Invalid mode E:JSON string invalid
	 */
	/*
	 * public String otpVerifyActivateParent(String message, String messageBody) {
	 * String retval="E"; JsonDecoder otp; if(messageBody.isEmpty()) { otp=new
	 * JsonDecoder(message); }else { otp=new JsonDecoder(messageBody); }
	 * if(otp.getErrorCode().equals("0")) { retval=new
	 * UserDBOperations().otpVerifyActivateParent(otp.getNString("id"),otp.
	 * getNString("otp"),otp.getNString("mode")); }else{
	 * retval="E:JSON string invalid"; } return retval; } public String
	 * sendSMSOTP(String message, String messageBody) { String retval="E";
	 * JsonDecoder smsOtp; if(messageBody.isEmpty()) { smsOtp=new
	 * JsonDecoder(message); }else { smsOtp=new JsonDecoder(messageBody); }
	 * if(smsOtp.getErrorCode().equals("0")) { // retval=new
	 * UserDBOperations().otpVerifyActivateParent(smsOtp.getNString("phone"),smsOtp.
	 * getNString("otp")); }else{ retval="E:JSON string invalid"; } return retval; }
	 * public String isParentPhoneAvailable(String phone) {
	 * if(NullPointerExceptionHandler.isNullOrEmpty(phone)) return
	 * "-8:At least one parameter null or empty"; return new
	 * UserDBOperations().isParentPhoneAvailable(phone); }/
	 **/
	// create new parent
	// TODO newStudentEntry
	/**
	 * 
	 * @param message     studentId,name,address,parentContact,class,section,schoolId,due,month
	 * @param messageBody
	 * @return 0 is successfully created Anything -ve is error.
	 */
	/*
	 * public String newStudentEntry(String message, String messageBody) { String
	 * retval="E:failed to process."; JsonDecoder json; if(messageBody.isEmpty()) {
	 * json=new JsonDecoder(message); }else { json=new JsonDecoder(messageBody); }
	 * if(json.getErrorCode().equals("0")) { retval=new
	 * UserDBOperations().createStudent(json); }else{
	 * retval="E:JSON string invalid"; } return retval; }/
	 **/
	/**
	 * 
	 * @param id student data id
	 * @return 0 success all negative values is error.
	 */

	/*
	 * public String deleteStudent(String id) {
	 * if(NullPointerExceptionHandler.isNullOrEmpty(id)) return
	 * "-8:At least one parameter null or empty"; return new
	 * UserDBOperations().deleteStudent(id); }/
	 **/
	// TODO upload list add parents and students
	/*
	 * feesFileInsert d getUploadStatus d getFileList d createStudent
	 * newStudentEntry d deleteStudent d paymentUpdate logTransaction d logSMS d
	 */
	/**
	 * 
	 * @param message     filename,schoolId,month
	 * @param messageBody
	 * @return 0 is successfully inserted Anything -ve is error.
	 */
	public String bubbleFileInsert(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = new UserDBOperations(bubbleDS).bubbleFileInsert(json).getJsonObject().toString();;
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return "E:JSON string invalid"
	 */
	public String uploadStatusGetter(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = getUploadStatus(json.getEString("filename"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	/**
	 * 
	 * @param filename
	 * @return filename,schoolId,created,uploaded,status,month,estimated_upload_time,comments
	 *         -ve integer is error
	 */
	public String getUploadStatus(String filename) {
		if (NullPointerExceptionHandler.isNullOrEmpty(filename))
			return "-8:At least one parameter null or empty";
		return new UserDBOperations(bubbleDS).getUploadStatus(filename);
	}

	public String uploadFileListGetter(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			retval = getFileList(json.getEString("id"), json.getEString("userType"));
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	/**
	 * 
	 * @param schoolId
	 * @return filename,schoolId,created,uploaded,status,month,estimated_upload_time,comments
	 *         -ve integer is error
	 */
	public String getFileList(String id, String userType) {
		if (NullPointerExceptionHandler.isNullOrEmpty(id))
			return "-8:At least one parameter null or empty";
		return new UserDBOperations(bubbleDS).getFileList(id, userType);
	}
	// TODO
	// TODO
	// TODO
	/**
	 * 
	 * @param message     userId,schoolId,sId,purpose,amount,txTime,pg,log txTime in
	 *                    the format "yy-mm-dd %H:mi:ss" (%y-%m-%d %H:%i:%s) sId is
	 *                    the data id of student
	 * @param messageBody
	 * @return when SUCCESS 0:Payment logged when FAILURE logId
	 * 
	 *         Anything -ve is error
	 */
	/*
	 * public String logTransaction(String message, String messageBody) { String
	 * retval="E"; JsonDecoder json; if(messageBody.isEmpty()) { json=new
	 * JsonDecoder(message); }else { json=new JsonDecoder(messageBody); }
	 * if(json.getErrorCode().equals("0")) {
	 * if(json.getEString("log").equalsIgnoreCase("success")) retval=new
	 * UserDBOperations().paymentUpdate(json); else { retval=new
	 * UserDBOperations().logTransaction(json); } }else{
	 * retval="E:JSON string invalid"; } return retval; }/
	 **/
	// TODO view transaction for parents, for school
	/**
	 * 
	 * @param userId
	 * @return dataId,studentId,name,schoolName,amount,month,status,txtime -ve is
	 *         error
	 */
	/*
	 * public String getTransactionList(String userId) {
	 * if(NullPointerExceptionHandler.isNullOrEmpty(userId)) return
	 * "-8:At least one parameter null or empty"; return new
	 * UserDBOperations(bubbleDS).getTransactionList(userId); }/
	 **/

	// logSMS
	/**
	 * 
	 * @param message     userId,smsText,sTime,dTime,sentTo,groupId
	 * @param messageBody
	 * @return logId -ve is error
	 */
	/*
	 * public String logSMS(String message, String messageBody) { String retval="E";
	 * JsonDecoder json; if(messageBody.isEmpty()) { json=new JsonDecoder(message);
	 * }else { json=new JsonDecoder(messageBody); }
	 * if(json.getErrorCode().equals("0")) { retval=new
	 * UserDBOperations().logSMS(json); }else{ retval="E:JSON string invalid"; }
	 * return retval; }/
	 **/

	/**
	 * 
	 * @param message     (id,lastIndex,order: 1 desc, 0 asc)
	 * @param messageBody
	 * @return
	 */
	public String getInbox(String message, String messageBody) {
		String retval = "E";
		JsonDecoder json;
		if (messageBody.isEmpty()) {
			json = new JsonDecoder(message);
		} else {
			json = new JsonDecoder(messageBody);
		}
		if (json.getErrorCode().equals("0")) {
			try {
				long li = Long.parseLong(json.isParameterPresent("lastIndex") ? json.getEString("lastIndex") : "0");

				boolean isDescending;
				if (json.isParameterPresent("order")) {
					if (json.getEString("order").equalsIgnoreCase("1")
							|| json.getEString("order").equalsIgnoreCase("desc")) {
						isDescending = true;
					} else if (json.getEString("order").equalsIgnoreCase("0")
							|| json.getEString("order").equalsIgnoreCase("asc")) {
						isDescending = false;
					} else {
						isDescending = true;
					}
				} else {
					isDescending = true;
				}
				if (json.isParameterPresent("id")) {
					if (!NullPointerExceptionHandler.isNullOrEmpty(json.getEString("id"))) {
						retval = getInbox(json.getEString("id"), li, isDescending);
					} else {
						retval = "E:JSON string invalid, id value is invalid";
					}
				} else {
					retval = "E:JSON string invalid, id missing";
				}
			} catch (NumberFormatException nfe) {
				retval = "E:JSON string invalid, lastIndex NumberFormatException";
			}
		} else {
			retval = "E:JSON string invalid";
		}
		return retval;
	}

	public String getInbox(String userId, long lastIndex, boolean isDescending) {
		if (NullPointerExceptionHandler.isNullOrEmpty(userId))
			return "-8:At least one parameter null or empty";
		if (lastIndex < 0)
			lastIndex = 0;
		return new UserDBOperations(bubbleDS).getInbox(userId, lastIndex, isDescending);
	}

}
