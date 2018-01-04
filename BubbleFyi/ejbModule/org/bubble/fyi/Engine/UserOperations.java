/**
 * 
 */
package org.bubble.fyi.Engine;

import org.bubble.fyi.DBOperations.UserDBOperations;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;

/**
 * @author hafiz
 *
 */
public class UserOperations {

	/**
	 * 
	 */
	public UserOperations() {
		// Auto-generated constructor stub
	}
	/**
	 * TODO transaction rollback in case of error
	 * @param userId
	 * @return
	 * 0 success
	 * all negative values is error.
	 */
	public String deleteUser(String userId) {
		return new UserDBOperations().deleteUser(userId);
	}
	/**
	 * 
	 * @param credential
	 * @param password
	 * @param mode
	 * @return
	 * 0:Password verified
	 * 1:User not found
	 * 2:Verificaion failed
	 * 3:Verificaion consistency error
	 * 11:Invalid mode.
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 */
	public String validatePassword(String credential, String password, String mode) {
		return new UserDBOperations().validatePassword(credential,password,mode);
	}
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return
	 * 0:Password verified
	 * 1:User not found
	 * 2:Verificaion failed
	 * 3:Verificaion consistency error
	 * 11:Invalid mode.
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 * E:JSON string invalid
	 */
	public String validatePassword(String message, String messageBody) {
		String retval="E";
		JsonDecoder credentials;
		if(messageBody.isEmpty()) {
			credentials=new JsonDecoder(message);
		}else {
			credentials=new JsonDecoder(messageBody);
		}
		if(credentials.getErrorCode().equals("0")) {
			retval=validatePassword(credentials.getJsonObject().getString("id"), credentials.getJsonObject().getString("password"), credentials.getJsonObject().getString("mode"));
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	/*
	public String getUserType(String userId) {
		return new UserDBOperations().getUserType(userId);
	}/**/
	/*
	public String getUserId(String userId, String mode) {
		return new UserDBOperations().getUserId(userId,mode);
	}/**/
	/**
	 * 
	 * @param userId
	 * @param oldPass
	 * @param newPass
	 * @return
	 *  0:Password set
	 *  1:User not found
	 *  2:Current password is invalid
	 *  3:Error encountered while setting password
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 * 
	 */
	public String modifyPasswordFunc(String userId, String oldPass, String newPass) {
		return new UserDBOperations().modifyPasswordDB(userId, oldPass, newPass);
	}
	
	public String updateCustomerStatus(String id, String customerid, String status) {
		return new UserDBOperations().modifyCustomerStatus(id, customerid, status);
	}
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return
	 *  0:Password set
	 *  1:User not found
	 *  2:Current password is invalid
	 *  3:Error encountered while setting password
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 *  E:JSON string invalid
	 */
	public String modifyPassword(String message, String messageBody) {
		String retval="E";
		JsonDecoder credentials;
		if(messageBody.isEmpty()) {
			credentials=new JsonDecoder(message);
		}else {
			credentials=new JsonDecoder(messageBody);
		}
		if(credentials.getErrorCode().equals("0")) {
			retval=modifyPasswordFunc(credentials.getJsonObject().getString("id"),credentials.getJsonObject().getString("oldPass"),credentials.getJsonObject().getString("newPass"));
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	
	public String modifyCustomerStatus(String message, String messageBody) {
		String retval="E";
		JsonDecoder credentials;
		if(messageBody.isEmpty()) {
			credentials=new JsonDecoder(message);
		}else{
			credentials=new JsonDecoder(messageBody);
		}
		if(credentials.getErrorCode().equals("0")) {
			retval=updateCustomerStatus(credentials.getJsonObject().getString("id"),credentials.getJsonObject().getString("customerid"),credentials.getJsonObject().getString("status"));
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	
	//TODO modifyEmail
	//TODO modifyPhone
	//TODO modifyAddress
	//TODO modifyCustodian
	//TODO modifyUserType (SpiderAdmin,Admin[school],Parent)
	
	// addSpiderAdmin
	//TODO modifySpiderAdmin
	// deleteSpiderAdmin
	
	//TODO addParent
	//TODO modifyParentEmail
	//TODO modifyPassword
	//TODO addStudent
	
	// list Schools (Admins)
	// list Spider Admins
	// list Students from school
	// list students under parent
	public String getDashboard(String message, String messageBody) {
		String retval="E";
		JsonDecoder Credentials;
		if(messageBody.isEmpty()) {
			Credentials=new JsonDecoder(message);			
		}else{
			Credentials=new JsonDecoder(messageBody);
		}
		retval=new UserDBOperations().getList(Credentials.getJsonObject().getString("id"),Credentials.getJsonObject().getString("userType"));		
		return retval;
	}
	
	public String getCustomerList(String message, String messageBody) {
		String retval="E";
		JsonDecoder Credentials;
		if(messageBody.isEmpty()) {
			Credentials=new JsonDecoder(message);			
		}else{
			Credentials=new JsonDecoder(messageBody);
		}
		retval=new UserDBOperations().getAllCustomerList(Credentials.getJsonObject().getString("id"));		
		return retval;
	}
	
	
	/*
	public String getSpiderAdminList() {
		return new UserDBOperations().getList("SpiderAdmin");
	}/**/
	public String getStudentListForParent(String userId) {
		if(NullPointerExceptionHandler.isNullOrEmpty(userId)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getStudentList(userId, "P");
	}
	public String getStudentListForSchool(String userId) {
		if(NullPointerExceptionHandler.isNullOrEmpty(userId)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getStudentList(userId, "S");
	}
	/**
	 * 
	 * @param phone
	 * @return
	 * An alpha numeric string of length 6
	 * -1:General Error while generating OTP
	 * -2:SQLException
	 * -3:SQLException when closing connection
	 */
	public String getNewOtp(String phone) {
		if(NullPointerExceptionHandler.isNullOrEmpty(phone)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getNewOtp(phone);
	}
	public String getStoredOtp(String phone) {
		if(NullPointerExceptionHandler.isNullOrEmpty(phone)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getStoredOtp(phone);
	}
	public String createSpiderAdmin(String message, String messageBody) {
		String retval="E";
		JsonDecoder registrationInfo;
		if(messageBody.isEmpty()) {
			registrationInfo=new JsonDecoder(message);
		}else {
			registrationInfo=new JsonDecoder(messageBody);
		}
		if(registrationInfo.getErrorCode().equals("0")) {
			retval=new UserDBOperations().createSpiderAdmin(registrationInfo);
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return Positive number indicates userId
	 * Anything else is error.
	 */
	public String createParent(String message, String messageBody) {
		String retval="E";
		JsonDecoder registrationInfo;
		if(messageBody.isEmpty()) {
			registrationInfo=new JsonDecoder(message);
		}else {
			registrationInfo=new JsonDecoder(messageBody);
		}
		if(registrationInfo.getErrorCode().equals("0")) {
			retval=new UserDBOperations().createParent(registrationInfo);
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	/**
	 * 
	 * @param message
	 * @param messageBody
	 * @return
	 *  0:OTP verified and user activated
	 *  1:OTP verification failed.
	 * -1:OTP Error
	 * -2:SQLException for update query
	 * -3:SQLException for connection close
	 * -11:Invalid mode
	 *  E:JSON string invalid
	 */
	public String otpVerifyActivateParent(String message, String messageBody) {
		String retval="E";
		JsonDecoder otp;
		if(messageBody.isEmpty()) {
			otp=new JsonDecoder(message);
		}else {
			otp=new JsonDecoder(messageBody);
		}
		if(otp.getErrorCode().equals("0")) {
			retval=new UserDBOperations().otpVerifyActivateParent(otp.getNString("id"),otp.getNString("otp"),otp.getNString("mode"));
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	public String sendSMSOTP(String message, String messageBody) {
		String retval="E";
		JsonDecoder smsOtp;
		if(messageBody.isEmpty()) {
			smsOtp=new JsonDecoder(message);
		}else {
			smsOtp=new JsonDecoder(messageBody);
		}
		if(smsOtp.getErrorCode().equals("0")) {
//			retval=new UserDBOperations().otpVerifyActivateParent(smsOtp.getNString("phone"),smsOtp.getNString("otp"));
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	public String isParentPhoneAvailable(String phone) {
		if(NullPointerExceptionHandler.isNullOrEmpty(phone)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().isParentPhoneAvailable(phone);
	}
	// create new parent
	// TODO newStudentEntry
	/**
	 * 
	 * @param message studentId,name,address,parentContact,class,section,schoolId,due,month
	 * @param messageBody
	 * @return 0 is successfully created
	 * Anything -ve is error.
	 */
	public String newStudentEntry(String message, String messageBody) {
		String retval="E:failed to process.";
		JsonDecoder json;
		if(messageBody.isEmpty()) {
			json=new JsonDecoder(message);
		}else {
			json=new JsonDecoder(messageBody);
		}
		if(json.getErrorCode().equals("0")) {
			retval=new UserDBOperations().createStudent(json);
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	/**
	 * 
	 * @param id student data id
	 * @return 0 success
	 * all negative values is error.
	 */
	public String deleteStudent(String id) {
		if(NullPointerExceptionHandler.isNullOrEmpty(id)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().deleteStudent(id);
	}
	//TODO upload list add parents and students
	/*
	feesFileInsert d
	getUploadStatus d
	getFileList d
	createStudent newStudentEntry d
	deleteStudent d
	paymentUpdate logTransaction d
	logSMS d
	*/
	/**
	 * 
	 * @param message filename,schoolId,month
	 * @param messageBody
	 * @return 0 is successfully inserted
	 * Anything -ve is error.
	 */
	public String feesFileInsert(String message, String messageBody) {
		String retval="E";
		JsonDecoder json;
		if(messageBody.isEmpty()) {
			json=new JsonDecoder(message);
		}else {
			json=new JsonDecoder(messageBody);
		}
		if(json.getErrorCode().equals("0")) {
			retval=new UserDBOperations().feesFileInsert(json);
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	/**
	 * 
	 * @param filename
	 * @return filename,schoolId,created,uploaded,status,month,estimated_upload_time,comments
	 * -ve integer is error
	 */
	public String getUploadStatus(String filename) {
		if(NullPointerExceptionHandler.isNullOrEmpty(filename)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getUploadStatus(filename);
	}
	/**
	 * 
	 * @param schoolId
	 * @return filename,schoolId,created,uploaded,status,month,estimated_upload_time,comments
	 * -ve integer is error
	 */
	public String getFileList(String schoolId) {
		if(NullPointerExceptionHandler.isNullOrEmpty(schoolId)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getFileList(schoolId);
	}
	/**
	 * 
	 * @param message userId,schoolId,sId,purpose,amount,txTime,pg,log
	 * txTime in the format "yy-mm-dd %H:mi:ss" (%y-%m-%d %H:%i:%s)
	 * sId is the data id of student
	 * @param messageBody
	 * @return
	 * when SUCCESS
	 * 0:Payment logged
	 * when FAILURE
	 * logId
	 * 
	 * Anything -ve is error
	 */
	public String logTransaction(String message, String messageBody) {
		String retval="E";
		JsonDecoder json;
		if(messageBody.isEmpty()) {
			json=new JsonDecoder(message);
		}else {
			json=new JsonDecoder(messageBody);
		}
		if(json.getErrorCode().equals("0")) {
			if(json.getEString("log").equalsIgnoreCase("success"))
				retval=new UserDBOperations().paymentUpdate(json);
			else {
				retval=new UserDBOperations().logTransaction(json);
			}
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	//TODO view transaction for parents, for school
	/**
	 * 
	 * @param userId
	 * @return dataId,studentId,name,schoolName,amount,month,status,txtime
	 * -ve is error
	 */
	public String getTransactionList(String userId) {
		if(NullPointerExceptionHandler.isNullOrEmpty(userId)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getTransactionList(userId);
	}
	/**
	 * 
	 * @param userId
	 * @return dataId,studentId,name,class,section,amount,month,status,txtime,log,purpose
	 * -ve is error
	 */
	public String getTxListSchool(String userId) {
		if(NullPointerExceptionHandler.isNullOrEmpty(userId)) return "-8:At least one parameter null or empty";
		return new UserDBOperations().getTxListSchool(userId);
	}
	//logSMS
	/**
	 * 
	 * @param message userId,smsText,sTime,dTime,sentTo,groupId
	 * @param messageBody
	 * @return logId
	 * -ve is error
	 */
	public String logSMS(String message, String messageBody) {
		String retval="E";
		JsonDecoder json;
		if(messageBody.isEmpty()) {
			json=new JsonDecoder(message);
		}else {
			json=new JsonDecoder(messageBody);
		}
		if(json.getErrorCode().equals("0")) {
			retval=new UserDBOperations().logSMS(json);
		}else{
			retval="E:JSON string invalid";
		}
		return retval;
	}
	//TODO view sms for parents, for school
	
}
