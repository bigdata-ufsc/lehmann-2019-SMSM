package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class FastCBSMoT_Hurricane2vs3Patel {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new EuclideanDistanceFunction());
		PatelProblem problem = new PatelProblem("hurricane_2vs3", "hurricane");
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_hurricane", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 4; // distance in meters to find neighbors
		int timeTolerance = 21; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 2.2; // distance in meters to merge stops
		int mergeTolerance = 34;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 20; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.patel_hurricane_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.patel_hurricane_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update patel.hurricane_2vs3 set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.patel_hurricane_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.patel_hurricane_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
//			Map<String, Integer> bestCombinations = findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
//			for (Map.Entry<String, Integer> e : bestCombinations.entrySet()) {
//				if(e.getValue() > 400){
//					System.out.println(e.getKey() + " ->" + e.getValue());
//				}
//			}
			StopAndMoveExtractor.persistStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insertStop, insertMove);
		} finally {
			update.close();
			insertMove.close();
			insertStop.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

	private static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, AtomicInteger sid, AtomicInteger mid) {
		Map<String, Integer> bestCombinations = new HashMap<>();
		for (double i = 0.5; i <= 4.0; i += 0.2) {//ratio
			final double finalI = i;
			for (int j = 20; j <= 35; j+=1) {//timeTolerance
				final int finalJ = j;
				for (double k = 2.0; k <= 10.0; k += 0.2) {//maxDist
					final double finalK = k;
					IntStream.iterate(20, l -> l + 1).limit(15).parallel().forEach((l) -> {//mergeTolerance
						for (int m = 20; m <= 60; m+=5) {//minTime
							List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), finalI, finalJ, finalK, l, m, sid, mid);
							int stopsCount = 0;
							for (StopAndMove stopAndMove : findBestCBSMoT) {
								List<Stop> stops = stopAndMove.getStops();
								stopsCount += stops.size();
							}
							bestCombinations.put(String.format("%.2f-%d-%.2f-%d-%d", finalI, finalJ, finalK, l, m), stopsCount);
						}
					});
				}
			}
		}
		return bestCombinations;
	}

	private static List<StopAndMove> findCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, double ratio, int timeTolerance, double maxDist,
			int mergeTolerance, int minTime, AtomicInteger sid, AtomicInteger mid) {
		List<StopAndMove> ret = new ArrayList<>();
		while (!trajs.isEmpty()) {
			SemanticTrajectory t = trajs.remove(0);
			StopAndMove stopAndMove = fastCBSMoT.findStops(t, maxDist, minTime, timeTolerance, mergeTolerance, ratio, sid, mid);
			ret.add(stopAndMove);
		}
		return ret;
	}
}
