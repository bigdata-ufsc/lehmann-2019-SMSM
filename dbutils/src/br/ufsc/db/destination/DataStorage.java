package br.ufsc.db.destination;

import java.sql.SQLException;

public class DataStorage {
	private String user;
	private String pass;
	private String host;
	private Integer port;
	private String database;
	private DataStoreType type;
	private String stopTable;
	
	public DataStorage(String user, String pass,String host,Integer port,String database,DataStoreType type, String stopTable){
		this.user=user;
		this.pass=pass;
		this.host=host;
		this.port=port;
		this.database=database;
		this.type=type;
		this.stopTable=stopTable;
	}
	
	public String getStopTable() {
		return stopTable;
	}

	public DataStorer getStorer() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		DataStorer storer = type.getStorer();
		storer.connect(this);
		return storer;
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

	public DataStoreType getType() {
		return type;
	}

	public String getHost() {
		return host;
	}
}
