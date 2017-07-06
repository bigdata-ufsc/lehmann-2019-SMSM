package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class FastCBSMoT_Hurricane12vs45Patel {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new EuclideanDistanceFunction());
		PatelProblem problem = new PatelProblem("hurricane_12vs45", "hurricane");
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_hurricane", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 3.9; // distance in meters to find neighbors
		int timeTolerance = 24; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 2; // distance in meters to merge stops
		int mergeTolerance = 27;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 20; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();
		
		ResultSet executeQuery = conn.createStatement().executeQuery("select max(stop_id), max(move_id) from stops_moves.patel_hurricane");
		executeQuery.next();
		MutableInt sid = new MutableInt(executeQuery.getInt(1));
		MutableInt mid = new MutableInt(executeQuery.getInt(2));
		PreparedStatement update = conn.prepareStatement("update patel.hurricane_12vs45 set semantic_stop_id = ?, semantic_move_id = ? where gid in (SELECT * FROM unnest(?))");
		PreparedStatement insert = conn.prepareStatement("insert into stops_moves.patel_hurricane(stop_id, start_time, start_lat, start_lon, end_time, end_lat, end_lon, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
			Map<String, Integer> bestCombinations = findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
			for (Map.Entry<String, Integer> e : bestCombinations.entrySet()) {
				if(e.getValue() > 400){
					System.out.println(e.getKey() + " ->" + e.getValue());
				}
			}
			StopAndMoveExtractor.persistStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insert);
		} finally {
			update.close();
			insert.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

	private static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, MutableInt sid, MutableInt mid) {
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
			int mergeTolerance, int minTime, MutableInt sid, MutableInt mid) {
		List<StopAndMove> ret = new ArrayList<>();
		while (!trajs.isEmpty()) {
			SemanticTrajectory t = trajs.remove(0);
			StopAndMove stopAndMove = fastCBSMoT.findStops(t, maxDist, minTime, timeTolerance, mergeTolerance, ratio, sid, mid);
			ret.add(stopAndMove);
		}
		return ret;
	}
}
