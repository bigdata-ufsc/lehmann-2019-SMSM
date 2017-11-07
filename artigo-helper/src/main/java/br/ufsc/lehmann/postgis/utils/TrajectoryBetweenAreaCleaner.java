package br.ufsc.lehmann.postgis.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

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
		ResultSet rs = statement.executeQuery("SELECT tid, gid, ST_INTERSECTS(ST_GeomFromText('POLYGON((-122.40899 37.78196, -122.40395 37.78196, -122.40395 37.786, -122.40899 37.786, -122.40899 37.78196))'), st_makepoint(lon, lat)) as at_mall,"+
				"ST_INTERSECTS(ST_GeomFromText('POLYGON((-122.39258 37.61372, -122.38404 37.61372, -122.38404 37.61812, -122.39258 37.61812, -122.39258 37.61372))'), st_makepoint(lon, lat)) as at_airport,"+
				"direction "+
			  "FROM taxi.sanfrancisco_taxicab_cleaned "+
			  "order by tid, \"timestamp\";");
		Long currentTid = null;
		String direction = null;
		boolean toAirport = false, initialTrajectoryReached = false, finalTrajectoryReached = false;
		List<Long> gidsToRemove = new ArrayList<>();
		while(rs.next()) {
			if(currentTid == null || !currentTid.equals(rs.getLong("tid"))) {
				currentTid = rs.getLong("tid");
				direction = rs.getString("direction");
				if(direction.equals("airport to mall")) {
					toAirport = false;
				} else if(direction.equals("mall to airport")) {
					toAirport = true;
				} else{
					throw new RuntimeException("Unknown direction: " + direction);
				}
				initialTrajectoryReached = false;
				finalTrajectoryReached = false;
			}
			if(!initialTrajectoryReached) {
				if(toAirport) {
					if(!rs.getBoolean("at_mall")) {
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
					if(rs.getBoolean("at_mall")) {
						finalTrajectoryReached = true;
					}
				}
			} else {
				if(toAirport) {
					if(!rs.getBoolean("at_airport")) {
						gidsToRemove.add(rs.getLong("gid"));
					}
				} else {
					if(!rs.getBoolean("at_mall")) {
						gidsToRemove.add(rs.getLong("gid"));
					}
				}
			}
		}
		System.out.println("gid in (" + gidsToRemove + ")");
		conn.commit();
	}
}
