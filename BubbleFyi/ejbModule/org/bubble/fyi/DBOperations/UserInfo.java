/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonEncoder;
import org.bubble.fyi.Logs.LogWriter;

/**
 * @author hafiz
 *
 */
public class UserInfo {
	BubbleFyiDS fsDS;
	/**
	 * 
	 */
	public UserInfo() {
		fsDS= new BubbleFyiDS(); 
	}
	
	public JsonEncoder listUsers() {
		JsonEncoder jsonUserList=new JsonEncoder();
		
		return jsonUserList;		
	}
	/**
	 * If msisdn starts with 0, prepends 88.
	 * If msisdn starts with 880 or any other number, returns the String
	 * @param msisdn
	 * @return msisdn of the format 8801xx
	 */
	private String msisdnNormalize(String msisdn) {
		if(msisdn.startsWith("0")) {
			msisdn="88"+msisdn;
		}
		return msisdn;
	}
	/**
	 * 
	 * @param id
	 * @param mode 1:email, 2:phone
	 * @return jsonEncoder userInfo on success
	 * <br>errorCode 0 indicated success in fetching data
	 * <br>-1:General Error
	 * <br>-2:SQLException
	 * <br>-3:Exception
	 * <br>-4:SQLException while closing
	 */
	public JsonEncoder fetchUserInfo(String id, String mode) {
		JsonEncoder jsonEncoder=new JsonEncoder();
		String errorCode="-1";//default errorCode
		String sql="SELECT u.user_id, u.user_name, o.organization_name, u.user_email, u.user_type, u.phone, u.status, o.custodian_email,o.custodian_name,o.custodian_phone,o.organization_type,o.address,o.city,o.postcode FROM users u left join organizations o on u.user_id=o.user_id where u.<mode>=?";
		if(mode.equals("1")) { //email
			sql=sql.replace("<mode>", "user_email");
		}else { //phone
			sql=sql.replace("<mode>", "phone");
			id=msisdnNormalize(id);
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, id);
			ResultSet rs = fsDS.executeQuery();
			if (rs.next()) {
				jsonEncoder.addElement("id", rs.getString("user_id"));
				jsonEncoder.addElement("username", rs.getString("user_name"));
				jsonEncoder.addElement("email", rs.getString("user_email"));
				jsonEncoder.addElement("phoneNumber", rs.getString("phone"));
				jsonEncoder.addElement("userType", rs.getString("user_type"));
				jsonEncoder.addElement("status", rs.getString("status"));
				if(rs.getString("user_type").equalsIgnoreCase("Admin")) {
					jsonEncoder.addElement("schoolName", rs.getString("organization_name"));
					jsonEncoder.addElement("custodianEmail", rs.getString("custodian_email"));
					jsonEncoder.addElement("custodianName", rs.getString("custodian_name"));
					jsonEncoder.addElement("custodianPhone", rs.getString("custodian_phone"));
					jsonEncoder.addElement("organisationType", rs.getString("organization_type"));
					jsonEncoder.addElement("address", rs.getString("address"));
					jsonEncoder.addElement("city", rs.getString("city"));
					jsonEncoder.addElement("postcode", rs.getString("postcode"));
				}
				errorCode="0";
			}else {
				errorCode="-9:User details could not be retrieved";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		jsonEncoder.addElement("ErrorCode", errorCode);
		jsonEncoder.buildJsonObject();
		return jsonEncoder;
	}

}
