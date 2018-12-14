package br.ufsc.lehmann.postgis.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class StopToStopTrajectoryTransformer_Geolife2 {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		String pointsTable = "geolife.geolife2_cleaned";
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, pointsTable, null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		while(true) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT tid, gid, \"POI\" as poi "+
					"from "+ pointsTable + 
					" order by tid,time,gid");
			Integer currentTid = null;
			Set<Long> gidsToRemove = new LinkedHashSet<>();
			String currentStopId = null;
			Set<Long> lastGids = new LinkedHashSet<>();
			while(rs.next()) {
				if(currentTid == null || !currentTid.equals(rs.getInt("tid"))) {
					currentTid = rs.getInt("tid");
					if(!lastGids.isEmpty()) {
						gidsToRemove.addAll(lastGids);
						lastGids.clear();
					}
					currentStopId = null;
				}
				String stopId = rs.getString("poi");
				boolean isStop = !rs.wasNull();
				if(isStop) {
					if(currentStopId == null) {
						if(!lastGids.isEmpty()) {
							gidsToRemove.addAll(lastGids);
						}
					}
					currentStopId = stopId;
					lastGids.clear();
				} else {
					long lastGid = rs.getLong("gid");
					lastGids.add(lastGid);
				}
			}
			if(!lastGids.isEmpty()) {
				gidsToRemove.addAll(lastGids);
			}
			System.out.println(gidsToRemove.size());
			if(gidsToRemove.isEmpty()) {
				return;
			}
			int i = 0;
			for (; i < gidsToRemove.size(); i+=10000) {
				int min = Math.min(gidsToRemove.size() - i, 10000);
				System.out.printf("Deleting %d rows\n", min);
				List<Long> collect = gidsToRemove.stream().skip(i).limit(min).collect(Collectors.toList());
				String string = collect.toString();
				
				String sql = "delete from "+pointsTable + " where gid in (" + string.substring(1, string.length() - 1) + ")";
				conn.prepareStatement(sql).execute();
				conn.commit();
			}
		}
	}
}
