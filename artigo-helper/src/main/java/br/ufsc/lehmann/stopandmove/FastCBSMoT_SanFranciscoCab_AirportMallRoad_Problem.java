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
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCab_AirportMallRoad_Problem;
import br.ufsc.utils.LatLongDistanceFunction;

public class FastCBSMoT_SanFranciscoCab_AirportMallRoad_Problem {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new LatLongDistanceFunction());
		SanFranciscoCab_AirportMallRoad_Problem problem = new SanFranciscoCab_AirportMallRoad_Problem(new String[] {"101", "280"});
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.taxi_sanfrancisco_stop", null, "geom");

		// Trajectory t = retriever.fastFetchTrajectory(9543);
		// FIND STOPS
		double ratio = 600; // distance in meters to find neighbors
		int timeTolerance = 30 * 1000; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 500; // distance in meters to merge stops
		int mergeTolerance = 55 * 1000;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 30 * 1000; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.taxi_sanfrancisco_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from stops_moves.taxi_sanfrancisco_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement update = conn.prepareStatement("update taxi.sanfrancisco_taxicab_crawdad set semantic_stop_id = ?, semantic_move_id = ? where tid = ? and gid in (SELECT * FROM unnest(?))");
		PreparedStatement insertStop = conn.prepareStatement("insert into stops_moves.taxi_sanfrancisco_stop(stop_id, start_time, start_lat, start_lon, begin, end_time, end_lat, end_lon, length, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into stops_moves.taxi_sanfrancisco_move(move_id, start_time, start_stop_id, begin, end_time, end_stop_id, length) values (?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
//			Map<String, Integer> bestCombinations = findBestCBSMoT(fastCBSMoT, trajs, sid, mid);
//			ArrayList<Integer> t = new ArrayList<>(bestCombinations.values());
//			Collections.sort(t);
//			System.out.println(t.get(t.size() - 1));
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

	public static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, AtomicInteger sid, AtomicInteger mid) {
		Map<String, Integer> bestCombinations = new HashMap<>();
		for (int i = 300; i <= 600; i+=50) {//ratio
			final int finalI = i;
			IntStream.iterate(20 * 1000, j -> j + 5 * 1000).limit(10).parallel().forEach((j) -> {//timeTolerance
				final int finalJ = j;
				for (int k = 500; k <= 800; k+=50) {//maxDist
					final int finalK = k;
					IntStream.iterate(20*1000, l -> l + 5 * 1000).limit(10).parallel().forEach((l) -> {//mergeTolerance
						for (int m = 30 * 1000; m <= 120 * 1000; m+=10000) {//minTime
							List<StopAndMove> findBestCBSMoT = StopAndMoveExtractor.findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), finalI, finalJ, finalK, l, m, sid, mid);
							int stopsCount = 0;
							for (StopAndMove stopAndMove : findBestCBSMoT) {
								List<Stop> stops = stopAndMove.getStops();
								stopsCount += stops.size();
							}
							if(stopsCount > trajs.size()) {
								System.out.println(String.format("%d-%d-%d-%d-%d", finalI, finalJ, finalK, l, m) + "->" + stopsCount);
							}
							bestCombinations.put(String.format("%d-%d-%d-%d-%d", finalI, finalJ, finalK, l, m), stopsCount);
						}
					});
				}
			});
		}
		return bestCombinations;
	}

}
