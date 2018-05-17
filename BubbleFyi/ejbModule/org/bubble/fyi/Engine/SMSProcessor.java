package org.bubble.fyi.Engine;

import java.util.Calendar;
import java.util.Date;

import org.bubble.fyi.Api.AuthenticationToken;
import org.bubble.fyi.DBOperations.SMSSender;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Initializations.LoadConfigurations;
import org.bubble.fyi.Logs.LogWriter;

/**
 * @author wasif
 *
 */
public class SMSProcessor {
	BubbleFyiDS bubbleDS;
	/**
	 * 
	 */
	public SMSProcessor(BubbleFyiDS bubbleDS) {
		this.bubbleDS=bubbleDS;
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
		SMSSender sendSMS=new SMSSender(bubbleDS);
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
		String errorCode="0005";
		JsonDecoder SMSInfo;
		if(messageBody.isEmpty()) {
			SMSInfo=new JsonDecoder(message);
		}else {
			SMSInfo=new JsonDecoder(messageBody);
		}
		if(SMSInfo.getErrorCode().equals("0")) {
			//TODO check if token is found 
			String token =SMSInfo.getJsonObject().getString("token");//TODO get token
			LogWriter.LOGGER.info("token : "+token);
			if(LoadConfigurations.authenticationTokenHM.containsKey(token)) {
				AuthenticationToken at= LoadConfigurations.authenticationTokenHM.get(token);
				String userID=at.getUserId();
				// Check validity		
				//LogWriter.LOGGER.info("validity date : "+at.getValidity().toString());
				if(checktokenValidity(at.getValidity())) {

					retval=new SMSSender(bubbleDS).insertToSenderAPI(SMSInfo,userID).getJsonObject().toString();
					errorCode="0000";
				}else{
					if(LoadConfigurations.checkValidUserToken.containsKey(userID)) 
						LoadConfigurations.checkValidUserToken.remove(userID); 

					LoadConfigurations.authenticationTokenHM.remove(token);//remove token if validity is over
					errorCode="0004";//token expired
				}				
			}else {
				errorCode="0003";//token not found
			}
		}else{
			//error decoding json
			//retval="E:JSON string invalid";
			errorCode="0009";
		}

		if(!errorCode.equals("0000")) {
			JsonEncoder jsonEncoder=new JsonEncoder();
			jsonEncoder.addElement("ErrorCode", errorCode);
			jsonEncoder.buildJsonObject();
			retval=jsonEncoder.getJsonObject().toString();
		}
		LogWriter.LOGGER.info("errorCode : "+errorCode);
		return retval;
	}

	public boolean checktokenValidity(Date tokenValidity) {
		boolean retval=false;
		Calendar date = Calendar.getInstance();
		long t= date.getTimeInMillis();
		Date currentTime=new Date(t);
		//LogWriter.LOGGER.info("currentTime : "+currentTime.toString());
		if(currentTime.compareTo(tokenValidity)<0) {
			retval=true;
		}
		return retval;
	}
}


