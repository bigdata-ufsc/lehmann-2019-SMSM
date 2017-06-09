package br.ufsc.lehmann.stopandmove;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class FastCBSMoT_BusesPatel {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new EuclideanDistanceFunction());
		PatelProblem problem = new PatelProblem("buses");
		List<SemanticTrajectory> trajs = problem.data();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "stops_moves.patel_vehicles", null, "geom");

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
		
		ResultSet executeQuery = conn.createStatement().executeQuery("select max(stop_id) from stops_moves.patel_vehicles");
		executeQuery.next();
		MutableInt sid = new MutableInt(executeQuery.getInt(1));
		PreparedStatement update = conn.prepareStatement("update patel.buses set semantic_stop_id = ?, semantic_move_id = ? where gid in (SELECT * FROM unnest(?))");
		PreparedStatement insert = conn.prepareStatement("insert into stops_moves.patel_vehicles(stop_id, start_time, start_lat, start_lon, end_time, end_lat, end_lon, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);
//			Map<String, Integer> bestCombinations = findBestCBSMoT(fastCBSMoT, trajs, sid);
//			Optional<Map.Entry<String, Integer>> bestCombination = bestCombinations.entrySet().stream().sorted((c, c2) -> c2.getValue().compareTo(c.getValue())).findFirst();
//			System.out.println(bestCombination.get().getKey() + " ->" + bestCombination.get().getValue());
			List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), ratio, timeTolerance, maxDist, mergeTolerance, minTime, sid);
			for (StopAndMove stopAndMove : findBestCBSMoT) {
				List<Stop> stops = stopAndMove.getStops();
				System.out.println("Traj.: " + PatelDataReader.TID.getData(stopAndMove.getTrajectory(), 0) + ", stops: " + stops.size());
				for (Stop stop : stops) {
					System.out.println("From " + stop.getStartTime() + " to " + stop.getEndTime());
					List<Integer> gids = stopAndMove.getGids(stop);
					Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
					update.setInt(1, stop.getStopId());
					update.setNull(2, Types.NUMERIC);
					update.setArray(3, array);
					update.addBatch();
					
					List<TPoint> points = new ArrayList<>(stop.getPoints());
					insert.setInt(1, stop.getStopId());
					insert.setTimestamp(2, stop.getStartTime());
					insert.setDouble(3, points.get(0).getX());
					insert.setDouble(4, points.get(0).getY());
					insert.setTimestamp(5, stop.getEndTime());
					insert.setDouble(6, points.get(points.size() - 1).getX());
					insert.setDouble(7, points.get(points.size() - 1).getY());
					insert.setDouble(8, stop.getCentroid().getX());
					insert.setDouble(9, stop.getCentroid().getY());
					insert.addBatch();
				}
				if(sid.getValue() % 10 == 0) {
					update.executeBatch();
					insert.executeBatch();
					conn.commit();
				}
			}
			update.executeBatch();
			insert.executeBatch();
			conn.commit();
		} finally {
			update.close();
			insert.close();
			conn.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

	private static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, MutableInt sid) {
		Map<String, Integer> bestCombinations = new ConcurrentHashMap<>();
		IntStream.iterate(20, i -> i + 4).limit(5).parallel().forEach((j) -> {//timeTolerance
			for (int i = 100; i <= 300; i+=20) {//ratio
				for (int k = 350; k <= 600; k+=25) {//maxDist
					for (int l = 200; l <= 500; l+=25) {//mergeTolerance
						for (int m = 20; m <= 40; m+=4) {//minTime
							List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), i, j, k, l, m, sid);
							int stopsCount = 0;
							for (StopAndMove stopAndMove : findBestCBSMoT) {
								List<Stop> stops = stopAndMove.getStops();
								stopsCount += stops.size();
							}
							bestCombinations.put(String.format("%d-%d-%d-%d-%d", i, j, k, l, m), stopsCount);
						}
					}
				}
			}
		});
		return bestCombinations;
	}

	private static List<StopAndMove> findCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, double ratio, int timeTolerance, double maxDist,
			int mergeTolerance, int minTime, MutableInt sid) {
		List<StopAndMove> ret = new ArrayList<>();
		while (!trajs.isEmpty()) {
			SemanticTrajectory t = trajs.remove(0);
			StopAndMove stopAndMove = fastCBSMoT.findStops(t, maxDist, minTime, timeTolerance, mergeTolerance, ratio, sid);
			ret.add(stopAndMove);
		}
		return ret;
	}

}
