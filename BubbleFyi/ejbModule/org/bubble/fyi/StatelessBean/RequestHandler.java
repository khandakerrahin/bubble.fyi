package org.bubble.fyi.StatelessBean;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.bubble.fyi.Initializations.LoadConfigurations;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.LoginProcessor;
import org.bubble.fyi.StatelessBean.RequestHandlerLocal;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;

import javax.ejb.Startup;

import org.bubble.fyi.Engine.RegistrationProcessor;
import org.bubble.fyi.Engine.SMSProcessor;
import org.bubble.fyi.Engine.UserOperations;
/**
 * Session Bean implementation class RequestHandler
 */
/*
@Stateless(name = "Bubble.Fyi")
@TransactionManagement(TransactionManagementType.BEAN)
@LocalBean
@Asynchronous
public class RequestHandler implements RequestHandlerRemote, RequestHandlerLocal {
    public RequestHandler() {
        // TODO Auto-generated constructor stub
    }

}/**/


/**
 * Session Bean implementation class
 * @author wasif
 * @implements RequestHandlerLocal session interface.
 * @see RequestHandlerLocal
 */
@Stateless
@Startup
//@Interceptors(value = org.Spider.Utility.monitor.MessageMonitor.class)
public class RequestHandler implements RequestHandlerLocal {

	public LoadConfigurations loadConf;
	LogWriter logWriter;
	String replyAddr="Bubble.Fyi";
	String appName="Bubble.Fyi";
	String channel;
	public RequestHandler() {

	}

	@PostConstruct
	public void loadConfiguration() {
		try {

		} catch (Exception ex) {
			LogWriter.LOGGER.severe(ex.getMessage());
		}
	}

	/**
	 * Implementation method to process new request.
	 * @param msg
	 * @param loadConf
	 * @param force
	 * @return The result of the processing. String value.
	 * @throws JMSException
	 * @throws Exception
	 * @see RequestHandlerLocal
	 */
	//@Override
	public String processNewRequest(MapMessage msg, LoadConfigurations loadConf, boolean forceLogWrite) throws JMSException, Exception  {
		//request example  
		//login https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=fees.school&destinationType=queue&clientid=fees.school&target=ENGINE&LoadConf=N&message={%20%22username%22:%22t1@sp.com%22,%20%22password%22:%22specialt1pass%22,%20%22mode%22:%221%22}&reply=true&action=login
		//school registration: https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=fees.school&destinationType=queue&clientid=fees.school&target=ENGINE&LoadConf=N&message={%20%22schoolName%22:%22Skola%201%22,%22email%22:%22spiderco@sdxb.com%22,%22phone%22:%228801912345678%22,%22password%22:%22spidercom%22,%22custodianName%22:%22SpiderCom%22,%22address%22:%2210A%20Dhanmondi%22,%22city%22:%22Dhaka%22,%22postcode%22:%221209%22}&reply=true&action=registerSchool
		this.logWriter=new LogWriter(forceLogWrite);
		String message 	=	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("message"))?"":msg.getString("message");
		String action 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("action")) ?"":msg.getString("action");
		String messageBody=	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("body"))?"":msg.getString("body");
		String isTest 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("isTest"))?"":msg.getString("isTest");
		String src   	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("src"))?"":msg.getString("src");
		String target  	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("target"))?"":msg.getString("target");
		String traceON 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("traceON"))?"":msg.getString("traceON");
		String channel 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("channel"))?"default":msg.getString("channel");
		boolean isTestEnv = false;
		this.logWriter.setChannel(channel);
		this.logWriter.setTarget(target);
		this.logWriter.setSource(src);
		this.logWriter.setAction(action);

		this.loadConf = loadConf;
		String retVal =  "10000";
		LogWriter.LOGGER.info("Message :"+message);
		LogWriter.LOGGER.info("Message Body :"+messageBody);
		BubbleFyiDS bubbleDS = new BubbleFyiDS();
		try {
			if(action.equalsIgnoreCase("login")) {//documented
				//json: 
				//example: { "username":"t1@sp.com", "password":"specialt1pass", "mode":"1"}
				retVal=new LoginProcessor(bubbleDS).processLogin(message,messageBody);
			}else if(action.equalsIgnoreCase("registerUser")) {//documented			
				retVal=new RegistrationProcessor(bubbleDS).processCustomerRegistration(message,messageBody);
			}else if(action.equalsIgnoreCase("sendSMS")) {//documented			
				retVal=new SMSProcessor(bubbleDS).processSendSMS(message,messageBody);
			}else if(action.equalsIgnoreCase("sendSMSv2")) {//documented			
				retVal=new SMSProcessor(bubbleDS).processSendSMSv2(message,messageBody);
			}else if(action.equalsIgnoreCase("getReports")) {//documented
				retVal=new UserOperations(bubbleDS).getDashboard(message,messageBody);
			}else if(action.equalsIgnoreCase("modifyPassword")) {
				retVal=new UserOperations(bubbleDS).modifyPassword(message,messageBody);
			}else if(action.equalsIgnoreCase("updateProfile")) {
				retVal=new UserOperations(bubbleDS).modifyProfile(message,messageBody);
			}else if(action.equalsIgnoreCase("customerList")) {//documented
				retVal=new UserOperations(bubbleDS).getCustomerList(message,messageBody);			
			}else if(action.equalsIgnoreCase("updateCustomerStatus")) {
				retVal=new UserOperations(bubbleDS).modifyCustomerStatus(message,messageBody);
			}else if(action.equalsIgnoreCase("createList")) {//documented	
				retVal=new UserOperations(bubbleDS).createGroupDetail(message,messageBody); 
			}else if(action.equalsIgnoreCase("getGroupList")) {//documented	
				retVal=new UserOperations(bubbleDS).getGroupList(message,messageBody); 
			}else if(action.equalsIgnoreCase("bubbleFileInsert")) {
				retVal=new UserOperations(bubbleDS).bubbleFileInsert(message,messageBody);
			}else if(action.equalsIgnoreCase("getUploadStatus")) {
				retVal=new UserOperations(bubbleDS).uploadStatusGetter(message,messageBody);
			}else if(action.equalsIgnoreCase("getUploadStatusV2")) {
				retVal=new UserOperations(bubbleDS).uploadStatusGetterV2(message,messageBody);
			}else if(action.equalsIgnoreCase("getFileList")) {
				retVal=new UserOperations(bubbleDS).uploadFileListGetter(message,messageBody);
			}else if(action.equalsIgnoreCase("sendBulkSMSInstant")) {// used for both group instant sms and group scheduled sms //TODO
				retVal=new UserOperations(bubbleDS).instantGroupSMSDetail(message,messageBody); 
			}else if(action.equalsIgnoreCase("sendOneToOneBulkSMSInstant")) {// used for both oneToOne instant sms and oneToOne scheduled sms //TODO
				retVal=new UserOperations(bubbleDS).instantOneToOneSMSDetail(message,messageBody); 
			}else if(action.equalsIgnoreCase("getFailedMsisdnList")) {
				retVal=new UserOperations(bubbleDS).getFailedMsisdnList(message,messageBody); 
			}		
			/*
			else if(action.equalsIgnoreCase("sendScheduledSMS")) { //TODO Not in Use old version used Scheduled for bulk sms redundant;
				retVal=new UserOperations(bubbleDS).sentSMSScheduled(message,messageBody); 	
			}/**/
			else if(action.equalsIgnoreCase("getTotalSMSCount")) {

				//retVal=new LoginProcessor().processLogin(message,messageBody);
				retVal=new UserOperations(bubbleDS).TotalSMSCounter(message,messageBody); 
				//TODO Changed on 20180312
			}else if(action.equalsIgnoreCase("getPendingBulksmsList")) {
				retVal=new UserOperations(bubbleDS).getPendingBulksmsList(message,messageBody);
			}else if(action.equalsIgnoreCase("updatePendingBulksmsList")) {
				retVal=new UserOperations(bubbleDS).modifyBulksmsPendingStatus(message,messageBody);			
			}
			else if(action.equalsIgnoreCase("sendBulksmsFromList")) { // both group sms scheduled or not scheduled
				retVal=new UserOperations(bubbleDS).sentSMSFromList(message,messageBody); 			
			}else if(action.equalsIgnoreCase("getBulksmsDetail")) {//documented	
				retVal=new UserOperations(bubbleDS).getBulksmsDetailC(message,messageBody); 
			}else if(action.equalsIgnoreCase("getCustomizedSMSDetail")) {//documented	
				retVal=new UserOperations(bubbleDS).getCustomizedSMSDetail(message,messageBody); 
			}else if(action.equalsIgnoreCase("getBulkSendStatus")) {
				retVal=new UserOperations(bubbleDS).BulkSendStatus(message,messageBody); 
			}else if(action.equalsIgnoreCase("getAddressBook")) {//documented	
				retVal=new UserOperations(bubbleDS).getAddressBook(message,messageBody); 	
			}else if(action.equalsIgnoreCase("getListMsisdn")) {//documented	
				retVal=new UserOperations(bubbleDS).getListMsisdn(message,messageBody); 
			}
			/**/
			else if(action.equalsIgnoreCase("getListMsisdnRow")) {
				retVal=new UserOperations(bubbleDS).getListMsisdnRow(message,messageBody);			
			}
			/**/else if(action.equalsIgnoreCase("modifyGroupList")) {
				retVal=new UserOperations(bubbleDS).modifyGroupList(message,messageBody);			
			}
			else if(action.equalsIgnoreCase("totalCountAddressBook")) {//documented	
				retVal=new UserOperations(bubbleDS).getCountAddressBook(message,messageBody); 
			}else if(action.equalsIgnoreCase("uploadLogo")) {//documented	
				retVal=new UserOperations(bubbleDS).uploadLogo(message,messageBody); 
			}else if(action.equalsIgnoreCase("singleSMSReport")) {//documented
				retVal=new UserOperations(bubbleDS).getSingleSMSReport(message,messageBody);
			}else if(action.equalsIgnoreCase("getFailedUploadMsisdnList")) {
				retVal=new UserOperations(bubbleDS).getFailedUploadMsisdnList(message,messageBody);
			}else if(action.equalsIgnoreCase("requestFailedMsisdnReport")) {	
				retVal=new UserOperations(bubbleDS).requestFailedMsisdnReport(message,messageBody); 
			}else if(action.equalsIgnoreCase("deleteGroup")) {//documented
				//retVal=new UserOperations().getSingleSMSReport(message,messageBody);
				retVal=new UserOperations(bubbleDS).deleteGroup(message,messageBody);
			}else if(action.equalsIgnoreCase("getActivePackage")) {//documented	
				retVal=new UserOperations(bubbleDS).getPackageList(message,messageBody); 
			}else if(action.equalsIgnoreCase("initiatePackagePurchase")) {//documented	
				retVal=new UserOperations(bubbleDS).packagePurchaseRequester(message,messageBody); 
			}else if(action.equalsIgnoreCase("setPaymentResponse")) {//documented	
				retVal=new UserOperations(bubbleDS).paymentRecordUpdate(message,messageBody); 
			}else if(action.equalsIgnoreCase("lowBalanceBulkSMSResend")) {
				retVal=new UserOperations(bubbleDS).lowBalanceBulkSMSResend(message,messageBody);			
			}else if(action.equalsIgnoreCase("getPaymentReports")) {//documented
				retVal=new UserOperations(bubbleDS).getPaymentLog(message,messageBody);
			}else if(action.equalsIgnoreCase("getUserMaskings")) {//TODO documentation
				retVal=new UserOperations(bubbleDS).getUserMaskings(message,messageBody);
			}else if(action.equalsIgnoreCase("requestSingleSMSReport")) {//documented	
				retVal=new UserOperations(bubbleDS).reportDownloadSinglesms(message,messageBody); 
			}else if(action.equalsIgnoreCase("requestBulkSMSSummaryReport")) {//documented
				retVal=new UserOperations(bubbleDS).getBulkReportSummary(message,messageBody);
			}else if(action.equalsIgnoreCase("requestOneToOneSMSReport")) {//documented	
				retVal=new UserOperations(bubbleDS).reportDownloadOneToOnesms(message,messageBody); 
			}else if(action.equalsIgnoreCase("downloadReports")) {//documented
				retVal=new UserOperations(bubbleDS).getReportInfo(message,messageBody);
			}else if(action.equalsIgnoreCase("getGeoLocation")) {//documented	
				retVal=new UserOperations(bubbleDS).getGeoList(message,messageBody); 
			}else if(action.equalsIgnoreCase("requestSMSReport")) {//documented	
				retVal=new UserOperations(bubbleDS).reportRequestSMS(message,messageBody); 
			}else if(action.equalsIgnoreCase("targetBasedSMSRequest")) {//TODO change t
				retVal=new UserOperations(bubbleDS).sentTargetBasedSMS(message,messageBody); 			
			}else if(action.equalsIgnoreCase("targetBasedSMSRequestV2")) {
				retVal=new UserOperations(bubbleDS).requestTargetBasedSMS(message,messageBody); 
			}else if(action.equalsIgnoreCase("getTargetSMSPendingList")) {
				retVal=new UserOperations(bubbleDS).targetSMSPendingList(message,messageBody);			
			}else if(action.equalsIgnoreCase("updateTargetSMSList")) { //update by sales admin
				retVal=new UserOperations(bubbleDS).modifyTargetsmsStatus(message,messageBody);			
			}else if(action.equalsIgnoreCase("getTargetPendingListCustomer")) {
				retVal=new UserOperations(bubbleDS).targetSMSPendingListCustomer(message,messageBody);	
			}else if(action.equalsIgnoreCase("updateTargetSMSListCustomer")) {
				retVal=new UserOperations(bubbleDS).modifyTargetsmsStatusCustomer(message,messageBody);			
			}else if(action.equalsIgnoreCase("getAllTargetSMSList")) { //ADMIN VIEW
				//TODO pending
				retVal=new UserOperations(bubbleDS).modifyTargetsmsStatusCustomer(message,messageBody);			
			}
			//SMS inbound
			else if(action.equalsIgnoreCase("getInbox")) {
				retVal=new UserOperations(bubbleDS).getInbox(message,messageBody); 			
			}
			//Get district and upazillas
			else if(action.equalsIgnoreCase("getGeoLocation2")) {//documented	
				retVal=new UserOperations(bubbleDS).getGeoLocation(message,messageBody); 
			}
			//APIs for sms Sending through API
			else if(action.equalsIgnoreCase("authenticate")) {
				retVal=new LoginProcessor(bubbleDS).authenticateUser(message,messageBody);
			}else if(action.equalsIgnoreCase("sendSMSApi")) {//documented			
				retVal=new SMSProcessor(bubbleDS).processSendSMSapi(message,messageBody);
			}else if(action.equalsIgnoreCase("cleanExpiredRecords")) {//documented
				//TODO Pending
				retVal=new LoginProcessor(bubbleDS).cleanExpiredRecords();
			}/** API used by bulk sms Distributer Application **/

			else if(action.equalsIgnoreCase("smsDistributer")) { // both group sms scheduled or not scheduled
				retVal=new UserOperations(bubbleDS).smsDistributionInitiator(message,messageBody); 
				
			}

			// NiharekahS
			else if(action.equalsIgnoreCase("requestFormulaTextApproval")) {
				retVal=new UserOperations(bubbleDS).requestFormulaTextApproval(message,messageBody);			
			}else if(action.equalsIgnoreCase("getPendingFormulaTextApprovalList")) {
				retVal=new UserOperations(bubbleDS).getPendingFormulaTextApprovalList(message,messageBody);			
			}else if(action.equalsIgnoreCase("updatePendingFormulaTextApprovalList")) {
				retVal=new UserOperations(bubbleDS).updatePendingFormulaTextApprovalList(message,messageBody);			
			}else if(action.equalsIgnoreCase("getFormulaTextApprovalRequestStatus")) {
				retVal=new UserOperations(bubbleDS).getFormulaTextApprovalRequestStatus(message,messageBody);			
			}else if(action.equalsIgnoreCase("getApprovedFormulaTextList")) {
				retVal=new UserOperations(bubbleDS).getApprovedFormulaTextList(message,messageBody);			
			}else if(action.equalsIgnoreCase("matchFormulaText")) {
				retVal=new UserOperations(bubbleDS).matchFormulaText(message,messageBody);			
			}else if(action.equalsIgnoreCase("matchFormulaTextFromDBbyText")) {
				retVal=new UserOperations(bubbleDS).matchFormulaTextFromDBbyText(message,messageBody);			
			}else if(action.equalsIgnoreCase("matchFormulaTextFromDBbyID")) {
				retVal=new UserOperations(bubbleDS).matchFormulaTextFromDBbyID(message,messageBody);			
			}
			
			// NiharekahS end


			/*else if(action.equalsIgnoreCase("CheckloginAPI")) {
				retVal=new LoginProcessor(bubbleDS).processLoginAPI(message,messageBody);
			}/**/

			else retVal="I:Invalid action";
		}finally{
			if(bubbleDS.getConnection() != null){
				//this.logWriter.flush(bubbleDS);
				try {
					bubbleDS.getConnection().close();
				} catch (SQLException e) {
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}

		LogWriter.LOGGER.info("retVal: "+retVal);
		return retVal;
	}

	/**
	 * Gets the date today T + i days
	 * @param tPlusD
	 * @return date in yyyyMMdd format
	 */
	public String getDate(int tPlusD) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, tPlusD);
		java.util.Date dt = cal.getTime();
		SimpleDateFormat sdm = new SimpleDateFormat("yyyyMMdd");
		String datePart = sdm.format(dt);
		return  datePart;
	}
}




