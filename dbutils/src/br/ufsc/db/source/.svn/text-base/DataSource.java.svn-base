package br.ufsc.db.source;

import java.sql.SQLException;
import java.util.Set;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class DataSource {
	private String user;
	private String pass;
	private String host;
	private Integer port;
	private String database;
	private DataSourceType type;
	private String trajectoryTable;
	private Set<String> featureTables;
	private String geomCol;
	
	public DataSource(String user, String pass, String host, Integer port, String database, 
			DataSourceType type, String trajectoryTable, Set<String> featureTables, String geomCol){
		this.user=user;
		this.pass=pass;
		this.host=host;
		this.port=port;
		this.database=database;
		this.type=type;
		this.trajectoryTable=trajectoryTable;
		this.featureTables=featureTables;
		this.geomCol = geomCol;
	}
	
	public String getTrajectoryTable() {
		return trajectoryTable;
	}

	public Set<String> getFeatureTables() {
		return featureTables;
	}
	
	public String getGeomCol(){
		return geomCol;
	}

	public DataRetriever getRetriever() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		DataRetriever retriever = type.getRetriever();
		retriever.connect(this);
		return retriever;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public Integer getPort() {
		return port;
	}

	public String getDatabase() {
		return database;
	}

	public DataSourceType getType() {
		return type;
	}

	public String getHost() {
		return host;
	}
}
