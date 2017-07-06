package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multiset;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;

public class StopAndMove {

	private Multimap<Stop, Integer> stops = MultimapBuilder.linkedHashKeys().hashSetValues().build();
	private Multimap<Move, Integer> moves = MultimapBuilder.linkedHashKeys().hashSetValues().build();
	private SemanticTrajectory trajectory;
	private Move uncompletedMove;

	public StopAndMove(SemanticTrajectory trajectory) {
		this.trajectory = trajectory;
	}

	public List<Stop> getStops() {
		return new ArrayList<>(stops.keySet());
	}

	public List<Move> getMoves() {
		return new ArrayList<>(moves.keySet());
	}

	public void mergeStops(Stop s1, Stop s2) {
		s1.setEndTime(s2.getEndTime());
		double cx = (s1.getCentroid().getX() + s2.getCentroid().getX()) / 2;
		double cy = (s1.getCentroid().getY() + s2.getCentroid().getY()) / 2;

		TPoint c = new TPoint(0, cx, cy, new Timestamp(0));
		s1.setCentroid(c);
		
		stops.putAll(s1, stops.removeAll(s2));

		Set<Move> removedMoves = moves.asMap().entrySet().parallelStream().filter((Map.Entry<Move, Collection<Integer>> entry) -> {
			return entry.getKey().getStart() == s1 && entry.getKey().getEnd() == s2;
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
		removedMoves.forEach((Move m) -> {
			moves.removeAll(m);
		});
	}

	public Collection<Integer> remove(Stop s) {
		Set<Move> movesToMerge = moves.asMap().entrySet().parallelStream().filter((Map.Entry<Move, Collection<Integer>> entry) -> {
			return entry.getKey().getStart() == s || entry.getKey().getEnd() == s;
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
		if(movesToMerge.isEmpty()) {
			throw new IllegalStateException("No moves around stop");
		}
		Collection<Integer> stopPoints = stops.removeAll(s);
		List<Integer> mergedPoints = new ArrayList<>(stopPoints);
		Move initial = null, end = null;
		Integer initialIndex = Integer.MAX_VALUE, endIndex = -1;
		for (Move move : movesToMerge) {
			if(initialIndex > move.getBegin()) {
				initialIndex = move.getBegin();
				initial = move;
			}
			if(endIndex < move.getBegin() + move.getLength()) {
				endIndex = move.getBegin() + move.getLength();
				end = move;
			}
			mergedPoints.addAll(moves.removeAll(move));
		}
		Move move = new Move(trajectory, initial.getMoveId(), initial.getStart(), end.getEnd(), initial.getStartTime(), end.getEndTime(), initialIndex, endIndex - initialIndex);
		moves.putAll(move, mergedPoints);
		return stopPoints;
	}

	public void addMove(Move move, Collection<Integer> gids) {
		if(uncompletedMove != null) {
			ArrayList<Integer> list = new ArrayList<>(gids);
			list.addAll(moves.removeAll(uncompletedMove));
			gids = list;
			move = new Move(trajectory, uncompletedMove.getMoveId(), uncompletedMove.getStart(), move.getEnd(), uncompletedMove.getStartTime(), move.getEndTime(), uncompletedMove.getBegin(), uncompletedMove.getLength() + move.getLength());
		}
		moves.putAll(move, gids);
		this.uncompletedMove = move;
	}

	public void addStop(Stop s, Collection<Integer> gids) {
		stops.putAll(s, gids);
		if(uncompletedMove != null) {
			uncompletedMove.setEnd(s);
			uncompletedMove = null;
		}
	}

	public List<Integer> getGids(Stop stop) {
		return new ArrayList<>(stops.get(stop));
	}

	public List<Integer> getGids(Move move) {
		return new ArrayList<>(moves.get(move));
	}

	public SemanticTrajectory getTrajectory() {
		return trajectory;
	}

	public Stop lastStop() {
		if(stops.isEmpty()) {
			return null;
		}
		Multiset<Stop> keys = stops.keys();
		return keys.stream().skip(keys.size() - 1).findFirst().get();
	}
}
