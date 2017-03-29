package br.ufsc.db.factory;

import java.sql.SQLException;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class RetrieverFactory {
	
	
	public static DataRetriever getRetriever(String user,String pass,String host,int port,String database,DataSourceType type,String table,String geomColumn) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		DataSource source = new DataSource(user, pass, host, port,
			database, type, table, null, geomColumn);
		DataRetriever retriever = source.getRetriever();
		return retriever;
	}
}
