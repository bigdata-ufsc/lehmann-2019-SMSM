
package br.ufsc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class used to provide the access and communication to a single database.
 * Before any method call it is needed to open a connection with the DB, when is done using
 * DB the connection must be closed. The configuration is still implemented as a class: DBConfig.java.
 * Further versions implementations should consider some alternatives for this.<br>
 * 
 * This is not intended to be used on web, or any concurrency applications, even though part of
 * the necessary treatment was given. For any more advanced applications consider a implementation
 * of a data pool.
 * 
 * @author	Artur Aquino, Vitor Fontes
 * @version	1.0, October 2012
 * @since	2012-10-23
 *
 */
public final class DBConnectionProvider {
	
	private static final DBConnectionProvider instance = new DBConnectionProvider();
	private static Connection conn;
	private static boolean isConnectionOpen = false;
	
	static{
		try {
			Class.forName(DBConfig.driverPostgres); // Loads class from driver
		} catch (ClassNotFoundException e) {
			DBConfig.LOGGER.severe("Error loading class from driver!");
		}
	}
	
	// Private constructor prevents instantiation from other classes
    private DBConnectionProvider() { }

    /**
     * Opens connection with database according to the settings specified on DBConfig.java.
     * If a connection was already established then the method returns.
     * 
     * @throws ClassNotFoundException if the class cannot be located
     * @throws SQLException if a database access error occurs
     */
	public void open() throws SQLException{
		if(isConnectionOpen) return;
		
		conn = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		isConnectionOpen = true;

		DBConfig.LOGGER.fine("Connection Opened Successfully!");
	}
	
	/**
	 * Closes connection if it was established beforehand.
	 * 
	 * @throws SQLException if a database access error occurs
	 */
	public void close() throws SQLException{
		if(!isConnectionOpen) return;

		conn.close();
		isConnectionOpen=false;
		DBConfig.LOGGER.fine("Connection Closed Successfully!");
	}
	
	/**
	 * Provides Connection to methods classes
	 * 
	 * @return Database connection
	 */
	public Connection getConn(){
		return conn;
	}

	/**
	 * Checks whether connection is opened
	 * 
	 * @return true if connection is open, otherwise false
	 */
	public static boolean isConnectionOpen() {
		return DBConnectionProvider.isConnectionOpen;
	}
	
	/**
	 * Returns the Singleton instance of the class which communicates with the database
	 * 
	 * @return instance of DBConnectionProveider.java
	 */
	public static final DBConnectionProvider getInstance(){
		return DBConnectionProvider.instance;
	}

}
