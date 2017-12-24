package org.bubble.fyi.StatelessBean;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

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
        // TODO Auto-generated constructor stub
    }

}
