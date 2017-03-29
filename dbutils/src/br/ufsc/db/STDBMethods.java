package br.ufsc.db;


/**
 * Class with some methods related to spatio-temporal databases.
 * Since there is a huge amount of them only a few where implemented, but the number
 * must increase depending on the future needs.
 * 
 * The implementation is different from DBMethods class because here the methods are often used
 * as part of an SQL query. The methods just mount the strings to be used there, then.
 * 
 * @author Artur Aquino, Vitor Fontes
 *
 */
public class STDBMethods {
	
	/**
	 * Method to add an alias to any other method needed.
	 * 
	 * @param alias		alias
	 * @return			String representing the code to add the alias
	 */
	private String addAlias(String alias){
		String ret = "";
		if(alias!=null)
			ret += " as " + alias;
		return ret;
	}

	/**
	 * Implementation of postGIS ST_AsText
	 * 
	 * @param geom		geometry
	 * @param alias		alias
	 * @return			String with SQL statement
	 */
	String ST_astext(String geom, String alias){
		String ret = "st_astext(" + geom + ")";
		ret += this.addAlias(alias);
		return ret;
	}
	
	/**
	 * Implementation of postGIS ST_Intersects
	 * 
	 * @param geom1		geometry
	 * @param geom2		geometry
	 * @param alias		alias
	 * @return			String with SQL statement
	 */
	String ST_intersects(String geom1, String geom2, String alias){
		return "st_intersects(" + geom1 + ", " + geom2 + ")" + this.addAlias(alias);
	}
	
	/**
	 * Implementation of postGIS ST_buffer
	 * 
	 * @param geom		geometry
	 * @param radius	radius of buffer
	 * @param modifiers	num_seg_quarter_circle (default 8) or buffer_style_params (default "endcap=round join=round")
	 * @param alias		alias
	 * @return			String with SQL statement
	 */
	String ST_buffer(String geom, double radius, String modifiers, String alias){
		if(modifiers==null)
			modifiers = "";
		else
			modifiers = ", " + modifiers;
		return "st_buffer(" + geom + ", " + radius + modifiers + ")" + this.addAlias(alias);
	}
	
}
