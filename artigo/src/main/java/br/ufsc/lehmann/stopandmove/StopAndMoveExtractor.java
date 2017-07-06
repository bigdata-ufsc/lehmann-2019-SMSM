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
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;

public class StopAndMoveExtractor {

	public static void persistStopMove(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, double ratio, int timeTolerance, double maxDist,
			int mergeTolerance, int minTime, Connection conn, MutableInt sid, MutableInt mid, PreparedStatement update, PreparedStatement insert)
			throws SQLException {
		List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), ratio, timeTolerance, maxDist, mergeTolerance, minTime, sid, mid);
		for (StopAndMove stopAndMove : findBestCBSMoT) {
			List<Stop> stops = stopAndMove.getStops();
			List<Move> moves = stopAndMove.getMoves();
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
				insert.setNull(2, Types.INTEGER);
				insert.setTimestamp(3, new Timestamp(stop.getStartTime()));
				insert.setDouble(4, points.get(0).getX());
				insert.setDouble(5, points.get(0).getY());
				insert.setTimestamp(6, new Timestamp(stop.getEndTime()));
				insert.setDouble(7, points.get(points.size() - 1).getX());
				insert.setDouble(8, points.get(points.size() - 1).getY());
				insert.setDouble(9, stop.getCentroid().getX());
				insert.setDouble(10, stop.getCentroid().getY());
				insert.addBatch();
			}
			System.out.println("Traj.: " + PatelDataReader.TID.getData(stopAndMove.getTrajectory(), 0) + ", moves: " + moves.size());
			for (Move move : moves) {
				System.out.println("From " + move.getStartTime() + " to " + move.getEndTime());
				List<Integer> gids = stopAndMove.getGids(move);
				Array array = conn.createArrayOf("integer", gids.toArray(new Integer[gids.size()]));
				update.setNull(1, Types.NUMERIC);
				update.setInt(2, move.getMoveId());
				update.setArray(3, array);
				update.addBatch();
				
				TPoint initialPoint = Semantic.GEOGRAPHIC.getData(move.getT(), move.getBegin());
				TPoint endPoint = Semantic.GEOGRAPHIC.getData(move.getT(), move.getBegin() + move.getLength());
				insert.setNull(1, Types.INTEGER);
				insert.setInt(2, move.getMoveId());
				insert.setTimestamp(3, new Timestamp((long) move.getStartTime()));
				insert.setDouble(4, initialPoint.getX());
				insert.setDouble(5, initialPoint.getY());
				insert.setTimestamp(6, new Timestamp((long) move.getEndTime()));
				insert.setDouble(7, endPoint.getX());
				insert.setDouble(8, endPoint.getY());
				insert.setNull(9, Types.DOUBLE);
				insert.setNull(10, Types.DOUBLE);
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
	}

	public static Map<String, Integer> findBestCBSMoT(FastCBSMoT fastCBSMoT, List<SemanticTrajectory> trajs, MutableInt sid, MutableInt mid) {
		Map<String, Integer> bestCombinations = new HashMap<>();
		for (int i = 40; i <= 200; i+=20) {//ratio
			final int finalI = i;
			IntStream.iterate(20 * 1000, j -> j + 2 * 1000).limit(10).parallel().forEach((j) -> {//mergeTolerance
				final int finalJ = j;
				for (int k = 475; k <= 475; k+=25) {//maxDist
					final int finalK = k;
					IntStream.iterate(325, l -> l + 25).limit(1).parallel().forEach((l) -> {//mergeTolerance
						for (int m = 10 * 1000; m <= 20 * 1000; m+=2) {//minTime
							List<StopAndMove> findBestCBSMoT = findCBSMoT(fastCBSMoT, new ArrayList<>(trajs), finalI, finalJ, finalK, l, m, sid, mid);
							int stopsCount = 0;
							for (StopAndMove stopAndMove : findBestCBSMoT) {
								List<Stop> stops = stopAndMove.getStops();
								stopsCount += stops.size();
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
