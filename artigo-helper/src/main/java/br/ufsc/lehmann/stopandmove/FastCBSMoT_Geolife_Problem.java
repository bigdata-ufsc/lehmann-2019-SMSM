package br.ufsc.lehmann.stopandmove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.utils.LatLongDistanceFunction;

public class FastCBSMoT_Geolife_Problem {

	private static DataSource source;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		FastCBSMoT fastCBSMoT = new FastCBSMoT(new LatLongDistanceFunction());
		List<SemanticTrajectory> trajs = new Geolife2DatabaseReader().read();
		source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "public.geolife2_stop", null, "geom");

		// FIND STOPS
		double ratio = 100; // distance in meters to find neighbors
		int timeTolerance = 30 * 1000; // time in ms dif do ponto final para o inicial deve ser maior que timeTolerance

		// Merge - Será feito merge nos stops em que a distância dos centroid estiver há até maxDist de distância
		// e em que o tempo do ponto inicial do primeiro stop e do ponto final do segundo stop
		// seja menor ou igual a mergeTolerance
		double maxDist = 200; // distance in meters to merge stops
		int mergeTolerance = 10 * 60 * 1000;// time in ms

		// Clean - Os stops devem ter pelo menos o minTime
		int minTime = 5 * 60 * 1000; // time in ms

		// System.out.println(T.size());
		long start = System.currentTimeMillis();
		Connection conn = source.getRetriever().getConnection();

		ResultSet lastStop = conn.createStatement().executeQuery("select max(stop_id) from public.geolife2_stop");
		lastStop.next();
		AtomicInteger sid = new AtomicInteger(lastStop.getInt(1));
		ResultSet lastMove = conn.createStatement().executeQuery("select max(move_id) from public.geolife2_move");
		lastMove.next();
		AtomicInteger mid = new AtomicInteger(lastMove.getInt(1));
		PreparedStatement insertStop = conn.prepareStatement("insert into public.geolife2_stop(stop_id, start_time, end_time, start_lat, start_lon, end_lat, end_lon, begin, length, centroid_lat, centroid_lon) values (?,?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement insertMove = conn.prepareStatement("insert into public.geolife2_move(move_id, start_time, end_time, start_stop_id, end_stop_id) values (?,?,?,?,?)");
		PreparedStatement insertMapping = conn.prepareStatement("insert into public.geolife2_stops_moves(tid, gps_point_id, is_stop, is_move, semantic_id) values (?,?,?,?,?)");
		try {
			conn.setAutoCommit(false);

			List<StopAndMove> findBestCBSMoT = StopAndMoveExtractor.findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), ratio, timeTolerance, maxDist, mergeTolerance, minTime, sid, mid);
			int stopsCount = 0;
			for (StopAndMove stopAndMove : findBestCBSMoT) {
				List<Stop> stops = stopAndMove.getStops();
				stopsCount += stops.size();
				for (Stop stop : stops) {
					int stopId = stop.getStopId();
					List<TPoint> gpsPoints = stop.getPoints();
					for (TPoint tPoint : gpsPoints) {
						insertMapping.setLong(1, ((Number) stopAndMove.getTrajectory().getTrajectoryId()).longValue());
						insertMapping.setLong(2, tPoint.getGid());
						insertMapping.setBoolean(3, true);
						insertMapping.setBoolean(4, false);
						insertMapping.setLong(5, stopId);
						insertMapping.addBatch();
					}
					insertMapping.executeBatch();

					TPoint meanPoint = stop.medianPoint();
					insertStop.setInt(1, stopId);
					insertStop.setTimestamp(2, Timestamp.from(Instant.ofEpochMilli(stop.getStartTime())));
					insertStop.setTimestamp(3, Timestamp.from(Instant.ofEpochMilli(stop.getEndTime())));

					List<TPoint> points = new ArrayList<>(stop.getPoints());
					insertStop.setDouble(4, points.get(0).getX());
					insertStop.setDouble(5, points.get(0).getY());
					insertStop.setDouble(6, points.get(points.size() - 1).getX());
					insertStop.setDouble(7, points.get(points.size() - 1).getY());
					insertStop.setInt(8, stop.getBegin());
					insertStop.setInt(9, stop.getLength());
					
					insertStop.setDouble(10, meanPoint.getX());
					insertStop.setDouble(11, meanPoint.getY());
					insertStop.execute();
				}
				List<Move> moves = stopAndMove.getMoves();
				for (Move move : moves) {
					Stop startStop = move.getStart();
					Stop endStop = move.getEnd();
					if(startStop == null || endStop == null) {
						continue;
					}
					TPoint[] gpsPoints = move.getPoints();
					int moveId = mid.incrementAndGet();
					if(gpsPoints != null && gpsPoints.length > 0) {
						for (TPoint tPoint : gpsPoints) {
							insertMapping.setLong(1, ((Number) stopAndMove.getTrajectory().getTrajectoryId()).longValue());
							insertMapping.setLong(2, tPoint.getGid());
							insertMapping.setBoolean(3, false);
							insertMapping.setBoolean(4, true);
							insertMapping.setLong(5, moveId);
							insertMapping.addBatch();
						}
						insertMapping.executeBatch();
					}
					insertMove.setInt(1, moveId);
					insertMove.setTimestamp(2, Timestamp.from(Instant.ofEpochMilli(startStop.getEndTime())));
					insertMove.setTimestamp(3, Timestamp.from(Instant.ofEpochMilli(endStop.getStartTime())));
					insertMove.setInt(4, startStop.getStopId());
					insertMove.setInt(5, endStop.getStopId());
					insertMove.execute();
				}
				conn.commit();
			}
			System.out.println("Stop count: " + stopsCount);
		} finally {
			insertStop.close();
			insertMove.close();
			insertMapping.close();
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
