/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Initializations.SecretKey;
import org.bubble.fyi.Logs.LogWriter;

/**
 * @author hafiz
 *
 */
public class Login {
	BubbleFyiDS fsDS;
	/**
	 * 
	 */
	public Login() {
		fsDS= new BubbleFyiDS();
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
	 * @param loginCredential
	 * @param password
	 * @param mode
	 * @return
	 * 1:User verified
	 * 0:User credentials did not match
	 * E:General Exception
	 * -2:General Error at compareCredentialsInDB()
	 * 
	 */
	public String compareCredentialsInDB(String loginCredential, String password, int mode) {
		String retval="-2";
		String sql="select count(*) as counter from tbl_users where <mode>=? and password=?";
		if(mode==1) { //email
			sql=sql.replace("<mode>", "phone");
			loginCredential=this.msisdnNormalize(loginCredential);
		}else if(mode==2) { //phone
			sql=sql.replace("<mode>", "email");			
		}else{
			sql=sql.replace("<mode>", "username");
		}
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, loginCredential);
			fsDS.getPreparedStatement().setString(2, password);
			//fsDS.getPreparedStatement().setString(3, SecretKey.SECRETKEY);
			ResultSet rs = fsDS.executeQuery();
			while (rs.next()) {
				retval=rs.getString(1);
				LogWriter.LOGGER.info("User count:"+retval);
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();
		}catch(Exception e){
			LogWriter.LOGGER.severe(e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					fsDS.getConnection().close();
				} catch (SQLException e) {
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
//		if(retval.equals("1")) //credentials valid
//		else if(retval.equals("0")); //username and password does not match
		if(retval.matches("1|0")) return retval;
		else return "E";
	}
}
