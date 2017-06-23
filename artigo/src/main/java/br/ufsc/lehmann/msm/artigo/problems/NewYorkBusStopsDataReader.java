package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class NewYorkBusStopsDataReader {
	
	public List<Stop> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.nyc_20140927", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, end_lat, end_lon, centroid_lat, " + //
						"centroid_lon, start_time, end_time " + //
						"FROM stops_moves.bus_nyc_20140927");
		Map<Integer, Stop> stops = new HashMap<>();
		while(stopsData.next()) {
			int stopId = stopsData.getInt("stop_id");
			Stop stop = stops.get(stopId);
			if(stop == null) {
				stop = new Stop(stopId, null, stopsData.getTimestamp("start_time"), stopsData.getTimestamp("end_time"), new TPoint(stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon")),
						new TPoint(stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon")), new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon")));
				stops.put(stopId, stop);
			}
		}
		System.out.printf("Retrieved %d stops\n", stops.size());
		return new ArrayList<>(stops.values());
	}
}
