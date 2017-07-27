package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.TaxiShangaiProblem;

public class FastCBSMoT_TaxiShangai {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new EuclideanDistanceFunction());
		TaxiShangaiProblem problem = new TaxiShangaiProblem();
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.taxi_shangai_20070220", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 300; // distance in meters to find neighbors
		int timeTolerance = 28; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 350; // distance in meters to merge stops
		int mergeTolerance = 200;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 20; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet executeQuery = conn.createStatement().executeQuery("select max(stop_id), max(move_id) from stops_moves.taxi_shangai_20070220");
		executeQuery.next();
		AtomicInteger sid = new AtomicInteger(executeQuery.getInt(1));
		AtomicInteger mid = new AtomicInteger(executeQuery.getInt(2));
		PreparedStatement update = conn.prepareStatement("update taxi.shangai_20070220 set semantic_stop_id = ?, semantic_move_id = ? where gid in (SELECT * FROM unnest(?))");
		PreparedStatement insert = conn.prepareStatement("insert into stops_moves.taxi_shangai_20070220(stop_id, start_time, start_lat, start_lon, end_time, end_lat, end_lon, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
			Map<String, Integer> bestCombinations = StopAndMoveExtractor.findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
			int maxStops = 0;
			String bestConfiguration = null;
			for (Map.Entry<String, Integer> e : bestCombinations.entrySet()) {
				if(e.getValue() > maxStops) {
					maxStops = e.getValue();
					bestConfiguration = e.getKey();
				}
			}
			System.out.println(bestConfiguration + " ->" + bestCombinations.get(bestConfiguration));
//			StopAndMoveExtractor.persistStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insert);
		} finally {
			update.close();
			insert.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}
}
