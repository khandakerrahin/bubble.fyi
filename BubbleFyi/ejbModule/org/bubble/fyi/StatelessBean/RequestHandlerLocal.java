package org.bubble.fyi.StatelessBean;

import javax.ejb.Local;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.bubble.fyi.Initializations.LoadConfigurations;

@Local
public interface RequestHandlerLocal {
	/**
	 * Abstract method to process new request.
	 * @param msg
	 * @param loadConf
	 * @param force
	 * @return The result of the processing. String value.
	 * @throws JMSException
	 * @throws Exception
	 */
	public String processNewRequest(MapMessage msg, LoadConfigurations loadConf, boolean force) throws JMSException, Exception;	 
}
