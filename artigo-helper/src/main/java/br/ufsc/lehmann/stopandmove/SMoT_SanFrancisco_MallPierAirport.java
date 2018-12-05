package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDatabaseReader;

public class SMoT_SanFrancisco_MallPierAirport {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		SanFranciscoCabDatabaseReader problem = new SanFranciscoCabDatabaseReader(false, new String[] {"101", "280"}, null, null, 
				"stops_moves.taxi_sanfrancisco_airport_mall_pier_stop", "stops_moves.taxi_sanfrancisco_airport_mall_pier_move", "taxi.sanfrancisco_taxicab_airport_mall_pier_cleaned");
		List<SemanticTrajectory> trajs = problem.read();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.taxi_sanfrancisco_airport_mall_pier_stop", null, "geom");

		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.taxi_sanfrancisco_airport_mall_pier_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.taxi_sanfrancisco_airport_mall_pier_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update taxi.sanfrancisco_taxicab_airport_mall_pier_cleaned set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.taxi_sanfrancisco_airport_mall_pier_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon, \"POI\") values (?,?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.taxi_sanfrancisco_airport_mall_pier_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
			FastSMoT<String, Number> fastSMoT = new FastSMoT<>(SanFranciscoCabDatabaseReader.REGION_INTEREST);
			List<StopAndMove> bestSMoT = new ArrayList<>();
			for (SemanticTrajectory T : trajs) {
				bestSMoT.add(fastSMoT.findStops(T, sid, mid));
			}
			StopAndMoveExtractor.persistStopAndMove(conn, update, insertStop, new StopAndMoveExtractor.StopPersisterCallback() {

				@Override
						public void parameterize(PreparedStatement statement, Stop stop) throws SQLException {
							/**
							 * Intersection - POLYGON ((-122.40946 37.7375, -122.40215 37.7375, -122.40215 37.731, -122.40946 37.731, -122.40946 37.7375))
							 * 
							 * Brisbane - POLYGON ((-122.391 37.688, -122.388 37.688, -122.388 37.679, -122.391 37.679, -122.391 37.688))
							 * 
							 * Pier - POLYGON ((-13624607.041687667 4548581.778072609,  -13624607.041687667 4549414.764443452, -13623853.387352144 4549414.764443452, -13623853.387352144 4548581.778072609, -13624607.041687667 4548581.778072609))
							 * 
							 * Airport - POLYGON ((-122.39258 37.61372, -122.39258 37.61812, -122.38404 37.61812, -122.38404 37.61372, -122.39258 37.61372))
							 * 
							 * WSFC - POLYGON ((-122.40899 37.78196, -122.40899 37.786, -122.40395 37.786, -122.40395 37.78196, -122.40899 37.78196))
							 * 
							 */
							statement.setString(12, String.valueOf(stop.getStopName()));
							double lat, lon;
							if (stop.getStopName().equals("intersection_101_280")) {
								// '37.73425','-122.405805'
								lat = 37.73425;
								lon = -122.405805;
							} else if (stop.getStopName().equals("pier")) {
								// 37.78429, -122.38854
								lat = 37.78429;
								lon = -122.38854;
							} else if (stop.getStopName().equals("airport")) {
								// '37.61592','-122.38831'
								lat = 37.61592;
								lon = -122.38831;
							} else if (stop.getStopName().equals("mall")) {
								// '37.78398','-122.40647'
								lat = 37.78398;
								lon = -122.40647;
							} else {
								lat = -1;
								lon = -1;
							}
							statement.setDouble(11, lat);// lat
							statement.setDouble(10, lon);// lon
						}
				
			}, insertMove, bestSMoT);
						
		} finally {
			update.close();
			insertStop.close();
			insertMove.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

}
