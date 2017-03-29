package br.ufsc.db.source;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public enum DataSourceType {
	PGSQL("PGSQL",new PGRetriever());
	
	private String id;
	private DataRetriever connector;
	DataSourceType(String id,DataRetriever conn) {
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

	public void setConnection(DataRetriever conn) {
		this.connector = conn;
	}

	public DataRetriever getRetriever() {
		return connector;
	}
	
}
