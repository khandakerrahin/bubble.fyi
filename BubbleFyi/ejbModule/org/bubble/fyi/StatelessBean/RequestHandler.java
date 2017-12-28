package org.bubble.fyi.StatelessBean;

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
import org.bubble.fyi.Engine.LoginProcessor;
import org.bubble.fyi.StatelessBean.RequestHandlerLocal;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;

/**
 * Session Bean implementation class RequestHandler
 */
@Stateless(name = "Bubble.Fyi")
@TransactionManagement(TransactionManagementType.BEAN)
@LocalBean
@Asynchronous
public class RequestHandler implements RequestHandlerRemote, RequestHandlerLocal {

    /**
     * Default constructor. 
     */
    public RequestHandler() {
        // Auto-generated constructor stub
    }
    
    public LoadConfigurations loadConf;
	LogWriter logWriter;
	String replyAddr="bubble.fyi";
	String appName="bubble.fyi";
	String channel;

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
	@Override
	public String processNewRequest(MapMessage msg, LoadConfigurations loadConf, boolean forceLogWrite) throws JMSException, Exception  {
		//request example  
		//login https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=bubble.fyi&destinationType=queue&clientid=bubble.fyi&target=ENGINE&LoadConf=N&message={%20%22username%22:%22t1@sp.com%22,%20%22password%22:%22specialt1pass%22,%20%22mode%22:%221%22}&reply=true&action=login
		//school registration: https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=bubble.fyi&destinationType=queue&clientid=bubble.fyi&target=ENGINE&LoadConf=N&message={%20%22schoolName%22:%22Skola%201%22,%22email%22:%22spiderco@sdxb.com%22,%22phone%22:%228801912345678%22,%22password%22:%22spidercom%22,%22custodianName%22:%22SpiderCom%22,%22address%22:%2210A%20Dhanmondi%22,%22city%22:%22Dhaka%22,%22postcode%22:%221209%22}&reply=true&action=registerSchool
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
		if(action.equalsIgnoreCase("login")) {//documented
			//json: 
			//example: { "username":"t1@sp.com", "password":"specialt1pass", "mode":"1"}
			//retVal=new LoginProcessor().processLogin(message,messageBody);
		}
		/*
		 * TODO
		 * login
		*/
		/*
		 * TODO ApplicationLog
		 */
		else retVal="I:Invalid action";
		LogWriter.LOGGER.info("retVal"+retVal);
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
