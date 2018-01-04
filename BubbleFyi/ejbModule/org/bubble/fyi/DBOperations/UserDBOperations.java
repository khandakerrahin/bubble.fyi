/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonDecoder;
import org.bubble.fyi.Initializations.SecretKey;
import org.bubble.fyi.Logs.LogWriter;
import org.bubble.fyi.Utilities.NullPointerExceptionHandler;
import org.bubble.fyi.Utilities.RandomStringGenerator;

/**
 * @author hafiz
 * update information
 * delete user/organization
 */
public class UserDBOperations {
	BubbleFyiDS fsDS;
	//private static final Logger LOGGER = LogWriter.LOGGER.getLogger(UserDBOperations.class.getName());
	/**
	 * 
	 */
	public UserDBOperations() {
		fsDS = new BubbleFyiDS();
	}
	/**
	 * TODO transaction rollback in case of error
	 * @param userId
	 * @return
	 * 0 success
	 * all negative values is error.
	 */
	public String deleteUser(String userId) {
		String retval="-1";
		try {
			String sqlDeleteUser="DELETE FROM users WHERE user_id=?";
			fsDS.prepareStatement(sqlDeleteUser);
			fsDS.getPreparedStatement().setString(1, userId);
			fsDS.execute();
			fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("User deleted from users list.");
			String sqlDeleteOrgs="DELETE FROM organizations WHERE user_id=?";
			fsDS.prepareStatement(sqlDeleteOrgs);
			fsDS.getPreparedStatement().setString(1, userId);
			fsDS.execute();
			fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("User deleted.");
			retval="0";
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("deleteUser(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	public String modifyUser(String userId, JsonDecoder modifications) {
		//TODO Modify user
		String retval="-1";

		return retval;
	}
	/**
	 * 
	 * @param userId
	 * @param oldPass
	 * @param newPass
	 * @return
	 *  0:Password set
	 *  1:User not found
	 *  2:Current password is invalid
	 *  3:Error encountered while setting password
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 */
	public String modifyPasswordDB(String userId, String oldPass, String newPass) {
		//Modify password
		String retval="-1";
		//	check if old password matches
		//	retrieve old password, keySeed
		String keySeed="",passwd="";
		//String sql="select AES_DECRYPT(passwd_enc,concat_ws('',?,key_seed,key_seed,key_seed)) as passwd, key_seed from tbl_users where id=?";
		String sql="select password as passwd from tbl_users where id=?";
		
		try {
			fsDS.prepareStatement(sql);
		fsDS.getPreparedStatement().setString(1, userId);
			fsDS.executeQuery();
			if(fsDS.getResultSet().next()) {
				passwd=fsDS.getResultSet().getString(1);
				}else {
				retval="1:User not found";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			if(oldPass.equals(passwd)) {
				//proceed to change passwd
				if(setNewPassword(userId,newPass)) {
					retval="0:Password set";
					LogWriter.LOGGER.info("New password");
				}else {
					retval="3:Error encountered while setting password";
					LogWriter.LOGGER.info("Error encountered while setting password.");
				}
			}else {
				//password didn't match
				if(retval.startsWith("-1")) {
					retval="2:Current password is invalid";
					LogWriter.LOGGER.info("Current password did not match.");
				}
			}
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("modifyPassword(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	
	public String modifyCustomerStatus(String userId, String customerId, String Status) {
		//Modify password
		String retval="-1";
		//	check if old password matches
		//	retrieve old password, keySeed
		/*String keySeed="",passwd="";
		//String sql="select AES_DECRYPT(passwd_enc,concat_ws('',?,key_seed,key_seed,key_seed)) as passwd, key_seed from tbl_users where id=?";
		String sql="select password as passwd from tbl_users where id=?";
		
		try {
			fsDS.prepareStatement(sql);
		    fsDS.getPreparedStatement().setString(1, userId);
			fsDS.executeQuery();
			if(fsDS.getResultSet().next()) {
				passwd=fsDS.getResultSet().getString(1);
				}else {
				retval="1:User not found";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			if(oldPass.equals(passwd)) {/**/
				//proceed to change passwd
				if(setNewStatus(userId,customerId,Status)) {
					retval="0:Customer Status Updated";
					LogWriter.LOGGER.info("New password");
				}else {
					retval="3:Error encountered while Updating Status";
					LogWriter.LOGGER.info("Error encountered while updating Customer Status.");
				}
			/*}else {
				//password didn't match
				if(retval.startsWith("-1")) {
					retval="2:Current password is invalid";
					LogWriter.LOGGER.info("Current password did not match.");
				}
			}
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("modifyCustomerStatus(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}/**/
		return retval;
	}
	
	private boolean setNewStatus(String userId,String customerId,String newflag) {
		boolean retval=false;
		String sqlUpdateUser="UPDATE tbl_users u set u.flag=?,u.updated_by=?,u.updated_on= CURRENT_TIMESTAMP WHERE u.id=?";
		try {
			fsDS.prepareStatement(sqlUpdateUser);
			fsDS.getPreparedStatement().setString(1, newflag);
			fsDS.getPreparedStatement().setString(2, userId);
			fsDS.getPreparedStatement().setString(3, customerId);
			fsDS.execute();
			fsDS.closePreparedStatement();
			retval=true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewStatus(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval=false;
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	
	
	private boolean setNewPassword(String userId,String newPass) {
		boolean retval=false;
		String sqlUpdateUser="UPDATE tbl_users u set u.password=? WHERE u.id=?";
		try {
			fsDS.prepareStatement(sqlUpdateUser);
			fsDS.getPreparedStatement().setString(1, newPass);
			fsDS.getPreparedStatement().setString(2, userId);
			fsDS.execute();
			fsDS.closePreparedStatement();
			retval=true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewPassword(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval=false;
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	public String changeKeySeed(String userId, String newKeySeed) {
		//TODO Modify keySeed
		String retval="-1";
		//	retrieve oldKeySeed, password
		//	set newKeySeed, password
		return retval;
	}
	/**
	 * 
	 * @param credential
	 * @param password
	 * @param mode
	 * @return
	 * 0:Password verified
	 * 1:User not found
	 * 2:Verificaion failed
	 * 3:Verificaion consistency error
	 * 11:Invalid mode.
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 */
	public String validatePassword(String credential, String password, String mode) {
		String retval="-1";
		String sql="select count(*) as counter from users where <mode>=?  and passwd_enc=AES_ENCRYPT(?,concat_ws('',?,key_seed,key_seed,key_seed))";
		if(mode.equals("1")) {//email
			sql=sql.replace("<mode>", "user_email");
		}else if(mode.equals("2")) {//phone
			sql=sql.replace("<mode>", "phone");
		}else if(mode.equals("3")) {//userid
			sql=sql.replace("<mode>", "user_id");
		}else {
			retval="11:Invalid mode";
		}
		if(retval.startsWith("-1")) {
			String counter="-1";
			try {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, credential);
				fsDS.getPreparedStatement().setString(2, password);
				fsDS.getPreparedStatement().setString(3, SecretKey.SECRETKEY);				
				fsDS.executeQuery();
				if(fsDS.getResultSet().next()) {
					counter=fsDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
				fsDS.closeResultSet();
				fsDS.closePreparedStatement();
				if(counter.equals("1")) {
					retval="0:Password verified";
				}else if(counter.equals("0")) {
					retval="2:Verificaion failed";
				}else {
					retval="3:Verificaion consistency error";
				}
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("validatePassword(): "+e.getMessage());
			}finally{
				if(fsDS.getConnection() != null){
					try {
						fsDS.getConnection().close();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}      
			}
		}
		return retval;
	}

	public String getUserType(String userId) {
		String retval="-1";
		String sql="select flag from tbl_users where id=?";
		if(retval.startsWith("-1")) {
			try {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, userId);
				fsDS.executeQuery();
				if(fsDS.getResultSet().next()) {
					retval=fsDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
				fsDS.closeResultSet();
				fsDS.closePreparedStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				retval="-2";
				LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
			}finally{
				if(fsDS.getConnection() != null){
					try {
						fsDS.getConnection().close();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}      
			}
		}
		return retval;
	}
	/**
	 * 
	 * @param credential
	 * @param mode
	 * @return userId in the users table
	 * <br>1:User not found, 11:Invalid mode , -1 for unknown error, -2 for SQLException
	 */
	/* public String getUserId(String credential,String mode) {
		String retval="-1";
		String sql="select user_id from users where <mode>=?";
		if(mode.equals("1")) {//email
			sql=sql.replace("<mode>", "user_email");
		}else if(mode.equals("2")) {//phone
			sql=sql.replace("<mode>", "phone");
		}else if(mode.equals("3")) {//userid
			sql=sql.replace("<mode>", "user_id");
		}else {
			retval="11:Invalid mode";
		}
		if(retval.startsWith("-1")) {
			try {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, credential);
				fsDS.executeQuery();
				if(fsDS.getResultSet().next()) {
					retval=fsDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
				fsDS.closeResultSet();
				fsDS.closePreparedStatement();
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("validatePassword(): "+e.getMessage());
			}finally{
				if(fsDS.getConnection() != null){
					try {
						fsDS.getConnection().close();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
					}
				}      
			}
		}
		return retval;
	}/**/

	//list Schools (Admins)
	//list Spider Admins
	//list Parents
	//list All
	/**
	 * 
	 * 
	 * @param listType Admin/Parents/SpiderAdmin/all
	 * @param x to distinguish from string return overload
	 * @return
	 */
	// for parents id,phone,email,name,otp,otp_expire_time,status
	/*public Map<String,Object> getList(String listType,int x){
		Map<String,Object> mapList=new HashMap<String,Object >();
		List<String> row;
		String errorCode="-1";
		String sql="SELECT u.user_id, u.user_name, o.organization_name, u.user_email, u.user_type, u.phone, u.status, o.custodian_email,o.custodian_name,o.custodian_phone,o.organization_type,o.address,o.city,o.postcode FROM users u, organizations o where u.user_id=o.user_id and user_type=? order by user_id asc";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, listType);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				row=new ArrayList<String>();
				row.add(rs.getString("user_id"));
				row.add(rs.getString("user_name"));
				row.add(rs.getString("user_email"));
				row.add(rs.getString("phone"));
				row.add(rs.getString("user_type"));
				row.add(rs.getString("status"));
				if(rs.getString("user_type").equalsIgnoreCase("Admin")) {
					row.add(rs.getString("organization_name"));
					row.add(rs.getString("custodian_email"));
					row.add(rs.getString("custodian_name"));
					row.add(rs.getString("custodian_phone"));
					row.add(rs.getString("organization_type"));
					row.add(rs.getString("address"));
					row.add(rs.getString("city"));
					row.add(rs.getString("postcode"));
				}
				mapList.put(rs.getString("user_id"), row);
				errorCode="0";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("MapList : "+mapList);
		}catch(Exception e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			mapList.clear();
			mapList.put("errorcode", errorCode);
		}
		return mapList;
	}/**/

	/**
	 * 
	 * 
	 * @param listType Admin/Parents/SpiderAdmin/all
	 * @param x to distinguish from string return overload
	 * @return
	 * List of Parents and SpiderAdmin: userId,username,email,phone,userType,status
	 * List of Admins: userId,username,email,phone,userType,status,organizationName,custodianName,custodianEmail,custodianPhone,organizationType,city,postcode,address
	 * <br> u.user_id, u.user_name, u.user_email, u.phone, u.user_type, u.status, o.organization_name,o.custodian_name, o.custodian_email,o.custodian_phone,o.organization_type,o.city,o.postcode,o.address
	 */
	// for parents id,phone,email,name,otp,otp_expire_time,status
	public String getList(String id,String userType){
		//TODO
		LogWriter.LOGGER.severe(" going to get list --> getList id:userType ::"+id+":"+userType);
		String retval="";
		String errorCode="-1";
		//String sql="SELECT u.user_id, u.user_name, u.user_email, u.phone, u.user_type, u.status, o.organization_name,o.custodian_name, o.custodian_email,o.custodian_phone,o.organization_type,o.city,o.postcode,o.address FROM users u left join organizations o on u.user_id=o.user_id where user_type=? order by user_id asc";
		String sqlAdmin="SELECT t.aparty,t.bparty,t.message,t.sms_count,t.insert_date,t.flag,t.userid,t.exec_date,t.responseCode,t.source_id FROM smsinfo t ORDER BY `ID` ";
		String sql="SELECT t.aparty,t.bparty,t.message,t.sms_count,t.insert_date,t.flag,t.userid,t.exec_date,t.responseCode,t.source_id FROM smsinfo t where t.userid=? ORDER BY `ID` asc";
		try {
			if(userType.equals("Admin")) {
				fsDS.prepareStatement(sqlAdmin);
				//ResultSet rs = fsDS.executeQuery();
			}else {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, id);			
			}
			
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				
				if(userType.equalsIgnoreCase("Admin")) {
					retval+=rs.getString("aparty")+",";
					//retval+="\""+rs.getString("organization_name")+"\""+",";
					retval+=rs.getString("userid")+",";
				}				
				retval+=rs.getString("bparty")+",";
				retval+="\""+rs.getString("message")+"\""+",";
				retval+="\""+rs.getString("insert_date")+"\""+",";
				retval+=rs.getString("sms_count")+",";
				retval+="\""+rs.getString("exec_date")+"\""+",";
				retval+=rs.getString("flag")+",";
				retval+=rs.getString("source_id")+",";
				retval+=rs.getString("responseCode");			
				retval+="|";		
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			if(NullPointerExceptionHandler.isNullOrEmpty(retval)) retval="0";
			int lio=retval.lastIndexOf("|");
			if(lio>0) retval=retval.substring(0,lio);
			errorCode="0";
			LogWriter.LOGGER.info("MapList : "+retval);
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
		if(!errorCode.startsWith("0")) {
			retval=errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get list --> "+retval);
		return retval;
	}
	// list Students from school
	// list students under parent
	
	public String getAllCustomerList(String id){
		String retval="";
		String errorCode="-1";
		String userFlag=getUserType(id);
		
		if(userFlag.equals("5")) {
		String sql="SELECT t.custodian_name,t.username,t.email,t.phone,t.organization_name,t.city,t.address,t.postcode,t.insert_date,t.flag FROM  tbl_users t where t.flag !=?";
		
		try {
			fsDS.prepareStatement(sql);
			
			//fsDS.getPreparedStatement().setString(1, userFlag);
			fsDS.getPreparedStatement().setString(1, userFlag);	
			//TODO
			LogWriter.LOGGER.severe(" after prepared Statement");
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+="\""+rs.getString("custodian_name")+"\""+",";
				retval+="\""+rs.getString("username")+"\""+",";
				retval+="\""+rs.getString("organization_name")+"\""+",";
				retval+=rs.getString("city")+",";
				retval+="\""+rs.getString("address")+"\""+",";
				retval+=rs.getString("postcode")+",";
				retval+="\""+rs.getString("insert_date")+"\""+",";
				retval+=rs.getString("flag")+",";						
				retval+="|";		
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			if(NullPointerExceptionHandler.isNullOrEmpty(retval)) retval="0";
			int lio=retval.lastIndexOf("|");
			if(lio>0) retval=retval.substring(0,lio);
			errorCode="0";
			LogWriter.LOGGER.info("MapList : "+retval);
		}catch(SQLException e){
			errorCode= "-2";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-4";
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
	  }else {
		  errorCode="-7: User not Authorized to perform this action";
	  }
		if(!errorCode.startsWith("0")) {
			retval=errorCode;
		}
		LogWriter.LOGGER.severe(" return from  get Customer list --> userFlag : "+userFlag+":"+retval);
		return retval;
	}
	
	/**
	 * 
	 * @param userId Parents Phone, School's id TODO remove leading 0s
	 * @param listFor S:School/P:parent
	 * @param x to distinguish from the String return
	 * @return
	 */
	public Map<String,Object> getStudentList(String userId, String listFor,int x){
		Map<String,Object> mapList=new HashMap<String,Object >();
		List<String> row;
		String errorCode="-1";
		String sql="SELECT s.ID,s.SID,s.NAME,s.ADDRESS,s.PARENT_CONTACT,s.CLASS,s.SECTION,s.SCHOOL_ID,o.ORGANIZATION_NAME,s.DUE,s.PAID,s.PAYMENT_STATUS,s.STATUS FROM students s, organizations o where <mode>=? and s.school_id=o.user_id";
		if(listFor.equals("S")) { //email
			sql=sql.replace("<mode>", "school_id");
		}else { //phone
			sql=sql.replace("<mode>", "parent_contact");
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, userId);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				row=new ArrayList<String>();
				row.add(rs.getString("SID"));
				row.add(rs.getString("NAME"));
				row.add(rs.getString("ADDRESS"));
				row.add(rs.getString("PARENT_CONTACT"));
				row.add(rs.getString("CLASS"));
				row.add(rs.getString("SECTION"));
				row.add(rs.getString("SCHOOL_ID"));
				row.add(rs.getString("ORGANIZATION_NAME"));
				row.add(rs.getString("DUE"));
				row.add(rs.getString("PAID"));
				row.add(rs.getString("PAYMENT_STATUS"));
				row.add(rs.getString("STATUS"));
				mapList.put(rs.getString("ID"), row);
				errorCode="0";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
		}catch(Exception e){
			errorCode= "-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			mapList.clear();
			mapList.put("errorcode", errorCode);
		}
		return mapList;
	}
	/**
	 * 
	 * @param userId Parents Phone, School's id TODO remove leading 0s
	 * @param listFor S:School/P:parent
	 * @return CSV List in the format in order:
	 * <br> s.ID,s.SID,s.NAME,s.PARENT_CONTACT,s.CLASS,s.SECTION,s.SCHOOL_ID,o.ORGANIZATION_NAME,s.DUE,s.PAID,s.PAYMENT_STATUS,s.STATUS,s.ADDRESS
	 * Empty if no records
	 */
	public String getStudentList(String userId, String listFor){
		String retval="";
		String errorCode="-1";
		LogWriter.LOGGER.info("getStudentList(): Id:"+userId+" listFor:"+listFor);
		String sql="SELECT s.ID,s.SID,s.NAME,s.PARENT_CONTACT,s.CLASS,s.SECTION,s.SCHOOL_ID,o.ORGANIZATION_NAME,s.DUE,s.PAID,s.PAYMENT_STATUS,date_format(s.PAYMENT_TIME,'%Y-%m-%d %H:%i:%s') as PAYMENT_TIME,s.STATUS,s.ADDRESS FROM students s, organizations o where <mode>=? and s.school_id=o.user_id";
		if(listFor.equals("S")) { //email
			sql=sql.replace("<mode>", "school_id");
		}else { //phone
			sql=sql.replace("<mode>", "parent_contact");
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, userId);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+=rs.getString("ID")+",";
				retval+="\""+rs.getString("SID")+"\""+",";
				retval+="\""+rs.getString("NAME")+"\""+",";
				retval+=rs.getString("PARENT_CONTACT")+",";
				retval+="\""+rs.getString("CLASS")+"\""+",";
				retval+="\""+rs.getString("SECTION")+"\""+",";
				retval+=rs.getString("SCHOOL_ID")+",";
				retval+="\""+rs.getString("ORGANIZATION_NAME")+"\""+",";
				retval+="\""+rs.getString("DUE")+"\""+",";
				retval+="\""+rs.getString("PAID")+"\""+",";
				retval+="\""+rs.getString("PAYMENT_STATUS")+"\""+",";
				retval+="\""+rs.getString("PAYMENT_TIME")+"\""+",";
				retval+="\""+rs.getString("STATUS")+"\""+",";
				retval+="\""+rs.getString("ADDRESS")+"\"|";
				errorCode="0";
//				LogWriter.LOGGER.info("looping: "+retval);
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
//			LogWriter.LOGGER.info("retvalBeforeMod: "+retval);
			if(errorCode.equals("0"))
				retval=retval.substring(0,retval.lastIndexOf("|"));
			else{
				errorCode="0";
			}
		}catch(SQLException e){
			errorCode= "-2";
			LogWriter.LOGGER.severe("getStudentList() SQLException:"+e.getMessage());
		}catch(Exception e){
			errorCode= "-4";
			LogWriter.LOGGER.severe("getStudentList():"+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					errorCode="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			retval= errorCode;
		}
		return retval;
	}
	//otp,otp_expire_time,status for parents
	/**
	 * 
	 * @param phone
	 * @return
	 * An alpha numeric string of length 6
	 * -1:General Error while generating OTP
	 * -2:SQLException
	 * -3:SQLException when closing connection
	 */
	public String getNewOtp(String phone) {
		String retval="-1:General Error while generating OTP";
		String otp=generateOtp();
		String sql="update users set otp=?,otp_expire=date_add(now(),Interval 2 hour) where phone=?";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, otp);
			fsDS.getPreparedStatement().setString(2, phone);
			fsDS.execute();
			fsDS.closePreparedStatement();
			retval=otp;
		} catch (SQLException e) {
			retval="-2:SQLException";
			LogWriter.LOGGER.severe("getNewOtp(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	/**
	 * 
	 * @param phone
	 * @return
	 * OTP String length 6 CharacterSet {A-Z 0-9}
	 *  1:OTP expired or User not found
	 * -2:SQLException
	 */
	public String getStoredOtp(String phone) {
		String retval="-1:General Error while generating OTP";
		String sql="select otp,otp_expire from users where phone=? and otp_expire>now()";
		String otp="",otpExpire="";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, phone);
			fsDS.execute();
			if(fsDS.getResultSet().next()) {
				otp=fsDS.getResultSet().getString(1);
				otpExpire=fsDS.getResultSet().getString(2);
				retval=otp+","+otpExpire;
			}else {
				retval="1:OTP expired or User not found";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
		} catch (SQLException e) {
			retval="-2:SQLException";
			LogWriter.LOGGER.severe("getNewOtp(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	private String generateOtp() {
		return RandomStringGenerator.getRandomString("1234567890ABEDEFGHIJKLMNOPQRSTUVWXYZ", 6);
	}
	/**
	 * 
	 * @param id email or phone or userId
	 * @param otp Alpha-numeric text
	 * @param mode 1=email, 2=phone, 3=userId
	 * @return
	 *  0:OTP verified and user activated
	 *  1:OTP verification failed.
	 * -1:OTP Error
	 * -2:SQLException for update query
	 * -3:SQLException for connection close
	 * -11:Invalid mode
	 */
	public String otpVerifyActivateParent(String id, String otp, String mode) {
		//fetch otp;//compare otp//if otp correct, activate
		String retval="-1";
		String sql="update users set status=1 from users where <mode>=? and otp=? and otp_expire>now()";
		if(mode.equals("1")) {//email
			sql=sql.replace("<mode>", "user_email");
		}else if(mode.equals("2")) {//phone
			sql=sql.replace("<mode>", "phone");
		}else if(mode.equals("3")) {//userid
			sql=sql.replace("<mode>", "user_id");
		}else {
			retval="-11:Invalid mode";
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, id);
			fsDS.getPreparedStatement().setString(2, otp);
			long r=fsDS.executeUpdate();
			fsDS.closePreparedStatement();
			if(r>0) retval="0:OTP verified and user activated";
			else retval="1:OTP varification failed.";
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("otpVerifyActivateParent(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return retval;		
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
	 * // OTP here needed
	 * @return Positive number indicates userId
	 * Anything -ve is error.
	 */
	public String createParent(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		String sqlInsertUsers="INSERT INTO users("
				+ "user_name,user_email,user_password,user_type,status,phone,key_seed,passwd_enc,otp,otp_expire)" 
				+ "VALUES" 
				+ "(?,?,?,'Parent',0,?,?,AES_ENCRYPT(?,concat_ws('',?,?,?,?)),?,date_add(now(),Interval 15 minute))";
		String userId="-1";
		try {
			//json: schoolName,email,phone,password
			fsDS.prepareStatement(sqlInsertUsers,true);
			String keySeed=jsonDecoder.getEString("email")+this.msisdnNormalize(jsonDecoder.getEString("phone"));
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getEString("name"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getEString("email"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getEString("password"));
			fsDS.getPreparedStatement().setString(4, this.msisdnNormalize(jsonDecoder.getEString("phone")));
			fsDS.getPreparedStatement().setString(5, keySeed);//key_seed
			fsDS.getPreparedStatement().setString(6, jsonDecoder.getEString("password"));//AES encrypt password
			fsDS.getPreparedStatement().setString(7, SecretKey.SECRETKEY);//key
			fsDS.getPreparedStatement().setString(8, keySeed);
			fsDS.getPreparedStatement().setString(9, keySeed);
			fsDS.getPreparedStatement().setString(10, keySeed);
			fsDS.getPreparedStatement().setString(11, generateOtp());
			try{ 
				fsDS.execute();
				userId=getUserId();
			}catch(SQLIntegrityConstraintViolationException de) {
				errorCode="-1:User with the email address or phone number exists";
				LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:"+de.getMessage());
			}catch(SQLException e) {
				errorCode="-11:Inserting user credentials failed";
				LogWriter.LOGGER.severe("SQLException"+e.getMessage());
			}
			if(fsDS.getConnection() != null) fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("UserID:"+userId);
			if(errorCode.equals("-1")) errorCode=userId;
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
		return errorCode;
	}
	/**
	 * // OTP here needed
	 * @return Positive number indicates userId
	 * Anything -ve is error.
	 */
	public String createSpiderAdmin(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		String sqlInsertUsers="INSERT INTO users("
				+ "user_name,user_email,user_password,user_type,status,phone,key_seed,passwd_enc)" 
				+ "VALUES" 
				+ "(?,?,?,'SpiderAdmin',0,?,?,AES_ENCRYPT(?,concat_ws('',?,?,?,?)))";
		String userId="-1";
		try {
			//json: schoolName,email,phone,password
			fsDS.prepareStatement(sqlInsertUsers,true);
			String keySeed=jsonDecoder.getJsonObject().getString("email")+this.msisdnNormalize(jsonDecoder.getEString("phone"));
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getEString("name"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getEString("email"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getEString("password"));
			fsDS.getPreparedStatement().setString(4, this.msisdnNormalize(jsonDecoder.getNString("phone")));
			fsDS.getPreparedStatement().setString(5, keySeed);//key_seed
			fsDS.getPreparedStatement().setString(6, jsonDecoder.getEString("password"));//AES encrypt password
			fsDS.getPreparedStatement().setString(7, SecretKey.SECRETKEY);//key
			fsDS.getPreparedStatement().setString(8, keySeed);
			fsDS.getPreparedStatement().setString(9, keySeed);
			fsDS.getPreparedStatement().setString(10, keySeed);
			try{ 
				fsDS.execute();
				userId=getUserId();
			}catch(SQLIntegrityConstraintViolationException de) {
				errorCode="-1:User with the email address or phone number exists";
				LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:"+de.getMessage());
			}catch(SQLException e) {
				errorCode="-11:Inserting user credentials failed";
				LogWriter.LOGGER.severe("SQLException"+e.getMessage());
			}
			if(fsDS.getConnection() != null) fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("UserID:"+userId);
			if(errorCode.equals("-1")) errorCode=userId;
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
		return errorCode;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private String getUserId() throws SQLException {
		String retval="-1";
		ResultSet rs=fsDS.getGeneratedKeys();
		if(rs.next()) {
			retval=rs.getString(1);
		}
		return retval;
	}
	/**
	 * Only deletes from users table
	 * @param userId
	 */
	public void deleteUsersEntry(String userId) {
		try {
			String sqlDeleteUser="DELETE FROM users WHERE user_id=?";
			fsDS.prepareStatement(sqlDeleteUser);
			fsDS.getPreparedStatement().setString(1, userId);
			fsDS.execute();
			fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("User entry deleted");
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("deleteUsersEntry(): "+e.getMessage());
		}
	}
	
	
	//parent phone available with school
	/**
	 * TODO sendOTP
	 * @param phone
	 * @return 1 or greater is positive
	 * 0 is not found
	 * -ve is Error
	 */
	public String isParentPhoneAvailable(String phone) {
		String retval="-1";
		String sql="select count(*) as counter from students where parent_contact=?";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, this.msisdnNormalize(phone));
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval=rs.getString(1);
				LogWriter.LOGGER.info("Student count:"+retval);
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
		}catch(Exception e){
			retval="-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		return retval;
	}
	//update student info
	// TODO modify student
	// delete student
	/**
	 * 
	 * @param id
	 * @return
	 * 0 success
	 * all negative values is error.
	 */
	public String deleteStudent(String id) {
		String retval="-1";
		try {
			String sqlDelete="DELETE FROM students WHERE id=?";
			fsDS.prepareStatement(sqlDelete);
			fsDS.getPreparedStatement().setString(1, id);
			fsDS.execute();
			fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("Student deleted.");
			retval="0";
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("deleteUser(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return retval;		
	}
	/**
	 * @param jsonDecoder studentId,name,address,parentContact,class,section,schoolId,due,paid,month
	 * @return 0 is successfully created
	 * Anything -ve is error.
	 */
	public String createStudent(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		String sqlInsertStudents="INSERT INTO students("
				+ "SID, name, Address, Parent_contact, class, Section, School_id, due, paid, month)" 
				+ "VALUES" 
				+ "(?,?,?,?,?,?,?,?,?,STR_TO_DATE(concat_ws('','01',?,year(now())),'%d%m%Y'))";
		try {
			//json: SID, name, Address, Parent_contact, class, Section, School_id, due, paid
			fsDS.prepareStatement(sqlInsertStudents,true);
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getEString("studentId"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getEString("name"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getEString("address"));
			fsDS.getPreparedStatement().setString(4, jsonDecoder.getEString("parentContact"));
			fsDS.getPreparedStatement().setString(5, jsonDecoder.getEString("class"));
			fsDS.getPreparedStatement().setString(6, jsonDecoder.getEString("section"));
			fsDS.getPreparedStatement().setString(7, jsonDecoder.getNString("schoolId"));
			fsDS.getPreparedStatement().setString(8, jsonDecoder.getNString("due"));
			fsDS.getPreparedStatement().setString(9, jsonDecoder.getNString("paid"));
			fsDS.getPreparedStatement().setString(10, String.format("%02d", Integer.parseInt(jsonDecoder.getNString("month"))));
			try{ 
				fsDS.execute();
			}catch(SQLIntegrityConstraintViolationException de) {
				errorCode="-1:Student entry for this month already exists";
				LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:"+de.getMessage());
			}catch(SQLException e) {
				errorCode="-11:Inserting student failed";
				LogWriter.LOGGER.severe("SQLException"+e.getMessage());
			}
			if(fsDS.getConnection() != null) fsDS.closePreparedStatement();
			if(errorCode.equals("-1")) errorCode="0";
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
		return errorCode;
	}
	//TODO schoolwise parent list
	//TODO sendsms
	/**
	 * 
	 * @param schoolId
	 * @return
	 */
	public String getFileList(String schoolId) {
		String retval="-1";
		String errorCode="-1";
		String sql="select file_name,school_id,date_format(created,'%Y-%m-%d %H:%i:%s') as created,date_format(uploaded,'%Y-%m-%d %H:%i:%s') as uploaded,CASE status WHEN 0 THEN 'new' WHEN 1 THEN 'uploading' WHEN 2 THEN 'uploaded' WHEN 3 THEN 'error' ELSE 'invalid' end as status,month,estimated_upload_time,comments from fees_file_info where school_id=?";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, schoolId);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+="\""+rs.getString("file_name")+"\""+",";
				retval+="\""+rs.getString("school_id")+"\""+",";
				retval+="\""+rs.getString("created")+"\""+",";
				retval+="\""+rs.getString("uploaded")+"\""+",";
				retval+="\""+rs.getString("status")+"\""+",";
				retval+="\""+rs.getString("month")+"\""+",";
				retval+="\""+rs.getString("estimated_upload_time")+"\""+",";
				retval+="\""+rs.getString("comments")+"\""+"|";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			int lio=retval.lastIndexOf("|");
			if(lio>0) retval=retval.substring(0,lio);
			errorCode="0";
//			retval=retval.substring(0,retval.lastIndexOf("|"));
		}catch(Exception e){
			retval="-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			retval= errorCode;
		}
		return retval;
	}
	/**
	 * 
	 * @param filename
	 * @return filename,schoolId,created,uploaded,status,month,estimated_upload_time,comments
	 * -ve integer is error
	 */
	public String getUploadStatus(String filename) {
		String retval="-1";
		String errorCode="-1";
		String sql="select file_name,school_id,date_format(created,'%Y-%m-%d %H:%i:%s') as created,date_format(uploaded,'%Y-%m-%d %H:%i:%s') as uploaded,CASE status WHEN 0 THEN 'new' WHEN 1 THEN 'uploading' WHEN 2 THEN 'uploaded' WHEN 3 THEN 'error' ELSE 'invalid' end as status,month,estimated_upload_time,comments from fees_file_info where file_name=?";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, filename);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+="\""+rs.getString("file_name")+"\""+",";
				retval+="\""+rs.getString("school_id")+"\""+",";
				retval+="\""+rs.getString("created")+"\""+",";
				retval+="\""+rs.getString("uploaded")+"\""+",";
				retval+="\""+rs.getString("status")+"\""+",";
				retval+="\""+rs.getString("month")+"\""+",";
				retval+="\""+rs.getString("estimated_upload_time")+"\""+",";
				retval+="\""+rs.getString("comments")+"\""+"|";
				errorCode="0";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			retval=retval.substring(0,retval.lastIndexOf("|"));
		}catch(Exception e){
			retval="-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			retval= errorCode;
		}
		return retval;
	}
	//feesFileInsert
	/**
	 * 
	 * @param jsonDecoder filename,schoolId,month
	 * @return 0 is successfully inserted
	 * Anything -ve is error.
	 */
	public String feesFileInsert(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		String sqlInsert="INSERT INTO fees_file_info("+ "file_name,school_id,month"+ ") VALUES"+ "(?,?,?)";
		try {
			//json: file_name,school_id
			fsDS.prepareStatement(sqlInsert);
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getEString("filename"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getEString("schoolId"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getEString("month"));
			try{ 
				fsDS.execute();
			}catch(SQLIntegrityConstraintViolationException de) {
				errorCode="-1:duplicate filename";
				LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:"+de.getMessage());
			}catch(SQLException e) {
				errorCode="-11:Inserting failed";
				LogWriter.LOGGER.severe("SQLException"+e.getMessage());
			}
			if(fsDS.getConnection() != null) fsDS.closePreparedStatement();
			if(errorCode.equals("-1")) errorCode="0";
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
		return errorCode;
	}
	//pay fees update
	/**
	 * 
	 * @param jsonDecoder userId,schoolId,sId,purpose,amount,txTime,pg,log
	 * txTime in the format "yyyy-mm-dd %H:mi:ss" (%Y-%m-%d %H:%i:%s) previously "yy-mm-dd %H:mi:ss" (%y-%m-%d %H:%i:%s)
	 * sId is the data id of student
	 * @return 0:Payment logged
	 * Anything -ve is error
	 */
	public String paymentUpdate(JsonDecoder jsonDecoder) {
		String retval="-1";
		String sql="update students set payment_status=1,paid=(paid+(?)),payment_time=STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') where id=?";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getIntString("amount"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getNString("txTime"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getEString("sId"));
			long r=fsDS.executeUpdate();
			fsDS.closePreparedStatement();
			if(r>0) {
				retval=logTransaction(jsonDecoder);
				if(!retval.startsWith("-"))
					retval="0:Payment logged";
				else retval="-9:severe error while transaction";
			}
			else retval="-1:Payment not updated";
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("paymentUpdate(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		return retval;	
	}
	/**
	 * 
	 * @param jsonDecoder userId,schoolId,sId,purpose,amount,txTime,pg,log
	 * status : 0 = failure, 1 = success
	 * log = success is translated as status=1 , otherwise status is 0
	 * @return logId
	 * -ve is error
	 */
	public String logTransaction(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		String sqlInsert="INSERT INTO transaction_logs(user_id,school_id,student_id,purpose,amount,transaction_time,payment_gateway,log,status)" 
				+ "VALUES" + "(?,?,?,?,?,STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?,?)";
		String insertId="-1";
		try {
			//json: schoolName,email,phone,password
			fsDS.prepareStatement(sqlInsert,true);
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getEString("userId"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getEString("schoolId"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getEString("sId"));
			fsDS.getPreparedStatement().setString(4, jsonDecoder.getEString("purpose"));
			fsDS.getPreparedStatement().setString(5, jsonDecoder.getEString("amount"));
			fsDS.getPreparedStatement().setString(6, jsonDecoder.getEString("txTime"));
			fsDS.getPreparedStatement().setString(7, jsonDecoder.getEString("pg"));
			fsDS.getPreparedStatement().setString(8, jsonDecoder.getEString("log"));
			fsDS.getPreparedStatement().setString(9, jsonDecoder.getEString("status").isEmpty()?(jsonDecoder.getEString("log").equalsIgnoreCase("Success")?"1":"0"):jsonDecoder.getEString("status"));
			try{ 
				fsDS.execute();
				insertId=getUserId();
			}catch(SQLException e) {
				errorCode="-11:Inserting transaction log failed";
				LogWriter.LOGGER.severe("SQLException"+e.getMessage());
			}
			if(fsDS.getConnection() != null) fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("insertId:"+insertId);
			if(errorCode.equals("-1")) errorCode=insertId;
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
		return errorCode;
	}
	//TODO getTxLog for school
	//TODO getTxLog for parents
	/**
	 * 
	 * @param jsonDecoder userId,smsText,sTime,dTime,sentTo,groupId
	 * @return logId
	 * -ve is error
	 */
	public String logSMS(JsonDecoder jsonDecoder) {
		String errorCode="-1";//default errorCode
		String sqlInsert="INSERT INTO sms_logs(user_id,sms_text,sent_time,delivered_time,b_party,group_id)" 
				+ "VALUES" + "(?,?,STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?)";
		String insertId="-1";
		try {
			//json: schoolName,email,phone,password
			fsDS.prepareStatement(sqlInsert,true);
			fsDS.getPreparedStatement().setString(1, jsonDecoder.getNString("userId"));
			fsDS.getPreparedStatement().setString(2, jsonDecoder.getNString("smsText"));
			fsDS.getPreparedStatement().setString(3, jsonDecoder.getNString("sTime"));
			fsDS.getPreparedStatement().setString(4, jsonDecoder.getNString("dTime"));
			fsDS.getPreparedStatement().setString(5, jsonDecoder.getNString("sentTo"));
			fsDS.getPreparedStatement().setString(6, jsonDecoder.getNString("groupId"));;
			try{ 
				fsDS.execute();
				insertId=getUserId();
			}catch(SQLException e) {
				errorCode="-11:Inserting sms log failed";
				LogWriter.LOGGER.severe("SQLException"+e.getMessage());
			}
			if(fsDS.getConnection() != null) fsDS.closePreparedStatement();
			LogWriter.LOGGER.info("insertId:"+insertId);
			if(errorCode.equals("-1")) errorCode=insertId;
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
		return errorCode;
	}
	/**
	 * 
	 * @param userId
	 * @return dataId,studentId,name,schoolName,amount,month,status,txtime, log, purpose
	 * -ve is error
	 */
	public String getTransactionList(String userId) {
		String retval="";
		String errorCode="-1";
		String sql="select t.id as data_id,ifnull(s.SID,'not available') as sid,ifnull(s.name,'not available') name,ifnull(o.organization_name,'not available') organization_name"
				+ ",amount,date_format(s.month,'%M') as month,t.status,date_format(transaction_time,'%Y-%m-%d %H:%i:%s') as txtime, t.log, t.purpose "
				+ "from transaction_logs t left join organizations o on t.school_id=o.user_id left join students s on t.student_id=s.id where t.user_id=? "
				+ "order by o.user_id asc,s.id asc, s.month desc,t.transaction_time desc";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, userId);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+="\""+rs.getString("data_id")+"\""+",";
				retval+="\""+rs.getString("SID")+"\""+",";
				retval+="\""+rs.getString("name")+"\""+",";
				retval+="\""+rs.getString("organization_name")+"\""+",";
				retval+="\""+rs.getString("amount")+"\""+",";
				retval+="\""+rs.getString("month")+"\""+",";
				retval+="\""+rs.getString("status")+"\""+",";
				retval+="\""+rs.getString("txtime")+"\""+",";
				retval+="\""+rs.getString("log")+"\""+",";
				retval+="\""+rs.getString("purpose")+"\""+"|";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			int lio=retval.lastIndexOf("|");
			if(lio>0) retval=retval.substring(0,lio);
//			retval=retval.substring(0,retval.lastIndexOf("|"));
			errorCode="0";
		}catch(Exception e){
			retval="-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			retval= errorCode;
		}
		return retval;
	}
	/**
	 * 
	 * @param userId
	 * @return dataId,studentId,name,class,section,amount,month,status,txtime,log,purpose
	 * -ve is error
	 */
	public String getTxListSchool(String userId) {
		String retval="-1";
		String errorCode="-1";
		String sql="select t.id as data_id,ifnull(s.SID,'not available') as sid,ifnull(s.name,'not available') name,"
				+ "ifnull(s.class,'not available') as class, ifnull(s.Section,'not available') as section"
				+ ",amount,date_format(s.month,'%M') as month,t.status,date_format(transaction_time,'%Y-%m-%d %H:%i:%s') as txtime, t.log, t.purpose "
				+ "from transaction_logs t left join organizations o on t.school_id=o.user_id left join students s on t.student_id=s.id where t.school_id=? "
				+ "order by s.month desc, txTime desc";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, userId);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+="\""+rs.getString("data_id")+"\""+",";
				retval+="\""+rs.getString("SID")+"\""+",";
				retval+="\""+rs.getString("name")+"\""+",";
				retval+="\""+rs.getString("class")+"\""+",";
				retval+="\""+rs.getString("section")+"\""+",";
				retval+="\""+rs.getString("amount")+"\""+",";
				retval+="\""+rs.getString("month")+"\""+",";
				retval+="\""+rs.getString("status")+"\""+",";
				retval+="\""+rs.getString("txtime")+"\""+",";
				retval+="\""+rs.getString("log")+"\""+",";
				retval+="\""+rs.getString("purpose")+"\""+"|";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			int lio=retval.lastIndexOf("|");
			if(lio>0) retval=retval.substring(0,lio);
//			retval=retval.substring(0,retval.lastIndexOf("|"));
			errorCode="0";
		}catch(Exception e){
			retval="-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			retval= errorCode;
		}
		return retval;
	}
	//TODO getSMSList
	public String getSMSList(String userId,String listFor) {
		String retval="-1";
		String errorCode="-1";
		String sql="";
		if(listFor.equals("S")) { //school
			sql=sql.replace("<mode>", "school_id");
		}else { //parent
			sql=sql.replace("<mode>", "parent_contact");
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, userId);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval+="\""+rs.getString("data_id")+"\""+",";
				retval+="\""+rs.getString("SID")+"\""+",";
				retval+="\""+rs.getString("name")+"\""+",";
				retval+="\""+rs.getString("organization_name")+"\""+",";
				retval+="\""+rs.getString("amount")+"\""+",";
				retval+="\""+rs.getString("month")+"\""+",";
				retval+="\""+rs.getString("status")+"\""+",";
				retval+="\""+rs.getString("txtime")+"\""+"|";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
			int lio=retval.lastIndexOf("|");
			if(lio>0) retval=retval.substring(0,lio);
//			retval=retval.substring(0,retval.lastIndexOf("|"));
			errorCode="0";
		}catch(Exception e){
			retval="-2";
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
		if(!errorCode.startsWith("0")) {
			retval= errorCode;
		}
		return retval;
	}
	//TODO getSMSLog for school
	//TODO getSMSLog for parents
}
