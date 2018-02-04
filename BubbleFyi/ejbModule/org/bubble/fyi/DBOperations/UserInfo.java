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
		//String sql="SELECT u.user_id, u.user_name, o.organization_name, u.user_email, u.user_type, u.phone, u.status, o.custodian_email,o.custodian_name,o.custodian_phone,o.organization_type,o.address,o.city,o.postcode FROM users u left join organizations o on u.user_id=o.user_id where u.<mode>=?";
		//TODO
		String sql="SELECT u.id, u.custodian_name,u.address, u.organization_name, u.username,u.email, if(u.flag=5,'Admin','Customer') as user_type,u.flag, u.phone, u.postcode,u.city,ifnull(u.logo_file,\"\") as logofile FROM tbl_users u where u.<mode>=?";
		if(mode.equals("2")) { //email
			sql=sql.replace("<mode>", "email");
		}else if(mode.equals("1")) { //phone
			sql=sql.replace("<mode>", "phone");
			id=msisdnNormalize(id);
		}else {
			sql=sql.replace("<mode>", "username");
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, id);
			ResultSet rs = fsDS.executeQuery();
			if (rs.next()) {
				jsonEncoder.addElement("id", rs.getString("id"));
				jsonEncoder.addElement("username", rs.getString("username"));
				jsonEncoder.addElement("email", rs.getString("email"));
				jsonEncoder.addElement("phoneNumber", rs.getString("phone"));
				jsonEncoder.addElement("userType", rs.getString("user_type"));
				jsonEncoder.addElement("status", rs.getString("flag"));
				jsonEncoder.addElement("logoFileName", rs.getString("logofile"));
				if(!rs.getString("user_type").equals("Admin")) {
				jsonEncoder.addElement("custodian_name", rs.getString("custodian_name"));
				jsonEncoder.addElement("address", rs.getString("address"));
				jsonEncoder.addElement("organization_name", rs.getString("organization_name"));
				jsonEncoder.addElement("postcode", rs.getString("postcode"));
				jsonEncoder.addElement("city", rs.getString("city"));
				}
				
				errorCode="0";
			}else {
				errorCode="-9:User details could not be retrieved";
			}
			rs.close();
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			LogWriter.LOGGER.severe(e.getMessage());
			e.printStackTrace();
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
