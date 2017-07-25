package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;

public class FastCBSMoT_NewYorkBus {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new LatLongDistanceFunction());
		NewYorkBusProblem problem = new NewYorkBusProblem();
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.bus_nyc_20140927_stop", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 100; // distance in meters to find neighbors
		int timeTolerance = 60 * 1000; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 300; // distance in meters to merge stops
		int mergeTolerance = 2 * 60 * 1000 + 1;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 60 * 1000; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.bus_nyc_20140927_stop");
		lastStop.next();
		MutableInt sid = new MutableInt(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.bus_nyc_20140927_move");
		lastMove.next();
		MutableInt mid = new MutableInt(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update bus.nyc_20140927 set semantic_stop_id = ?, semantic_move_id = ? where trim(infered_trip_id) = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.bus_nyc_20140927_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.bus_nyc_20140927_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
//			Map<String, Integer> bestCombinations = StopAndMoveExtractor.findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
//			for (Map.Entry<String, Integer> e : bestCombinations.entrySet()) {
//				if(e.getValue() > 400){
//					System.out.println(e.getKey() + " ->" + e.getValue());
//				}
//			}
			StopAndMoveExtractor.persistStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insertStop, insertMove);
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
