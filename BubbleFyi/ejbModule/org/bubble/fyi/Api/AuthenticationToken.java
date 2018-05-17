package org.bubble.fyi.Api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bubble.fyi.Initializations.LoadConfigurations;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Utilities.RandomStringGenerator;

public class AuthenticationToken {
	String tokenId;
	String UserId;
	Date validity;
	String type;
	String status;

	public AuthenticationToken() {
		this.type="0";
		this.status="0";
	}

	public String generateNewTokenId(String userId) {		
		DateFormat df = new SimpleDateFormat("ddHHmmssSSS");
		Date dateobj = new Date();
		this.tokenId = generateSalt() + df.format(dateobj)+generateSalt();	
		LogWriter.LOGGER.severe("TOKEN : "+this.tokenId);
		
		long ONE_MINUTE_IN_MILLIS=60000;//millisecs
		Calendar date = Calendar.getInstance();
		long t= date.getTimeInMillis();
		Date validitySetter=new Date(t + (10 * ONE_MINUTE_IN_MILLIS)); //Adding 10 min token validity		
		setUserId(userId);
		setValidity(validitySetter);
		setTokenId(this.tokenId);
		//LoadConfigurations.authenticationTokenHM.put(this.tokenId, this);
		return this.tokenId;
	}

	private String generateSalt() {
		return RandomStringGenerator.getRandomString("1234567890ABEDEFGHIJKLMNOPQRSTUVWXYZ~$@*!-abcdefghijklmnopqrstuvwxyz", 6);
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public Date getValidity() {
		return validity;
	}

	public void setValidity(Date validity) {
		this.validity = validity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
