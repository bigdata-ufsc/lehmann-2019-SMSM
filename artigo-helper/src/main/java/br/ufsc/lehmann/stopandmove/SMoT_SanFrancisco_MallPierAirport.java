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
		String movesTable = "stops_moves.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_move";
		String stopsTable = "stops_moves.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_stop";
		String pointsTable = "taxi.sanfrancisco_taxicab_airport_mall_pier_park_fisherman_cleaned";
		SanFranciscoCabDatabaseReader problem = new SanFranciscoCabDatabaseReader(false, (String[]) null, (String[]) null, (String[]) null, 
				stopsTable, movesTable, pointsTable);
		List<SemanticTrajectory> trajs = problem.read();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, stopsTable, null, "geom");

		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from " + stopsTable);
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from " + movesTable);
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update " + pointsTable + " set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into " + stopsTable + "(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon, \"POI\") values (?,?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into " + movesTable + "(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
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
							 * Fisherman - POLYGON ((-13627840.033406138 4551869.787694807, -13627849.522173362 4552506.721194661, -13627191.238947254 4552512.651674176, -13627193.848358242 4551863.857215292, -13627840.033406138 4551869.787694807))
							 * 
							 * Park - POLYGON ((-13632086.319896387 4546395.898772847, -13637841.936360996 4546136.587574809, -13637933.10171011 4547184.934121963, -13631546.63338944 4547700.141164636, -13631299.277073756 4546429.629179532, -13631895.180925177 4546339.681428374, -13632086.319896387 4546395.898772847))
							 * 
							 */
							statement.setString(12, String.valueOf(stop.getStopName()));
							double lat, lon;
							if (stop.getStopName().equals("intersection_101_280")) {
								// '37.73425','-122.405805'
								lat = -13626151.88274;
								lon = 4541951.97381;
							} else if (stop.getStopName().equals("pier")) {
								// 37.78429, -122.38854
								lat = -13624229.95173;
								lon = 4548997.88143;
							} else if (stop.getStopName().equals("airport")) {
								// '37.61592','-122.38831'
								lat = -13624204.34825;
								lon = 4525309.37467;
							} else if (stop.getStopName().equals("mall")) {
								// '37.78398','-122.40647'
								lat = -13626225.9102;
								lon = 4548954.21705;
							} else if (stop.getStopName().equals("park")) {
								// 37.76916	-122.48115
								lat = -13634539.24977;
								lon = 4546866.99132;
							} else if (stop.getStopName().equals("fisherman")) {
								// 37.80694	-122.41807
								lat = -13627517.2163;
								lon = 4552188.69437;
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
