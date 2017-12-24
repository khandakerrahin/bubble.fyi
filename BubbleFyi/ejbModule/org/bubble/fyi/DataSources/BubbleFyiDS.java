package org.bubble.fyi.DataSources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BubbleFyiDS {
	DataSource dataStore;
	Connection connection; 
	PreparedStatement preparedStatement; 
	InitialContext initialContext;
	ResultSet resultSet;

	public BubbleFyiDS(){
		super();
		try {
			initialContext = new InitialContext();
			dataStore = (DataSource)initialContext.lookup( "java:/bubble.fyiMySqlDS" );
			connection = dataStore.getConnection();
		}catch(Exception e){
			System.out.println("Exception thrown " +e);
		}
	}

	public PreparedStatement prepareStatement(String statement) throws SQLException {
		this.preparedStatement=this.getConnection().prepareStatement(statement);
		return this.preparedStatement;
	}
	public PreparedStatement prepareStatement(String statement,boolean returnGeneratedKeys) throws SQLException {
		if(returnGeneratedKeys) {
			this.preparedStatement=this.getConnection().prepareStatement(statement,PreparedStatement.RETURN_GENERATED_KEYS);
		}else {
			this.preparedStatement=this.getConnection().prepareStatement(statement);
		}
		return this.preparedStatement;
	}
	public ResultSet getGeneratedKeys() throws SQLException {
		return this.preparedStatement.getGeneratedKeys();
	}
	public ResultSet executeQuery() throws SQLException {
		this.resultSet= this.preparedStatement.executeQuery();
		return this.resultSet;
	}
	
	public boolean execute() throws SQLException {
		return this.preparedStatement.execute();
	}
	
	public long executeUpdate() throws SQLException {
		return this.preparedStatement.executeUpdate();
	}
	
	public void closePreparedStatement() throws SQLException {
		this.preparedStatement.close();
	}
	public void closeResultSet() throws SQLException {
		this.resultSet.close();
	}
	/**
	 * @return the dataStore
	 */
	public DataSource getDataStore() {
		return dataStore;
	}



	/**
	 * @param dataStore the dataStore to set
	 */
	public void setDataStore(DataSource dataStore) {
		this.dataStore = dataStore;
	}



	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}



	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}



	/**
	 * @return the preparedStatement
	 */
	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}



	/**
	 * @param preparedStatement the ps to set
	 */
	public void setPreparedStatement(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}



	/**
	 * @return the initialContext
	 */
	public InitialContext getInitialContext() {
		return initialContext;
	}



	/**
	 * @param initialContext the initialContext to set
	 */
	public void setInitialContext(InitialContext initialContext) {
		this.initialContext = initialContext;
	}



	/**
	 * @return the resultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
/*
	private String exampleQuery() {
		FeesSchoolDS fsDStest= new FeesSchoolDS();
		Connection con = null; 
		PreparedStatement pr = null; 
		String resultString="";
		try {
			con = fsDStest.getDs().getConnection();
			pr = con.prepareStatement("SELECT user_id, user_name,user_email, user_password, user_type, status FROM users");
			ResultSet rs = pr.executeQuery();
			while (rs.next()) {
				resultString+=rs.getString("user_id") + " , "
							+rs.getString("user_name")+ " , "
							+rs.getString("user_email")+ " , "
							+rs.getString("user_password")+ " , "
							+rs.getString("user_type")+ " , "
							+rs.getInt("status")+"\n";
				System.out.println(resultString);
//				LOGGER.info(resultString);
			}
			rs.close();
			pr.close();
		}catch(Exception e){
			e.printStackTrace();; 
		}finally{
			if(con != null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}      
		}
		return resultString;
	}
//*/
}
