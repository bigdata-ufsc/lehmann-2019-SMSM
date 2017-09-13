package br.ufsc.lehmann.postgis.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;

public class TransformationCoordinates {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_vehicle_stop", null,
				null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		List<Integer> srids = new ArrayList<>();
		ResultSet sridsData = st.executeQuery("select srid from spatial_ref_sys");
		System.out.println("Fetching SRIDs...");
		while(sridsData.next()) {
			srids.add(sridsData.getInt(1));
		}
		//
		PreparedStatement ps = conn.prepareStatement("SELECT 1 "//
				+ "FROM stops_moves.sergipe_tracks_move "//
				+ "where st_x(ST_TRANSFORM(ST_SetSRID(ST_MakePoint(475697.1, 4207209.6), ? ), 4326)) between 23.7248427950808 - 0.05 and 23.7248427950808 + 0.05 "//
				+ "limit 1");
		System.out.println("Testing SRIDs...");
		for (Integer srid : srids) {
			ps.setInt(1, srid);
			try {
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					System.out.println(srid);
					conn.close();
					return;
				}
			} catch (SQLException e) {
				//
				ps.close();
				conn.close();
				retriever = source.getRetriever();
				conn = retriever.getConnection();
				ps = conn.prepareStatement("SELECT 1 "//
						+ "FROM patel.vehicle "//
						+ "where st_y(ST_TRANSFORM(ST_SetSRID(ST_MakePoint(longitude, latitude), ? ), 4326)) = latitude_4326 "//
						+ "limit 1");
				System.err.println(e.getMessage());
			}
		}
		//
	}
}
