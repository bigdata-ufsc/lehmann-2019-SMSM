package br.ufsc.lehmann.postgis.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class TrajectoryBetweenAreaCleaner {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_vehicle_stop", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement statement = conn.createStatement();
		String pointsTable = "taxi.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_cleaned";
		ResultSet rs = statement.executeQuery("SELECT tid, gid, mall as at_mall,"+
				"airport as at_airport,"+
				"pier as at_pier,"+
				"direction "+
			  "FROM "+pointsTable+
			  " order by tid, \"timestamp\";");
		Long currentTid = null;
		String direction = null;
		boolean toAirport = false, initialTrajectoryReached = false, finalTrajectoryReached = false;
		List<Long> gidsToRemove = new ArrayList<>();
		while(rs.next()) {
			if(currentTid == null || !currentTid.equals(rs.getLong("tid"))) {
				currentTid = rs.getLong("tid");
				direction = rs.getString("direction");
				if(direction == null) {
					continue;
				}
				if(StringUtils.startsWith(direction, "airport to")) {
					toAirport = false;
				} else if(StringUtils.endsWith(direction, "to airport")) {
					toAirport = true;
				} else{
					throw new RuntimeException("Unknown direction: " + direction);
				}
				initialTrajectoryReached = false;
				finalTrajectoryReached = false;
			}
			if(!initialTrajectoryReached) {
				if(toAirport) {
					if(!(rs.getBoolean("at_pier") || rs.getBoolean("at_mall"))) {
						gidsToRemove.add(rs.getLong("gid"));
					} else {
						initialTrajectoryReached = true;
					}
				} else {
					if(!rs.getBoolean("at_airport")) {
						gidsToRemove.add(rs.getLong("gid"));
					} else {
						initialTrajectoryReached = true;
					}
				}
			} else if(!finalTrajectoryReached) {
				if(toAirport) {
					if(rs.getBoolean("at_airport")) {
						finalTrajectoryReached = true;
					}
				} else {
					if(rs.getBoolean("at_mall") || rs.getBoolean("at_pier")) {
						finalTrajectoryReached = true;
					}
				}
			} else {
				if(toAirport) {
					if(!rs.getBoolean("at_airport")) {
						gidsToRemove.add(rs.getLong("gid"));
					}
				} else {
					if(!(rs.getBoolean("at_pier") || rs.getBoolean("at_mall"))) {
						gidsToRemove.add(rs.getLong("gid"));
					}
				}
			}
		}
		System.out.println("gid in (" + gidsToRemove + ")");
		conn.commit();
	}
}
