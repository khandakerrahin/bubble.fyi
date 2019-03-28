package org.bubble.fyi.DBOperations;
import java.sql.SQLException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonEncoder;
import org.bubble.fyi.Logs.LogWriter;

public class EmailSender {
	BubbleFyiDS bubbleDS;
		String destinationName;
		String clientId;
		String target;
		String LoadConf;
		String reply;
		String action;
		String userId;
		//LogWriter logWriter;
		/**
		 * 
		 */
		public EmailSender(BubbleFyiDS bubbleDS) {
			this.bubbleDS = bubbleDS;
			/**
https://120.50.5.39:8443/HttpReceiver/HttpReceiver?destinationName=spiderpostbox&destinationType=queue&clientid=spiderpostbox&target=ENGINE&LoadConf=N&reply=true&action=sendEmail
			 */
			this.destinationName="spiderpostboxrelay";
			this.clientId="spiderpostboxrelay";
			this.target="ENGINE";
			this.LoadConf="N";
			this.reply="true";
			this.action="sendEmail";
			//this.userId="18";
			//this.logWriter=logWriter;
		//	this.logWriter.appendLog("SendEmail"+";action:"+this.action+";userId:"+this.userId);
		}
		/**
		 * 
		 * @param msisdn
		 * @param smsText
		 * @return 0 is success in sending to SMSC. Anything else is error.
		 * 
		 */
		/*
		public String sendEmail(String from, String to,String cc,String bcc,String subject,String mailBody) {
			//this.logWriter.appendAdditionalInfo(msisdn+":"+smsText);
			return sendEmailtoQueue(msisdn, smsText, true);
		}/**/
		
		/**
		 * 
		 * @param String MailId from emailSenderDetail table
		 * @param String mailBody
		 * @return
		 */
		public String sendEmailToGroup(String mailId,String mailBody) {
			//this.logWriter.appendAdditionalInfo(msisdn+":"+smsText);
			//TODO get all the info from
			String emailSubject="";
			String sender="";
			String reciever="";
			LogWriter.LOGGER.info("sendEmailToGroup:SenderId:"+mailId+" mailBody:"+mailBody);
			String sql="SELECT sender,receiver,mailSubject FROM emailSenderDetail where id=? and status=1 ;";

			try {
				bubbleDS.prepareStatement(sql);
				bubbleDS.getPreparedStatement().setString(1, mailId);
				bubbleDS.executeQuery();
				if(bubbleDS.getResultSet().next()) {
					sender=bubbleDS.getResultSet().getString(1);
					reciever=bubbleDS.getResultSet().getString(2);
					emailSubject=bubbleDS.getResultSet().getString(3);
				}
				bubbleDS.closeResultSet();
				bubbleDS.closePreparedStatement();
			} catch (Exception e) {
				LogWriter.LOGGER.severe("sendEmailToGroup sql exception: "+e);
			}
			
			return sendEmailtoQueue(sender, reciever, emailSubject,mailBody,true);
		}
		
		/**
		 * 
		 * @param sender
		 * @param reciever
		 * @param subject
		 * @param emailbody
		 * @param responseNeeded
		 * @return
		 */
		private String sendEmailtoQueue(String sender, String reciever, String subject,String emailbody,boolean responseNeeded){
			String retval="-1";
			////    destinationName=bubble.fyi&destinationType=queue&clientid=bubble.fyi&target=ENGINE&LoadConf=N&message={"id":"18","msisdn":"88019XXXX","smsText":"this is first text from bubble!!!"}&reply=true&action=sendSMS
			Destination tmpQueue = null;
			try {
				InitialContext iniCtx = new InitialContext();
				Object tmp = iniCtx.lookup("ConnectionFactory");

				QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
				QueueConnection QueueConn = qcf.createQueueConnection();
				Queue queue = (Queue) iniCtx.lookup("java:/queue/" + this.destinationName); //java:/jms/queue/bubble.fyi java:/queue/bubble.fyi
				QueueSession qSession = QueueConn.createQueueSession(false,	QueueSession.AUTO_ACKNOWLEDGE);
				MessageProducer messageProducer = qSession.createProducer(queue);
				MapMessage newMsg = qSession.createMapMessage();
				MapMessage msg = generateMapMessage(sender, reciever,subject,emailbody, newMsg);
				//		boolean force = InhouseLogger.isTraceForced(msg);

				if (responseNeeded) {
					tmpQueue = qSession.createTemporaryQueue();
					msg.setJMSReplyTo(tmpQueue);
					LogWriter.LOGGER.info("EMAIL Temporary Queue is created");
				}
				QueueConn.start();

				messageProducer.send(msg);

				if (responseNeeded) {
					MessageConsumer messageConsumer = qSession.createConsumer(tmpQueue);

					msg = (MapMessage) messageConsumer.receive(30000);
					messageConsumer.close();
					if (msg != null) {
						String response=msg.getString("returnTxt");
						LogWriter.LOGGER.info("SendEmail:"+response);
						if(response.startsWith("0")) {
							retval="0";
						//	this.logWriter.appendLog("SendEmail:"+retval);
						//	this.logWriter.setStatus(1);
						}else {
							retval="-2";
							
						//	this.logWriter.appendLog("SendEmail:"+retval);
							//this.logWriter.setStatus(0);
						}
					} else {
						retval="-3";
						LogWriter.LOGGER.severe("Timed out waiting for reply");
					//	this.logWriter.appendLog("SendEmail:"+retval);
						//this.logWriter.setStatus(0);
					}

				}else{
					//do nothing

				}
				// sessionTmp.close();
				messageProducer.close();
				qSession.close();

				QueueConn.stop();
				QueueConn.close();
				iniCtx.close();
			}catch(NamingException | JMSException ex){
				retval="E";
				LogWriter.LOGGER.severe(ex.getStackTrace().toString());
			//	this.logWriter.appendLog("SendEmail:"+retval);
			//	this.logWriter.setStatus(0);
			}catch(Exception e) {
				retval="E";
				LogWriter.LOGGER.severe(e.getStackTrace().toString());
			//	this.logWriter.appendLog("SendEmail:"+retval);
			//	this.logWriter.setStatus(0);
			}
			return retval;
		}

		public MapMessage generateMapMessage(String sender, String receiver,String emailSubject,String emailBody,MapMessage msg) throws JMSException {
			String body="";
			JsonEncoder jsonEncoder=new JsonEncoder();
			jsonEncoder.addElement("from", sender);
			jsonEncoder.addElement("to", receiver);
			jsonEncoder.addElement("cc", "");
			jsonEncoder.addElement("bcc", "");
			jsonEncoder.addElement("subject", emailSubject);
			jsonEncoder.addElement("mailBody", emailBody);

			body=jsonEncoder.buildJsonObject().toString();
			//body= jsonEncoder.toString();
			
			msg.setString("body", body);
			////    destinationName=bubble.fyi&destinationType=queue&clientid=bubble.fyi&target=ENGINE&LoadConf=N&message={"id":"18","msisdn":"88019XXXX","smsText":"this is first text from bubble!!!"}&reply=true&action=sendSMS
			String message="";
			/*
			  {
	"from":"M I Hafiz <hafiz@bubble.fyi>",
	"to":"wasif@spiderdxb.com,hafiz@spiderdxb.com",
	"cc":"shaker@spiderdxb.com",
	"bcc":"",
	"subject":"Spider e-Postal Service",
	"mailBody":"Our e-Postal service is now functional."
}
			 */
			

			msg.setString("message", message);
			msg.setStringProperty("target",this.target);
			msg.setBoolean("traceON", false);
			msg.setString("destination", "httpService");
			msg.setString("action", this.action);
			msg.setString("clientid", this.clientId);
			msg.setString("reply", this.reply);
			msg.setString("LoadConf", this.LoadConf);
			return msg;
		}
}
