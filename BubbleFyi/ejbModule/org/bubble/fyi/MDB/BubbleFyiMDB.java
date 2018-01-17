package org.bubble.fyi.MDB;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;


import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.naming.NamingException;

import org.bubble.fyi.Initializations.LoadConfigurations;
import org.bubble.fyi.StatelessBean.RequestHandlerLocal;
import org.bubble.fyi.Utilities.QueueProcess;
 
/**
 * Message-Driven Bean implementation class for: BubbleFyiMDB
 */
@MessageDriven(
		activationConfig = { 
				@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/bubble.fyi"), 
				@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
				@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
				@ActivationConfigProperty(propertyName = "clientID", propertyValue = "bubble.fyi"),
				@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "120"),
				@ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "target = 'ENGINE'")
		}, 
		mappedName = "java:/jms/queue/bubble.fyi")
public class BubbleFyiMDB implements MessageListener {
	@EJB(lookup="java:module/RequestHandler")
	private RequestHandlerLocal reqHandle;
	QueueConnection connection;
	QueueSession session;
	MessageProducer messageProducer;
	String appName  = "bubble.fyi";
	public LoadConfigurations loadConf = new LoadConfigurations();
	private static final Logger LOGGER = Logger.getLogger(BubbleFyiMDB.class.getName());
    /**
     * Default constructor. 
     */
    public BubbleFyiMDB() {
        // Auto-generated constructor stub
    	
    }//test commit
	
    
    @PostConstruct
	public void loadConfiguration() {
		try {
			LOGGER.info(appName+" MDB loaded");

			LOGGER.info("Loading configuration from DB ..");
			loadConf.loadConfigurationFromDB();

			LOGGER.info("Loading configuration from DB complete.");

		} catch (Exception ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	@PreDestroy
	public void PreDestroy() {

		LOGGER.info(appName+" MDB destroyed");
	}
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
        // Auto-generated method stub
    	// TODO Edit as necessary
    			if (message instanceof MapMessage) {
    				MapMessage msg = (MapMessage) message;
    				//msg.
    				try {

    					boolean force = true;//InhouseLogger.isTraceForced(msg);            
    					LOGGER.info("Received Message at "+appName+"=" + msg.getString("message"));

    					String loadConfigurations = msg.getString("LoadConf");
    					//boolean isNull = NullPointExceptionHendler.isNullOrEmptyAfterTrim(loadConfigurations);
    					String reply = "";
    					if(/*!isNull &&*/ loadConfigurations.equals("Y")){
    						loadConf.loadConfigurationFromDB();
    						reply ="Configurations Loaded";
    					}else{               
    						reply  =  reqHandle.processNewRequest(msg,loadConf,force);
    					//	LOGGER.info("Reply: "+reply);
    					}
    					// reply  =  reqHandle.processNewRequest(msg,loadConf,force);

    					if (message.getJMSReplyTo() != null) { // Reply to the temporary queue

    						try {
    							QueueProcess.Send(msg, message, reply+"");                    	 
    							//jmsAlarm.clearAlarm(); dbAlarm.clearAlarm();   
    						} catch (NamingException e) {
    							//jmsAlarm.setAlarm(); 
    							LOGGER.severe("exception in responseQueue:" + e.getMessage());
    						}
    					}

    					//jmsAlarm.clearAlarm();dbAlarm.clearAlarm();namingAlarm.clearAlarm();
    				} catch (JMSException e) {            
    					e.printStackTrace();
    					LOGGER.severe(e.getMessage());
    					//jmsAlarm.setAlarm();	
    					try {
    						if (message.getJMSReplyTo() != null) {					    
    							QueueProcess.Send(msg, message, "F");
    							//jmsAlarm.clearAlarm();
    							//dbAlarm.clearAlarm();  
    						}
    					} catch (NamingException | JMSException ex ) {
    						//jmsAlarm.setAlarm();	
    						LOGGER.severe("exception in "+appName+":"+ex.getMessage());
    					} 
    				} catch (Exception e1) {
    					e1.printStackTrace();
    					try {
    						if (message.getJMSReplyTo() != null) {					    
    							QueueProcess.Send(msg, message, "F");
    							//jmsAlarm.clearAlarm();
    							//dbAlarm.clearAlarm();  
    						}
    					} catch (NamingException | JMSException ex ) {
    						//jmsAlarm.setAlarm();	
    						LOGGER.severe("exception in "+appName+":"+ex.getMessage());
    					} 
    				}   

    			} else {
    				LOGGER.warning("INVALID MEESAGE RECEIVED");

    			}
        
    }

}
