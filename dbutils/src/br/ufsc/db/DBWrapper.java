package br.ufsc.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class used to wrap all database methods needed.
 * The use of any method must be done through this class.
 * 
 * @author	Artur Aquino, Vitor Fontes
 * @version	1.0, October 2012
 * @since	2012-11-28
 *
 */
public class DBWrapper {
	
	DBMethods methods = new DBMethods();
	STDBMethods stMethods = new STDBMethods();
	
	
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
	public String buildSQLQuery(String table, String fields, String where, String order, String addition) {
		return methods.buildSQLQuery(table, fields, where, order, addition);
	}
	
	//////////////////////////////////////////////////////
	////////////    Database Simple Methods   ////////////
	//////////////////////////////////////////////////////
	
	/**
	 * Executes the given SQL statement.
	 * 
	 * @param sql	any SQL statement
	 * @return		true if the first result is a ResultSet object; false if it is an update count, there are no results or there was an error
	 */
	public boolean execute(String sql){
		try {
			return  methods.execute(sql);
		} catch (SQLException e) {
			System.out.println("-----------------------------------\nDamaged sql text:\n"+sql+"\n-----------------------------------");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Executes the given SQL statement.
	 * 
	 * @param sql	any SQL statement
	 * @return		a ResultSet object that contains the data produced by the given query or null in case of error 
	 */
	public ResultSet executeQuery(String sql) {
		try {
			return methods.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("-----------------------------------\nDamaged sql text:\n"+sql+"\n-----------------------------------");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Executes a query on the specified database table, according to the parameters.
	 * It is probably more efficient, since it uses a simple statement.
	 * 
	 * @param table		any table of database
	 * @param fields	any valid fields of a SQL statement
	 * @param where		any valid WHERE clause
	 * @param addition	anything else to complement the statement
	 * @return			a new default Statement object or null in case of error 
	 */
	public ResultSet quickQuery(String table, String fields, String where, String order, String addition) {
		try {
			return methods.quickQuery(table, fields, where, order, addition);
		} catch (SQLException e) {
			System.out.println("-----------------------------------\nDamaged sql text:"
							+ "\ntable: " + (table==null ? "null" : table) 
							+ "\nfields: " + (fields==null ? "null" : fields)
							+ "\nwhere: " + (where==null ? "null" : where)
							+ "\norder: " + (order==null ? "null" : order)
							+ "\naddition: " + (addition==null ? "null" : addition)
							+"\n-----------------------------------");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Executes a query on the specified database table, according to the parameters.
	 * It is probably less efficient, since it uses a more complex statement that allows a better fetching of the ResultSet. 
	 * 
	 * @param table		any table of database
	 * @param fields	any valid fields of a SQL statement
	 * @param where		any valid WHERE clause
	 * @param addition	anything else to complement the statement
	 * @return			a new Statement object that will generate ResultSet objects with the given type and concurrency or null in case of error
	 */
	public ResultSet query(String table, String fields, String where, String order, String addition) {
		try {
			return methods.query(table, fields, where, order, addition);
		} catch (SQLException e) {
			System.out.println("-----------------------------------\nDamaged sql text:"
					+ "\ntable: " + (table==null ? "null" : table) 
					+ "\nfields: " + (fields==null ? "null" : fields)
					+ "\nwhere: " + (where==null ? "null" : where)
					+ "\norder: " + (order==null ? "null" : order)
					+ "\naddition: " + (addition==null ? "null" : addition)
					+"\n-----------------------------------");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Executes a query on the specified database table, according to fields and ordered by order.
	 * 
	 * @param table		any table of the database
	 * @param fields	any valid fields of a SQL statement
	 * @param order		any valid ORDER BY clause
	 * @return			a ResultSet object that contains the data produced by the given query; never null or null in case of error
	 */
	public ResultSet orderedQuery(String table, String fields, String order) {
		try {
			return methods.orderedQuery(table, fields, order);
		} catch (SQLException e) {
			System.out.println("-----------------------------------\nDamaged sql text:"
					+ "\ntable: " + (table==null ? "null" : table) 
					+ "\nfields: " + (fields==null ? "null" : fields)
					+ "\norder: " + (order==null ? "null" : order)
					+"\n-----------------------------------");
			e.printStackTrace();
			return null;
		}
	}
	
	
	///////////////////////////////////////////////////////////////
	////////////    Spatio-Temporal Database Methods   ////////////
	///////////////////////////////////////////////////////////////
	
	/**
	 * Implementation of postGIS ST_AsText
	 * 
	 * Returns the Well-Known Text (WKT) representation of the geometry/geography without SRID metadata.
	 * 
	 * @param geom		geometry
	 * @param alias		alias
	 * @return			String with SQL statement
	 */
	public String ST_astext(String geom, String alias){
		return stMethods.ST_astext(geom, alias);
	}
	
	/**
	 * Implementation of postGIS ST_Intersects
	 * 
	 * Returns TRUE if the Geometries/Geography "spatially intersect" - (share any portion of space) and FALSE if 
	 * they don’t (they are Disjoint).
	 * For geography -- tolerance is 0.00001 meters (so any points that close are considered to intersect) 
	 * 
	 * @param geom1		geometry
	 * @param geom2		geometry
	 * @param alias		alias
	 * @return			String with SQL statement
	 */
	public String ST_intersects(String geom1, String geom2, String alias){
		return stMethods.ST_intersects(geom1, geom2, alias);
	}
	
	/**
	 * Implementation of postGIS ST_buffer
	 * 
	 * Returns a geometry/geography that represents all points whose distance from this Geometry/geography is 
	 * less than or equal to distance.
	 * 
	 * @param geom		geometry
	 * @param radius	radius of buffer
	 * @param modifiers	num_seg_quarter_circle (default 8) or buffer_style_params (default "endcap=round join=round")
	 * @param alias		alias
	 * @return			String with SQL statement
	 */
	public String ST_buffer(String geom, double radius, String modifiers, String alias){
		return stMethods.ST_buffer(geom, radius, modifiers, alias);
	}

}
