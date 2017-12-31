/**
 * 
 */
package org.bubble.fyi.Engine;

import org.bubble.fyi.DBOperations.UserRegistration;

/**
 * @author wasif
 *
 */
public class RegistrationProcessor {

	/**
	 * 
	 */
	public RegistrationProcessor() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @json schoolName,email,phone,password,custodianName,address,city,postcode
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
	public String processCustomerRegistration(String message, String messageBody) {
		String retval="E";
		JsonDecoder registrationInfo;
		if(messageBody.isEmpty()) {
			registrationInfo=new JsonDecoder(message);
		}else {
			registrationInfo=new JsonDecoder(messageBody);
		}
		UserRegistration register=new UserRegistration();
		if(registrationInfo.getErrorCode().equals("0")) {
			retval=register.registerNewUser(registrationInfo);
		}else{
			//error decoding json
			retval="E:JSON string invalid";
		}
		return retval;
	}
}
