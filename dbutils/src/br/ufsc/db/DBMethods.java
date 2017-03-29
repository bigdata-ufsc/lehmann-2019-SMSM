package br.ufsc.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class with encapsulating all methods needed to be used as SQL statements.
 * In case of some needed method not implemented, please add it here.
 * 
 * @author Artur Aquino, Vitor Fontes
 *
 */
public class DBMethods {
	
	Connection conn = DBConnectionProvider.getInstance().getConn();

	/**
	 * Executes the given SQL statement.
	 * 
	 * @param sql	any SQL statement
	 * @return		true if the first result is a ResultSet object; false if it is an update count or there are no results
	 * @throws SQLException if a database access error occurs
	 */
	boolean execute(String sql) throws SQLException{
		return conn.createStatement().execute(sql);
	}
	
	/**
	 * Executes the given SQL statement.
	 * 
	 * @param sql	any SQL statement
	 * @return		a ResultSet object that contains the data produced by the given query - never null 
	 * @throws SQLException if a database access error occurs
	 */
	ResultSet executeQuery(String sql) throws SQLException{
		return conn.createStatement().executeQuery(sql);
	}

	/**
	 * Method to help on building SQL queries.
	 * Useful on nested selects to improve readability.
	 * 
	 * @param table		any table of database
	 * @param fields	any valid fields of a SQL statement
	 * @param where		any valid WHERE clause
	 * @param addition	anything else to complement the statement
	 * @return			a String representing the whole SQL statement
	 */
	String buildSQLQuery(String table, String fields, String where, String order, String addition){
		if(fields == null)
			fields = "*";

		String sql = "select " + fields + " from " + table;

		if(where != null)
			sql = sql + " where " + where;

		if(order != null)
			sql = sql + " order by " + order;
		
		if(addition != null)
			sql = sql + " " + addition;

		return sql;
	}

	/**
	 * Executes a query on the specified database table, according to the parameters.
	 * It is probably more efficient, since it uses a simple statement.
	 * 
	 * @param table		any table of database
	 * @param fields	any valid fields of a SQL statement
	 * @param where		any valid WHERE clause
	 * @param addition	anything else to complement the statement
	 * @return			a new default Statement object 
	 * @throws SQLException if a database access error occurs or this method is called on a closed connection
	 */
	ResultSet quickQuery(String table, String fields, String where, String order, String addition) throws SQLException{
		String sql = buildSQLQuery(table, fields, where, order, addition);
		return conn.createStatement().executeQuery(sql);
	}

	/**
	 * Executes a query on the specified database table, according to the parameters.
	 * It is probably less efficient, since it uses a more complex statement that allows a better fetching of the ResultSet. 
	 * 
	 * @param table		any table of database
	 * @param fields	any valid fields of a SQL statement
	 * @param where		any valid WHERE clause
	 * @param addition	anything else to complement the statement
	 * @return			a new Statement object that will generate ResultSet objects with the given type and concurrency 
	 * @throws SQLException if a database access error occurs or this method is called on a closed connection
	 */
	ResultSet query(String table, String fields, String where, String order, String addition) throws SQLException{
		String sql = buildSQLQuery(table, fields, where, order, addition);
		// Allows to walk the ResultSet in any direction
		return conn.createStatement(ResultSet.FETCH_UNKNOWN, ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
	}

	/**
	 * Executes a query on the specified database table, according to fields and ordered by order.
	 * 
	 * @param table		any table of the database
	 * @param fields	any valid fields of a SQL statement
	 * @param order		any valid ORDER BY clause
	 * @return			a ResultSet object that contains the data produced by the given query; never null.
	 * @throws SQLException if a database access error occurs or this method is called on a closed connection
	 */
	ResultSet orderedQuery(String table, String fields, String order) throws SQLException{
		String sql = buildSQLQuery(table, fields, null, order, null);
		return conn.createStatement(ResultSet.FETCH_UNKNOWN, ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
	}
	
}
