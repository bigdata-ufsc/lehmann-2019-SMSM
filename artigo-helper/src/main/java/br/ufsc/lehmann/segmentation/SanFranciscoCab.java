package br.ufsc.lehmann.segmentation;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabRecord;

public class SanFranciscoCab {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.sanfrancisco_taxicab_crawdad", null, "geom");
		Multimap<Integer, SanFranciscoCabRecord> registers = MultimapBuilder.hashKeys().arrayListValues().build();
		System.out.println("Connecting...");
		Connection conn = source.getRetriever().getConnection();
		System.out.println("Querying...");
		PreparedStatement ps = conn.prepareStatement("select gid, taxi_id, lat, lon, \"timestamp\", ocupation, airport, mall, road, direction, stop, route "//
				+ "from taxi.sanfrancisco_taxicab_crawdad order by taxi_id, \"timestamp\"");
		ResultSet rs = ps.executeQuery();
		int generatedTid = 0;
		boolean openTrip = true;
		Timestamp previousSign = null;
		System.out.println("Fetching...");
		while(rs.next()) {
			int passengers = rs.getInt("ocupation");
			Timestamp timestamp = rs.getTimestamp("timestamp");
			if((rs.wasNull() || passengers == 0) && openTrip) {
				generatedTid++;
				openTrip = false;
			} else if(passengers > 0 && !openTrip) {
				generatedTid++;
				openTrip = true;
			} else if(previousSign != null && ChronoUnit.MINUTES.between(previousSign.toInstant(), timestamp.toInstant()) > 5) {
				generatedTid++;
			}
			previousSign = timestamp;
			SanFranciscoCabRecord record = new SanFranciscoCabRecord(generatedTid, //
					rs.getInt("gid"), rs.getInt("taxi_id"), //
					timestamp, 
					passengers,
					rs.getDouble("lon"),
					rs.getDouble("lat"), 
					rs.getBoolean("airport"),
					rs.getBoolean("mall"),
					rs.getInt("road"),
					rs.getString("direction"),
					rs.getString("stop"),
					rs.getString("route"),
					null, null);
			registers.put(generatedTid, record);
		}
		System.out.println("Total trajectories count: " + registers.keySet().size());
		rs.close();
		ps.close();
		conn.setAutoCommit(false);
		ps = conn.prepareStatement("update taxi.sanfrancisco_taxicab_crawdad set tid = ? where gid in (SELECT * FROM unnest(?))");
		Set<Integer> tids = registers.keySet();
		System.out.println("Updating...");
		for (Integer tid : tids) {
			List<Integer> gids = registers.get(tid).stream().map((SanFranciscoCabRecord r) -> r.getGid()).collect(Collectors.toList());
			Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
			ps.setInt(1, tid);
			ps.setArray(2, array);
			ps.addBatch();
			if(tid % 100 == 0) {
				System.out.println("Current tid: " + tid);
				ps.executeBatch();
				conn.commit();
			}
		}
		ps.executeBatch();
		conn.commit();
	}
}
