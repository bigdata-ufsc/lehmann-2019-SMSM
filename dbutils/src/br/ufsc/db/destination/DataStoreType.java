package br.ufsc.db.destination;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSourceType;

public enum DataStoreType {
	PGSQL("PGSQL",new PGStorer());
	
	private String id;
	private DataStorer connector;
	DataStoreType(String id,DataStorer conn) {
		this.setId(id);
		this.setConnection(conn);
	}
	
	public static DataRetriever getConnectorByID(String id){
		for (DataSourceType type : DataSourceType.values()) {
			if (type.getId().equals(id)) {
				return type.getRetriever();
			}
		}
		return null;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setConnection(DataStorer conn) {
		this.connector = conn;
	}

	public DataStorer getStorer() {
		return connector;
	}
}
