package org.bubble.fyi.Initializations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

//import org.Banglalink.InhouseUtility.dataSource.DSInfo;
//import org.Banglalink.InhouseUtility.dataSource.InhouseDataFiller;
//import org.Banglalink.InhouseUtility.dataSource.MultiDataSource;
// 
 

public class replyMessageLoader  /*implements  InhouseDataFiller<DataContainerReplyMessage>*/{
	//@SuppressWarnings("rawtypes")
	public HashMap<Integer,String> replyMessage = new HashMap<Integer, String>();
	public replyMessageLoader() {
		// TODO Auto-generated constructor stub
	}

//	@Override
	public Vector<DataContainerReplyMessage> loadData(ResultSet rs)
			throws SQLException {
		Vector<DataContainerReplyMessage> ds = new Vector<DataContainerReplyMessage>(); 
		while (rs.next()) {		 
			DataContainerReplyMessage temp = new DataContainerReplyMessage();
			temp.id 				= rs.getInt("id");
			temp.reply_text  = rs.getString("reply_text");			 		 
			ds.add(temp);
		}
		return ds;
	}
	
	/**
	 * getRelpyMessage:: Upload reply SMS table in HASH during application UP.
	 */
	//@SuppressWarnings("unchecked")
	public void getRelpyMessage() {
		//ResultSet result = null;
		try {
	/**	MultiDataSource<DataContainerReplyMessage> ds;
	    	ds = new MultiDataSource<DataContainerReplyMessage>(DSInfo.getDS_INPATHS_NEW(), this);
	    	ds.setCallback(this); 
	    	String sqlQuery = "select id,reply_text from quick_pack.replies t order by t.id";
	    	Vector<DataContainerReplyMessage> data = ds.getDataList(sqlQuery);	 
	    	for (int i = 0; i < data.size(); i++) {
				DataContainerReplyMessage dataCon = (DataContainerReplyMessage) data.get(i);
				replyMessage.put(new Integer(dataCon.id), dataCon.sms_text);
	    	}/**/
			 		 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
class DataContainerReplyMessage{
	
	int id =0;
	String reply_text = "";
	
}