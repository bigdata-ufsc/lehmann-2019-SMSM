package br.ufsc.lehmann.stopandmove;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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

public class StopAndMoveExtractor {

	public static void extractStopMove(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, double ratio, int timeTolerance, double maxDist,
			int mergeTolerance, int minTime, Connection conn, AtomicInteger sid, AtomicInteger mid, PreparedStatement update, PreparedStatement insertStop, PreparedStatement insertMove)
			throws SQLException {
		List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), ratio, timeTolerance, maxDist, mergeTolerance, minTime, sid, mid);
//		for (StopAndMove stopAndMove : findBestCBSMoT) {
//			int moveAndStopPoints = 0;
//			List<Stop> stops = stopAndMove.getStops();
//			for (Stop stop : stops) {
//				List<Integer> gids = stopAndMove.getGids(stop);
//				moveAndStopPoints += gids.size();
//			}
//			List<Move> moves = stopAndMove.getMoves();
//			for (Move move : moves) {
//				if(move.getBegin() > stopAndMove.getTrajectory().length()) {
//					System.err.println("Traj.: " + stopAndMove.getTrajectory().getTrajectoryId());
//				}
//				List<Integer> gids = stopAndMove.getGids(move);
//				moveAndStopPoints += gids.size();
//			}
//			if(stopAndMove.getTrajectory().length() != moveAndStopPoints) {
//				System.out.println("Traj.: " + stopAndMove.getTrajectory().getTrajectoryId() + ", length: " + stopAndMove.getTrajectory().length() + ", stops and moves: " + moveAndStopPoints);
//			}
//			System.out.println("Traj.: " + stopAndMove.getTrajectory().getTrajectoryId() + ", stops: " + stops.size());
//			System.out.println("Traj.: " + stopAndMove.getTrajectory().getTrajectoryId() + ", moves: " + moves.size());
//		}
//		if(true) {
//			throw new RuntimeException();
//		}
		persistStopAndMove(conn, update, insertStop, insertMove, findBestCBSMoT);
	}

	public static void persistStopAndMove(Connection conn, PreparedStatement update, PreparedStatement insertStop,
			PreparedStatement insertMove, List<StopAndMove> findBestCBSMoT) throws SQLException {
		persistStopAndMove(conn, update, insertStop, null, insertMove, findBestCBSMoT);
	}

	public static void persistStopAndMove(Connection conn, PreparedStatement update, PreparedStatement insertStop, StopPersisterCallback stopCallback,
			PreparedStatement insertMove, List<StopAndMove> findBestCBSMoT) throws SQLException {
		int registers = 0;
		for (StopAndMove stopAndMove : findBestCBSMoT) {
			List<Stop> stops = stopAndMove.getStops();
			List<Move> moves = stopAndMove.getMoves();
			System.out.println("Traj.: " + stopAndMove.getTrajectory().getTrajectoryId() + ", stops: " + stops.size());
			for (Stop stop : stops) {
				registers++;
//				System.out.println("From " + stop.getStartTime() + " to " + stop.getEndTime());
				List<Integer> gids = stopAndMove.getGids(stop);
				Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
				update.setInt(1, stop.getStopId());
				update.setNull(2, Types.NUMERIC);
				update.setObject(3, stopAndMove.getTrajectory().getTrajectoryId());
				update.setArray(4, array);
				update.addBatch();
				
				List<TPoint> points = new ArrayList<>(stop.getPoints());
				insertStop.setInt(1, stop.getStopId());
				insertStop.setTimestamp(2, new Timestamp(stop.getStartTime()));
				insertStop.setDouble(3, points.get(0).getX());
				insertStop.setDouble(4, points.get(0).getY());
				insertStop.setInt(5, stop.getBegin());
				insertStop.setTimestamp(6, new Timestamp(stop.getEndTime()));
				insertStop.setDouble(7, points.get(points.size() - 1).getX());
				insertStop.setDouble(8, points.get(points.size() - 1).getY());
				insertStop.setInt(9, stop.getLength());
				insertStop.setDouble(10, stop.getCentroid().getX());
				insertStop.setDouble(11, stop.getCentroid().getY());
				
				stopCallback.parameterize(insertStop, stop);

				insertStop.addBatch();
				if(registers % 100 == 0) {
					try {
						update.executeBatch();
						insertStop.executeBatch();
						insertMove.executeBatch();
					} catch (SQLException e) {
						while(e.getNextException() != null) {
							e = e.getNextException();
						}
						throw e;
					}
					conn.commit();
				}
			}
			System.out.println("Traj.: " + stopAndMove.getTrajectory().getTrajectoryId() + ", moves: " + moves.size());
			for (Move move : moves) {
				registers++;
//				System.out.println("From " + move.getStartTime() + " to " + move.getEndTime());
				List<Integer> gids = stopAndMove.getGids(move);
				Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
				update.setNull(1, Types.NUMERIC);
				update.setInt(2, move.getMoveId());
				update.setObject(3, stopAndMove.getTrajectory().getTrajectoryId());
				update.setArray(4, array);
				update.addBatch();
				
				insertMove.setInt(1, move.getMoveId());
				insertMove.setTimestamp(2, new Timestamp((long) move.getStartTime()));
				if(move.getStart() != null) {
					insertMove.setInt(3, move.getStart().getStopId());
				} else {
					insertMove.setNull(3, Types.INTEGER);
				}
				insertMove.setDouble(4, move.getBegin());
				insertMove.setTimestamp(5, new Timestamp((long) move.getEndTime()));
				if(move.getEnd() != null) {
					insertMove.setInt(6, move.getEnd().getStopId());
				} else {
					insertMove.setNull(6, Types.INTEGER);
				}
				insertMove.setInt(7, move.getLength());
				insertMove.addBatch();
				if(registers % 100 == 0) {
					try {
						update.executeBatch();
						insertStop.executeBatch();
						insertMove.executeBatch();
					} catch (SQLException e) {
						throw e.getNextException();
					}
					conn.commit();
				}
			}
		}
		update.executeBatch();
		insertStop.executeBatch();
		insertMove.executeBatch();
		conn.commit();
	}

	public static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, AtomicInteger sid, AtomicInteger mid) {
		Map<String, Integer> bestCombinations = new HashMap<>();
		for (int i = 200; i > 40; i-=25) {//ratio
			final int finalI = i;
			IntStream.iterate(20 * 1000, j -> j + 2 * 1000).limit(10).parallel().forEach((j) -> {//timeTolerance
				final int finalJ = j;
				for (int k = 300; k <= 475; k+=25) {//maxDist
					final int finalK = k;
					IntStream.iterate(30 * 1000, l -> l + 2000).limit(15).parallel().forEach((l) -> {//mergeTolerance
						for (int m = 30 * 1000; m <= 90 * 1000; m+=1000) {//minTime
							List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), finalI, finalJ, finalK, l, m, sid, mid);
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

	public static List<StopAndMove> findCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, double ratio, int timeTolerance, double maxDist,
			int mergeTolerance, int minTime, AtomicInteger sid, AtomicInteger mid) {
		List<StopAndMove> ret = new ArrayList<>();
		while (!trajs.isEmpty()) {
			SemanticTrajectory t = trajs.remove(0);
			StopAndMove stopAndMove = fastCBSMoT.findStops(t, maxDist, minTime, timeTolerance, mergeTolerance, ratio, sid, mid);
			ret.add(stopAndMove);
		}
		return ret;
	}

	public static interface StopPersisterCallback {
		void parameterize(PreparedStatement statement, Stop stop) throws SQLException;
	}
	static StopPersisterCallback EMPTY = new StopPersisterCallback() {

		@Override
		public void parameterize(PreparedStatement statement, Stop stop) {
		}
		
	};
}
