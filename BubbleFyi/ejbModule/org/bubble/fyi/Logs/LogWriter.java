package org.bubble.fyi.Logs;

import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.NamingException;



/**
 * Class to set and insert the log into app_class
 * <br>Members are the fields of table app_log
 * @author hafiz
 *
 */
public class LogWriter {
	public static final Logger LOGGER = Logger.getLogger(LogWriter.class.getName());
	/* All the fields from table app_log */
	String msisdn          ;
	String channel         ;
	String status          ;
	/** Each field starts with ';' . For example, ;o6012 indicates offerId 6012. */
	String log             ;
	String action          ;
	String offerId         ;
	String source;
	String target;
	String refillId;
	String serviceId;
	String subscriptionTime;
	String expireTime;
	String insertTime;
	String miscellaneous;
	String id;
	String newSubscriptionTime;
	String failCounter;
	boolean force;
	private String subString(String str,int endIndex){
		return str.substring(0, str.length()<endIndex?str.length():endIndex);
	}
	public LogWriter(boolean force){
		this.id          ="null";
		this.msisdn          ="";
		this.channel         ="";
		this.status          ="";
		this.log             ="";
		this.action          ="";
		this.source          ="";
		this.target			 ="";
		this.refillId		="";
		this.serviceId		="";
		this.offerId		="";
		this.miscellaneous 	 ="";
		this.force=force;
		this.subscriptionTime="null";
		this.expireTime="null";
		this.insertTime="null";
		this.newSubscriptionTime="null";
		this.failCounter="null";
	}
	/**
	 * Clears the log to default value as in the constructor.
	 */
	public void clear(){
		this.id          ="null";
		this.msisdn          ="";
		this.channel         ="";
		this.status          ="";
		this.log             ="";
		this.action          ="";
		this.source          ="";
		this.target			 ="";
		this.refillId		="";
		this.serviceId		="";
		this.offerId		="";
		this.miscellaneous 	 ="";
		//this.force=force;
		this.subscriptionTime="null";
		this.expireTime="null";
		this.insertTime="null";
		this.newSubscriptionTime="null";
		this.failCounter="null";
	}
	/**
	 * Changes any null input to some default value.
	 */
	private void handleNull(){
//		id          =NullPointExceptionHendler.IsNullOrEmpty(id)?"null":id;
//		msisdn          =NullPointExceptionHendler.IsNullOrEmpty(msisdn)?"":msisdn;
//		channel         =NullPointExceptionHendler.IsNullOrEmpty(channel)?"":channel;
//		status          =NullPointExceptionHendler.IsNullOrEmpty(status)?"":status;
//		log             =NullPointExceptionHendler.IsNullOrEmpty(log)?"":log;
//		action          =NullPointExceptionHendler.IsNullOrEmpty(action)?"":action;
//		offerId        =NullPointExceptionHendler.IsNullOrEmpty(offerId)?"":offerId;
//		source          =NullPointExceptionHendler.IsNullOrEmpty(source)?"":source;
//		target          =NullPointExceptionHendler.IsNullOrEmpty(target)?"":target;
//		refillId          =NullPointExceptionHendler.IsNullOrEmpty(refillId)?"":refillId;
//		serviceId          =NullPointExceptionHendler.IsNullOrEmpty(serviceId)?"":serviceId;
//		miscellaneous   =NullPointExceptionHendler.IsNullOrEmpty(miscellaneous)?"":miscellaneous;
//		subscriptionTime   =NullPointExceptionHendler.IsNullOrEmpty(subscriptionTime)?"null":subscriptionTime;
//		expireTime   =NullPointExceptionHendler.IsNullOrEmpty(expireTime)?"null":expireTime;
//		insertTime   =NullPointExceptionHendler.IsNullOrEmpty(insertTime)?"null":insertTime;
//		newSubscriptionTime   =NullPointExceptionHendler.IsNullOrEmpty(newSubscriptionTime)?"null":newSubscriptionTime;
//		failCounter          =NullPointExceptionHendler.IsNullOrEmpty(failCounter)?"null":failCounter;
	}
	/**
	 * Handles null input and 
	 * <br>Truncates string input to their length bounds in table app_log 
	 */
	private void validateVariableBounds(){
		handleNull();
		msisdn          =subString(msisdn          ,13);
		channel         =subString(channel         ,50);
		status          =subString(status          ,1) ;
		log             =subString(log.replaceAll("'", "''")             ,160);
		action          =subString(action          ,30);
		offerId        =subString(offerId        ,36);
		source          =subString(source          ,50);
		target          =subString(target          ,30);
		serviceId		=subString(serviceId 	   ,30);
		refillId		=subString(refillId 	   ,30);
		miscellaneous   =subString(miscellaneous.replaceAll("'", "''")      ,1000);
		
	}
	
	/**
	 * Inserts the log into table app_log after calling validateVariableBounds()
	 * @return true if insert successful; false otherwise
	 * @throws NamingException
	 * @throws SQLException
	 */
	public boolean flush() throws NamingException {
		boolean retval=false;
		this.validateVariableBounds();
//		SimpleDataSource sData = new SimpleDataSource(org.Banglalink.InhouseUtility.dataSource.DSInfo.getDS_INPATHS_NEW());
		String sql = "insert into quick_pack.application_log"
				+"(id,msisdn, subscription_time, insert_time, expire_time, status, action, log, service_id,refill_id,offer_id,source,target,channel,"
				+ "miscellaneous,new_subscription_time,fail_counter)"
				+"values("
				+id
				+",'"+msisdn+"'"
				+","+((subscriptionTime.equals("null")||subscriptionTime.equals("sysdate"))?subscriptionTime:"to_date('"+subscriptionTime+"','yyyymmddhh24miss')")//"sysdate"//subscriptionTime
				+","+((insertTime.equals("null")||insertTime.equals("sysdate"))?insertTime:"to_date('"+insertTime+"','yyyymmddhh24miss')")//"sysdate"//insertTime
				+","+((expireTime.equals("null")||expireTime.startsWith("sysdate"))?expireTime:"to_date('"+expireTime+"','yyyymmddhh24miss')")//"sysdate"//expireTime
				+",'"+status+"'"
				+",'"+action+"'"
				+",'"+log+"'"
				+",'"+serviceId+"'"
				+",'"+refillId+"'"
				+",'"+offerId+"'"
				+",'"+source+"'"
				+",'"+target+"'"
				+",'"+channel+"'"
				+",'"+miscellaneous+"'"
				+","+((newSubscriptionTime.equals("null")||newSubscriptionTime.equals("sysdate"))?newSubscriptionTime:"to_date('"+newSubscriptionTime+"','yyyymmddhh24miss')")//"sysdate"//newSubscriptionTime
				+","+failCounter
				+ ")";
		LOGGER.info("Fees.School: "+"LogWriter Sql:"+sql);
		int retVal=-1;
		try {
//			retVal = sData.executeSQL(sql);
		} catch (/*SQLException e*/Exception e) {
			e.printStackTrace();
			LOGGER.severe("Fees.School: "+"LogWriter.flush() SQLException");
		}
		retval=(retVal>0)?true:false;
		LOGGER.severe("Fees.School: "+"LogWriter"+"flush() Response:"+retVal);
		return retval;
	}

	/**
	 * @param msisdn the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @param log the log to set
	 */
	public void setLog(String log) {
		this.log = log;
	}
	/**
	 * @param log the log to set
	 * Appends to log
	 * If first log, ""+newLog
	 * else log=log+log
	 */
	public void appendLog(String newLog) {
		this.log = this.log+newLog;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	/**
	 * @param offer_id the offer_id to set
	 */
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	/**
	 * @param force the force to set
	 */
	public void setForce(boolean force) {
		this.force = force;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	/**
	 * @param log the log to set
	 */
	public void setMiscellaneous(String miscellaneous) {
		this.miscellaneous = miscellaneous;
	}
	/**
	 * @param log the log to set
	 * Appends to log
	 * If first log, ""+newLog
	 * else log=log+log
	 */
	public void appendMiscellaneous(String miscellaneous) {
		this.miscellaneous = this.miscellaneous+miscellaneous;
	}
	/**
	 * @param refillId the refillId to set
	 */
	public void setRefillId(String refillId) {
		this.refillId = refillId;
	}
	/**
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	/**
	 * @param subscriptionTime the subscriptionTime to set
	 */
	public void setSubscriptionTime(String subscriptionTime) {
		this.subscriptionTime = subscriptionTime;
	}
	/**
	 * @param newSubscriptionTime the newSubscriptionTime to set
	 */
	public void setNewSubscriptionTime(String newSubscriptionTime) {
		this.newSubscriptionTime = newSubscriptionTime;
	}
	/**
	 * @param expireTime the expireTime to set
	 */
	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}
	/**
	 * @param insertTime the insertTime to set
	 */
	public void setInsertTime(String insertTime) {
		this.insertTime = insertTime;
	}
	/**
	 * subscriptionTime set to sysdate
	 */
	public void setSubscriptionTimeNow() {
		this.subscriptionTime = "sysdate";
	}
	/**
	 * newSubscriptionTime set to sysdate
	 */
	public void setNewSubscriptionTimeNow() {
		this.newSubscriptionTime = "sysdate";
	}
	/**
	 * expireTime set to sysdate
	 */
	public void setExpireTimeNow() {
		this.expireTime = "sysdate";
	}
	/**
	 * expireTime set to sysdate+minutes
	 */
	public void setExpireTimeNow(String minutes) {
		this.expireTime = "sysdate+numtodsinterval("+minutes+",'minute')";
	}
	/**
	 * insertTime set to sysdate
	 */
	public void setInsertTimeNow() {
		this.insertTime = "sysdate";
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @param failCounter the failCounter to set
	 */
	public void setFailCounter(String failCounter) {
		this.failCounter = failCounter;
	}
}
