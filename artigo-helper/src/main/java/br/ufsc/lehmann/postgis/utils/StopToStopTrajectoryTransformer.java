package br.ufsc.lehmann.postgis.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class StopToStopTrajectoryTransformer {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_vehicle_stop", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("SELECT infered_trip_id, gid, semantic_stop_id, semantic_move_id "+
			  "from bus.nyc_20140927_zoned "+
			  "order by infered_trip_id,time_received");
		String currentTid = null;
		Long currentStopId = null, currentMoveId = null;
		Set<Long> gidsToRemove = new LinkedHashSet<>();
		while(rs.next()) {
			if(currentTid == null || !currentTid.equals(rs.getString("infered_trip_id"))) {
				currentTid = rs.getString("infered_trip_id");
				if(currentMoveId != null) {
					gidsToRemove.add(rs.getLong("gid"));
					currentMoveId = null;
				}
				currentStopId = null;
			}
			Long stopId = rs.getLong("semantic_stop_id");
			boolean isStop = !rs.wasNull();
			Long moveId = rs.getLong("semantic_move_id");
			boolean isMove = !rs.wasNull();
			if(isStop) {
				if(currentMoveId != null) {
					currentMoveId = null;
				}
				currentStopId = stopId;
			}
			if(isMove) {
				if(currentStopId == null) {
					gidsToRemove.add(rs.getLong("gid"));
				} else {
					currentStopId = null;
					currentMoveId = moveId;
				}
			}
		}
		System.out.println("gid in (" + gidsToRemove + ")");
		conn.commit();
	}
}
