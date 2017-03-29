package br.ufsc.db.destination;

import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.DBConstants;
//TODO Verify if this class needs to exist in the API
@Deprecated
public class PGStorer extends DataStorer {

	@Override
	public void connect(DataStorage storage) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		this.storage=storage;
		Driver newDriver = (Driver) Class.forName("org.postgresql.Driver")
				.newInstance();
		DriverManager.registerDriver(newDriver);
		connection = DriverManager.getConnection("jdbc:postgresql://"+storage.getHost()+":"+storage.getPort()+"/"+storage.getDatabase(),
				storage.getUser(), storage.getPass());
	}



	@Override
	public void storeMoves(List<Move> moves) throws SQLException {
		// TODO Auto-generated method stub
	}
	
	private void insertStops(String values){
		values = values.substring(0, values.length()-2);
		
		String sql = "INSERT INTO " + storage.getStopTable() + " ("+ DBConstants.STOP_TID + "," + DBConstants.STOP_ID + "," + 
				DBConstants.STOP_NAME +  "," + DBConstants.STOP_START_TIME + "," + DBConstants.STOP_END_TIME + "," +  DBConstants.STOP_RF_TABLE + "," + 
				DBConstants.STOP_RF_ID + "," + DBConstants.STOP_AVG + "," + DBConstants.STOP_GEOM + ") VALUES " + values + ";";
		
		execute(sql);
	}
	
	private void createStopTable(int srid) throws SQLException{
		DatabaseMetaData metadata = connection.getMetaData();
		String tableName = storage.getStopTable();
		ResultSet resultSet = metadata.getTables(null, null, tableName, null);
		if(resultSet.next()){		// table exists
			execute("DROP TABLE "+tableName+";");
			execute("DELETE FROM geometry_columns WHERE f_table_name = '"+tableName+"';");
		}
		resultSet.close();
		String sql = "CREATE TABLE "+tableName+" ("+
						DBConstants.STOP_GID		+" serial NOT NULL,"+
						DBConstants.STOP_TID		+" integer NOT NULL,"+
						DBConstants.STOP_ID			+" integer NOT NULL,"+
						DBConstants.STOP_NAME		+" character varying,"+
						DBConstants.STOP_START_TIME	+" timestamp without time zone,"+
						DBConstants.STOP_END_TIME	+" timestamp without time zone,"+
						DBConstants.STOP_RF_TABLE	+" character varying,"+
						DBConstants.STOP_RF_ID		+" integer,"+
						DBConstants.STOP_AVG		+" real," +
						"    CONSTRAINT "+tableName+"_gidkey PRIMARY KEY ("+DBConstants.STOP_GID+")"+
						") WITHOUT OIDS;";
		execute(sql);
		execute("SELECT AddGeometryColumn('"+tableName+"', '"+DBConstants.STOP_GEOM+"',"+srid+", 'LINESTRING', 2);");
	}



	@Override
	public void storeStops(List<Stop> stops, int srid) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
