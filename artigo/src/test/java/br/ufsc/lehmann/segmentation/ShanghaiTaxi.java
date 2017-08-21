package br.ufsc.lehmann.segmentation;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.TaxiShangaiRecord;

public class ShanghaiTaxi {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "taxi.shangai_20070220", null, "geom");
		Multimap<Integer, TaxiShangaiRecord> registers = MultimapBuilder.hashKeys().arrayListValues().build();
		Connection conn = source.getRetriever().getConnection();
		PreparedStatement ps = conn.prepareStatement("select taxi_id, \"time\", longitude, latitude, speed, angle, passengers, gid "//
				+ "from taxi.shangai_20070220 order by taxi_id, \"time\"");
		ResultSet rs = ps.executeQuery();
		int generatedTid = 0;
		boolean openTrip = true;
		while(rs.next()) {
			int passengers = rs.getInt("passengers");
			if((rs.wasNull() || passengers == 0) && openTrip) {
				generatedTid++;
				openTrip = false;
			} else if(passengers > 0 && !openTrip) {
				generatedTid++;
				openTrip = true;
			}
			TaxiShangaiRecord record = new TaxiShangaiRecord(generatedTid, //
					rs.getInt("taxi_id"), rs.getInt("gid"), //
					rs.getTimestamp("time"), rs.getDouble("longitude"), //
					rs.getDouble("latitude"), passengers, //
					rs.getDouble("speed"), rs.getDouble("angle"), null, null);
			registers.put(generatedTid, record);
		}
		System.out.println("Total trajectories count: " + registers.keySet().size());
		rs.close();
		ps.close();
		conn.setAutoCommit(false);
		ps = conn.prepareStatement("update taxi.shangai_20070220 set tid = ? where gid in (SELECT * FROM unnest(?))");
		Set<Integer> tids = registers.keySet();
		for (Integer tid : tids) {
			List<Integer> gids = registers.get(tid).stream().map((TaxiShangaiRecord r) -> r.getGid()).collect(Collectors.toList());
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
