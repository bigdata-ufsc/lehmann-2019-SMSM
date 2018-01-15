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
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCab_AirportMallDirection_Problem;

public class SMoT_SanFranciscoCab {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		SanFranciscoCabProblem problem = new SanFranciscoCab_AirportMallDirection_Problem(SanFranciscoCabDatabaseReader.STOP_REGION_SEMANTIC, false, new String[] {"101", "280"}, new String[] {"mall to airport", "airport to mall"});
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.taxi_sanfrancisco_stop", null, "geom");

		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.taxi_sanfrancisco_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.taxi_sanfrancisco_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update taxi.sanfrancisco_taxicab set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.taxi_sanfrancisco_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon, \"POI\") values (?,?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.taxi_sanfrancisco_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
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
							} else if (stop.getStopName().equals("bayshore_fwy")) {
								// '37.6835','-122.3895'
								lat = 37.6835;
								lon = -122.3895;
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
