package org.bubble.fyi.Engine;

import org.bubble.fyi.DBOperations.SMSSender;

/**
 * @author wasif
 *
 */
public class SMSProcessor {

	/**
	 * 
	 */
	public SMSProcessor() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @json bparty,userid,message
	 * @example { "schoolName":"Skola 1","email":"spiderco@sdxb.com","phone":"8801912345678","password":"spidercom","custodianName":"SpiderCom","address":"10A Dhanmondi","city":"Dhaka","postcode":"1209"}
	 * @requestParameters action=registerSchool&message=jsonDecoder
	 * @param message
	 * @param messageBody
	 * @return 0:Successfully Inserted
	 * <br>1:User with the email address or phone number exists
	 * <br>2:Inserting organization details failed
	 * <br>11:Inserting user credentials failed
	 * <br>E:JSON string invalid
	 * <br>-1:Default Error Code
	 * <br>-2:SQLException
	 * <br>-3:General Exception
	 * <br>-4:SQLException while closing connection
	 */
	public String processSendSMS(String message, String messageBody) {
		String retval="E";
		JsonDecoder SMSInfo;
		if(messageBody.isEmpty()) {
			SMSInfo=new JsonDecoder(message);
		}else {
			SMSInfo=new JsonDecoder(messageBody);
		}
		SMSSender sendSMS=new SMSSender();
		if(SMSInfo.getErrorCode().equals("0")) {
			retval=sendSMS.insertToSender(SMSInfo);
		}else{
			//error decoding json
			retval="E:JSON string invalid";
		}
		return retval;
	}
	
	public String processSendSMSapi(String message, String messageBody) {
		String retval="E";
		JsonDecoder SMSInfo;
		if(messageBody.isEmpty()) {
			SMSInfo=new JsonDecoder(message);
		}else {
			SMSInfo=new JsonDecoder(messageBody);
		}
		//SMSSender sendSMS=new SMSSender();
		if(SMSInfo.getErrorCode().equals("0")) {
			retval=new SMSSender().insertToSenderAPI(SMSInfo);
		}else{
			//error decoding json
			retval="E:JSON string invalid";
		}
		return retval;
	}
}
