package br.ufsc.lehmann.geocode.reverse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class DublinFillDupesReverseGeocoding {

	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.dublin201301", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);

		ResultSet stopsData = st.executeQuery("SELECT to_fill.stop_id, trim(filled.street) " //
				+ "FROM stops_moves.bus_dublin_201301_stop to_fill inner join "//
				+ "stops_moves.bus_dublin_201301_stop filled on ST_DWithin(ST_Transform(ST_SetSRID(ST_Point(to_fill.centroid_lon,to_fill.centroid_lat), 4326), 3857), "//
				+ "ST_Transform(ST_SetSRID(ST_Point(filled.centroid_lon,filled.centroid_lat), 4326), 3857), 5) "//
				+ "where to_fill.stop_id <> filled.stop_id "
				+ "and to_fill.street is null "
				+ "and filled.street is not null "
				+ "and filled.stop_id in (select semantic_stop_id from bus.dublin_201301 where trim(journey_pattern) in ('017A1003', '00791001'))"
				+ "and to_fill.stop_id in (select semantic_stop_id from bus.dublin_201301 where trim(journey_pattern) in ('017A1003', '00791001'))");
		Map<Integer, String> toFill = new HashMap<>();
		System.out.println("Fetching...");
		while(stopsData.next()) {
			toFill.put(stopsData.getInt(1), stopsData.getString(2));
		}
		System.out.printf("To update %d registers", toFill.size());
		stopsData.close();
		st.close();
		int registers = 0;
		conn.setAutoCommit(false);
		PreparedStatement ps = conn.prepareStatement("update stops_moves.bus_dublin_201301_stop set street = ? where stop_id = ?");
		for (Map.Entry<Integer, String> entry : toFill.entrySet()) {
			ps.setString(1, entry.getValue());
			ps.setInt(2, entry.getKey());
			ps.addBatch();
			registers++;
			if(registers % 500 == 0) {
				ps.executeBatch();
				conn.commit();
			}
		}
		ps.executeBatch();
		conn.commit();
		
	}
}
