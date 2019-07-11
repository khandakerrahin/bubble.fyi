/**
 * 
 */
package org.bubble.fyi.DBOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import org.bubble.fyi.DataSources.BubbleFyiDS;
import org.bubble.fyi.Engine.JsonDecoder;
//import org.bubble.fyi.Initializations.SecretKey;
import org.bubble.fyi.Logs.LogWriter;

/**
 * @author hafiz
 *
 */
public class UserRegistration {
	BubbleFyiDS bubbleDS;

	/**
	 * 
	 */
	public UserRegistration(BubbleFyiDS bubbleDS) {
		this.bubbleDS = bubbleDS;
	}

	/**
	 * If msisdn starts with 0, prepends 88. If msisdn starts with 880 or any other
	 * number, returns the String
	 * 
	 * @param msisdn
	 * @return msisdn of the format 8801xx
	 */
	private String msisdnNormalize(String msisdn) {
		if (msisdn.startsWith("0")) {
			msisdn = "88" + msisdn;
		} else if (msisdn.startsWith("+0")) {
			msisdn = "88" + msisdn.substring(1);
		} else if (msisdn.startsWith("+88")) {
			msisdn = msisdn.substring(1);
		}
		return msisdn;
	}

	/**
	 * 
	 * @param userId
	 * @return 0:Successfully Inserted
	 * 
	 */
	public String insertToCustomerBalanceTable(String userId) {
		String retval = "-1";
		double balance = 0.0;
		String sql = "INSERT INTO customer_balance" + " (user_id,balance) " + "VALUES (" + "?, ?)";
		try {
			bubbleDS.prepareStatement(sql, true);
			bubbleDS.getPreparedStatement().setString(1, userId);
			bubbleDS.getPreparedStatement().setDouble(2, balance);

			retval = "0:Successfully Inserted";
			bubbleDS.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retval;
	}

	/**
	 * 
	 * @param userId
	 * @return 0:Successfully Inserted -1 failed
	 */
	public String insertToTblChargingTable(String userId) {
		String retval = "-1";
		String sql = "INSERT INTO tbl_charging" + " (user_id) " + "VALUES (" + "?)";
		try {
			bubbleDS.prepareStatement(sql, true);
			bubbleDS.getPreparedStatement().setString(1, userId);

			retval = "0:Successfully Inserted";
			bubbleDS.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bubbleDS.closePreparedStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retval;
	}

	/**
	 * 
	 * @param jsonDecoder Admin username, email, password, phone,flag
	 * @param jsonDecoder Customer
	 *                    custodian_name,organization_name,username,password,email,phone,
	 *                    address,postcode,city
	 * @return 0:Successfully Inserted <br>
	 *         1:User with the email address exists <br>
	 *         2:Inserting organization details failed <br>
	 *         11:Inserting user credentials failed <br>
	 *         E:JSON string invalid <br>
	 *         -1:Default Error Code <br>
	 *         -2:SQLException <br>
	 *         -3:General Exception <br>
	 *         -4:SQLException while closing connection insert to customer balance
	 *         table and TODO tbl_charging table
	 */

	public String registerNewUser(JsonDecoder jsonDecoder) {
		String errorCode = "-1";// default errorCode
		/*
		 * String sqlInsertUsers="INSERT INTO tbl_user(" +
		 * "user_name,user_email,user_password,user_type,status,phone,key_seed,passwd_enc)"
		 * + "VALUES" + "(?,?,?,'Admin',1,?,?,AES_ENCRYPT(?,concat_ws('',?,?,?,?)))";/
		 **/
		/*
		 * String sqlInsertCustomer="INSERT INTO tbl_users" +
		 * "(custodian_name,organization_name,username,password,email,phone, address,postcode,city)"
		 * + "VALUES (" + "?, ?, ?,?, ?, ?, ?,?,?)";/
		 **/
		String sqlInsertCustomer = "INSERT INTO tbl_users"
				+ "(custodian_name,organization_name,username,password,email,phone, address)" + "VALUES ("
				+ "?, ?, ?,?, ?, ?, ?)";
		String sqlInsertAdmin = "INSERT INTO tbl_users" + " (username, email, password, phone,flag) " + "VALUES ("
				+ "?, ?, ?, ?,5)";

		String userId = "-1";
		try {
			String userType = jsonDecoder.getJsonObject().getString("userType");
			LogWriter.LOGGER.info("userType: " + userType);
			if (userType.equals("Admin")) {
				try {
					// json: name,email,phone,password
					bubbleDS.prepareStatement(sqlInsertAdmin, true);
					bubbleDS.getPreparedStatement().setString(1, jsonDecoder.getJsonObject().getString("email"));
					bubbleDS.getPreparedStatement().setString(2, jsonDecoder.getJsonObject().getString("email"));
					bubbleDS.getPreparedStatement().setString(3, jsonDecoder.getJsonObject().getString("password"));
					bubbleDS.getPreparedStatement().setString(4,
							this.msisdnNormalize(jsonDecoder.getJsonObject().getString("phone")));
					errorCode = "0:Successfully Inserted";
					bubbleDS.execute();
					userId = getUserId();
				} catch (SQLIntegrityConstraintViolationException de) {
					errorCode = "1:User with the email address or phone number exists";
					LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());
				} catch (SQLException e) {
					errorCode = "11:Inserting user credentials failed";
					LogWriter.LOGGER.severe("SQLException" + e.getMessage());
				} catch (Exception e) {
					errorCode = "75:other Exception";
					e.printStackTrace();
				}
				bubbleDS.closePreparedStatement();
			} else {
				try {
					this.bubbleDS.prepareStatement(sqlInsertCustomer, true);

					bubbleDS.getPreparedStatement().setString(1,
							jsonDecoder.getJsonObject().getString("custodian_name"));
					bubbleDS.getPreparedStatement().setString(2,
							jsonDecoder.getJsonObject().getString("organisation_name"));
					// bubbleDS.getPreparedStatement().setString(3,
					// jsonDecoder.getJsonObject().getString("username"));
					bubbleDS.getPreparedStatement().setString(3, jsonDecoder.getJsonObject().getString("email")); // Wasif
																													// 20190508
																													// Username
																													// replaced
																													// by
																													// email
					bubbleDS.getPreparedStatement().setString(4, jsonDecoder.getJsonObject().getString("password"));
					bubbleDS.getPreparedStatement().setString(5, jsonDecoder.getJsonObject().getString("email"));
					bubbleDS.getPreparedStatement().setString(6, this.msisdnNormalize(jsonDecoder.getEString("phone")));
					bubbleDS.getPreparedStatement().setString(7, jsonDecoder.getJsonObject().getString("address"));
					// bubbleDS.getPreparedStatement().setString(8,
					// jsonDecoder.getJsonObject().getString("postcode"));
					// bubbleDS.getPreparedStatement().setString(9,
					// jsonDecoder.getJsonObject().getString("city"));

					bubbleDS.execute();
					userId = getUserId();
					errorCode = "0:Successfully Inserted";

					// TODO SEND email from here
					try { // SEND EMAIL Wasif 20190507
						String mailBody = "New Merchant Signed Up ! " + ".\n" + "\n" + "Client Detail as Following"
								+ "\n" + "Name: " + jsonDecoder.getJsonObject().getString("custodian_name") + ".\n"
								+ "Email ID : " + jsonDecoder.getJsonObject().getString("email") + "\n"
								+ "Mobile Number: " + jsonDecoder.getEString("phone") + "\n" + "Company name: "
								+ jsonDecoder.getJsonObject().getString("organisation_name") + "." + "\n"
								+ "Please take necessary action at your end." + "\n" + "Bubble.FYI Team";
						new EmailSender(bubbleDS).sendEmailToGroup("4", mailBody);
					} catch (Exception e) {
						e.printStackTrace();
						errorCode = "11:Inserting user credentials failed";
					}

				} catch (SQLIntegrityConstraintViolationException de) {
					errorCode = "1:User with the email address or phone number exists";
					LogWriter.LOGGER.info("SQLIntegrityConstraintViolationException:" + de.getMessage());

				} catch (SQLException e) {
					errorCode = "11:Inserting user credentials failed";
					LogWriter.LOGGER.severe("userRegistration Customer SQl exception : " + e.getMessage());

					// new UserDBOperations().deleteUsersEntry(userId); //only deletes from users
					// table
				} finally {
					if (bubbleDS.getConnection() != null)
						bubbleDS.closePreparedStatement();
				}
			}

		} catch (SQLException e) {
			errorCode = "-2";
			LogWriter.LOGGER.severe("Error code: -2: " + e.getMessage());
		} catch (Exception e) {
			errorCode = "-3";
			LogWriter.LOGGER.severe("Error code: -3: " + e.getMessage());
		} finally {
			// insert to customer balance table
			if (!userId.equalsIgnoreCase("-1")) { // not -1 means user created successfully
				insertToCustomerBalanceTable(userId);
				insertToTblChargingTable(userId);
			} else {
				LogWriter.LOGGER.info("user insert/create failed");
			}
			/*
			 * if(bubbleDS.getConnection() != null){ try { bubbleDS.getConnection().close();
			 * } catch (SQLException e) { errorCode="-4";
			 * LogWriter.LOGGER.severe(e.getMessage()); } } /
			 **/
		}
		LogWriter.LOGGER.info("userID: " + userId);

		return errorCode;
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private String getUserId() throws SQLException {
		String retval = "-1";
		ResultSet rs = bubbleDS.getGeneratedKeys();
		if (rs.next()) {
			retval = rs.getString(1);
		}
		return retval;
	}

	/*
	 * @SuppressWarnings("unused") private String getUserIdFromSequence(BubbleFyiDS
	 * bubbleDS) throws SQLException { String retval="-1"; String
	 * sqlSequence="SELECT LAST_INSERT_ID()";
	 * bubbleDS.prepareStatement(sqlSequence); ResultSet rs=bubbleDS.executeQuery();
	 * if(rs.next()) { retval=rs.getString(1); } bubbleDS.closePreparedStatement();
	 * return retval; }/
	 **/

}
