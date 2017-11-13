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
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.utils.LatLongDistanceFunction;

public class FastCBSMoT_SergipeTracks {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new LatLongDistanceFunction());
		SergipeTracksProblem problem = new SergipeTracksProblem(null);
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.sergipe_tracks_stop", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		//200-28000-200-15000-30000 -
		double ratio = 200; // distance in meters to find neighbors
		int timeTolerance = 28 * 1000; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 200; // distance in meters to merge stops
		int mergeTolerance = 15 * 1000;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 30 * 1000; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.sergipe_tracks_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.sergipe_tracks_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update sergipe.trackpoints set semantic_stop_id = ?, semantic_move_id = ? where track_id = ? and id in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.sergipe_tracks_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.sergipe_tracks_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
//			Map<String, Integer> bestCombinations = findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
//			for (Map.Entry<String, Integer> e : bestCombinations.entrySet()) {
//				if(e.getValue() > 400){
//					System.out.println(e.getKey() + " ->" + e.getValue());
//				}
//			}
			StopAndMoveExtractor.extractStopMove(fastCBSMoT, trajs, ratio, timeTolerance, maxDist, mergeTolerance, minTime, conn, sid, mid, update, insertStop, insertMove);
		} finally {
			update.close();
			insertStop.close();
			insertMove.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

	public static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, AtomicInteger sid, AtomicInteger mid) {
		Map<String, Integer> bestCombinations = new HashMap<>();
		for (int i = 40; i <= 200; i+=20) {//ratio
			final int finalI = i;
			IntStream.iterate(20 * 1000, j -> j + 2 * 1000).limit(10).parallel().forEach((j) -> {//timeTolerance
				final int finalJ = j;
				for (int k = 200; k <= 375; k+=25) {//maxDist
					final int finalK = k;
					IntStream.iterate(15 * 1000, l -> l + 1000).limit(10).parallel().forEach((l) -> {//mergeTolerance
						for (int m = 30 * 1000; m <= 90 * 1000; m+=1000) {//minTime
							List<StopAndMove> findBestCBSMoT = StopAndMoveExtractor.findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), finalI, finalJ, finalK, l, m, sid, mid);
							int stopsCount = 0;
							for (StopAndMove stopAndMove : findBestCBSMoT) {
								List<Stop> stops = stopAndMove.getStops();
								stopsCount += stops.size();
							}
							System.out.println(String.format("%d-%d-%d-%d-%d = %d", finalI, finalJ, finalK, l, m, stopsCount));
							bestCombinations.put(String.format("%d-%d-%d-%d-%d", finalI, finalJ, finalK, l, m), stopsCount);
						}
					});
				}
			});
		}
		return bestCombinations;
	}

}
