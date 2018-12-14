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

public class TrajectoryBetweenAreaCleaner_Geolife2 {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		String pointsTable = "geolife.geolife2_cleaned";
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, pointsTable, null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("SELECT tid, gid, \"POI\" as poi,"+
				"direction "+
			  "FROM "+pointsTable+
			  " order by tid, time;");
		Long currentTid = null;
		String direction = null;
		boolean toMicrosoft = false, initialTrajectoryReached = false, finalTrajectoryReached = false;
		List<Long> gidsToRemove = new ArrayList<>();
		while(rs.next()) {
			if(currentTid == null || !currentTid.equals(rs.getLong("tid"))) {
				currentTid = rs.getLong("tid");
				direction = rs.getString("direction");
				if(direction == null) {
					continue;
				}
				if(StringUtils.startsWith(direction, "Microsoft to")) {
					toMicrosoft = false;
				} else if(StringUtils.endsWith(direction, "to Microsoft")) {
					toMicrosoft = true;
				} else{
					throw new RuntimeException("Unknown direction: " + direction);
				}
				initialTrajectoryReached = false;
				finalTrajectoryReached = false;
			}
			if(!initialTrajectoryReached) {
				if(toMicrosoft) {
					if(!StringUtils.equalsIgnoreCase(rs.getString("poi"), "Dormitory")) {
						gidsToRemove.add(rs.getLong("gid"));
					} else {
						initialTrajectoryReached = true;
					}
				} else {
					if(!StringUtils.equalsIgnoreCase(rs.getString("poi"), "Microsoft")) {
						gidsToRemove.add(rs.getLong("gid"));
					} else {
						initialTrajectoryReached = true;
					}
				}
			} else if(!finalTrajectoryReached) {
				if(toMicrosoft) {
					if(StringUtils.equalsIgnoreCase(rs.getString("poi"), "Microsoft")) {
						finalTrajectoryReached = true;
					}
				} else {
					if(StringUtils.equalsIgnoreCase(rs.getString("poi"), "Dormitory")) {
						finalTrajectoryReached = true;
					}
				}
			} else {
				if(toMicrosoft) {
					if(!StringUtils.equalsIgnoreCase(rs.getString("poi"), "Microsoft")) {
						gidsToRemove.add(rs.getLong("gid"));
					}
				} else {
					if(!StringUtils.equalsIgnoreCase(rs.getString("poi"), "Dormitory")) {
						gidsToRemove.add(rs.getLong("gid"));
					}
				}
			}
		}
		System.out.println("Gids to remove: " + gidsToRemove.size());
		for (int i = 0; i < gidsToRemove.size(); i+=10000) {
			String string = gidsToRemove.subList(i, i + Math.min(10000, gidsToRemove.size() - i)).toString();
			String sql = "delete from "+pointsTable + " where gid in (" + string.substring(1, string.length() - 1) + ")";
			conn.prepareStatement(sql).execute();
			conn.commit();
		}
		conn.commit();
	}
}
