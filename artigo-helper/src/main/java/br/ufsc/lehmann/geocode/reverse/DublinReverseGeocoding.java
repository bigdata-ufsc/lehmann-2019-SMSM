package br.ufsc.lehmann.geocode.reverse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.asynchttpclient.DefaultAsyncHttpClient;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;

public class DublinReverseGeocoding {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.dublin_201301", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery("SELECT stop_id, start_lat, start_lon, begin, end_lat, end_lon, length, centroid_lat, " + //
				"centroid_lon, start_time, end_time, street " + //
				"FROM stops_moves.bus_dublin_201301_stop "//
				+ "where stop_id in (select distinct semantic_stop_id from bus.dublin_201301 " +
"where trim(journey_pattern) in ('00671001', '00431001')) and street is null");
		Map<Integer, Stop> stops = new HashMap<>();
		System.out.println("Fetching...");
		while (stopsData.next()) {
			int stopId = stopsData.getInt("stop_id");
			Stop stop = stops.get(stopId);
			if (stop == null) {
				stop = new Stop(stopId, null, //
						stopsData.getTimestamp("start_time").getTime(), //
						stopsData.getTimestamp("end_time").getTime(), //
						new TPoint(stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon")), //
						stopsData.getInt("begin"), //
						new TPoint(stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon")), //
						stopsData.getInt("length"), //
						new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon")),//
						stopsData.getString("street")//
				);
				stops.put(stopId, stop);
			}
		}
		System.out.println("Updating...");
		PreparedStatement ps = conn.prepareStatement("update stops_moves.bus_dublin_201301_stop set street = ? where stop_id = ?");
		ReverseGeocoding geocoding = new ReverseGeocoding();
		int registers = 0, counter = 0;
		DefaultAsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
		try {
//			try {
//				Thread.sleep(5 * 60 * 1000);
//			} catch (InterruptedException e1) {
//				throw new RuntimeException(e1);
//			}
			for (Iterator iterator = stops.entrySet().iterator(); iterator.hasNext();) {
				registers++;
				Map.Entry<Integer, Stop> entry = (Map.Entry<Integer, Stop>) iterator.next();
				boolean added = false;
				while (!added) {
					try {
						Place place = geocoding.fromLatLon(asyncHttpClient, entry.getValue().getCentroid().getX(),
								entry.getValue().getCentroid().getY());
						ps.setString(1, place.getAddress().getRoad());
						ps.setInt(2, entry.getKey());
						ps.addBatch();
						added = true;
					} catch (IllegalArgumentException e) {
						try {
							Thread.sleep(5 * 60 * 1000);
							System.err.println("Trying again...");
						} catch (InterruptedException e1) {
							throw new RuntimeException(e1);
						}
					}
				}
				if (registers % 100 == 0) {
					System.out.printf("%d\n", counter++);
					ps.executeBatch();
					conn.commit();
					try {
						Thread.sleep(1 * 60 * 1000);
					} catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
			ps.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			if (e.getNextException() != null) {
				throw e.getNextException();
			}
			throw e;
		} finally {
			asyncHttpClient.close();
		}
	}
}
