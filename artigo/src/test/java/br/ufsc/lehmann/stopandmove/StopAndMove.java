package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multiset;

import br.ufsc.core.trajectory.Semantic;
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
			stops.putAll(s1, moves.removeAll(m));
		});
		s1.setLength((s2.getBegin() + s2.getLength()) - s1.getBegin());
	}

	public Collection<Integer> remove(Stop s, Stop previousStop, Stop nextStop, AtomicInteger mid) {
		Set<Move> movesToMerge = moves.asMap().entrySet().parallelStream().filter((Map.Entry<Move, Collection<Integer>> entry) -> {
			return entry.getKey().getStart() == s || entry.getKey().getEnd() == s;
		}).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.counting())).keySet();
		if(movesToMerge.isEmpty()) {
			Collection<Integer> stopPoints = stops.removeAll(s);
			Move move = new Move(mid.incrementAndGet(), previousStop, nextStop, s.getStartTime(), s.getEndTime(), s.getBegin(), stopPoints.size(), null);
			moves.putAll(move, stopPoints);
			return stopPoints;
		}
		Collection<Integer> stopPoints = stops.get(s);
		List<Integer> mergedPoints = new ArrayList<>(stopPoints);
		Move initial = null, end = null;
		Integer initialIndex = previousStop == null ? 0 : previousStop.getBegin() + previousStop.getLength() + 1, endIndex = nextStop == null ? this.trajectory.length() - 1 : nextStop.getBegin();
		for (Move move : movesToMerge) {
			if(initialIndex >= move.getBegin()) {
				initialIndex = move.getBegin();
				initial = move;
			}
			if(endIndex <= move.getBegin() + move.getLength()) {
				endIndex = move.getBegin() + move.getLength();
				end = move;
			}
			mergedPoints.addAll(moves.get(move));
		}
		if(initial == null && end == null) {
			throw new RuntimeException("initial and end is null. trajId = " + this.trajectory.getTrajectoryId());
		}
		//remove o mapeamento antigo
		for (Move it : movesToMerge) {
			moves.removeAll(it);
		}
		int moveId = (initial == null ? end : initial).getMoveId();
		double startTime = initial == null ? (previousStop == null ? Semantic.TEMPORAL.getData(trajectory, trajectory.length() - 1).getEnd().toEpochMilli() : previousStop.getEndTime()) : initial.getStartTime();
		double endTime = end == null ? (nextStop == null ? Semantic.TEMPORAL.getData(trajectory, trajectory.length() - 1).getStart().toEpochMilli() : nextStop.getStartTime()) : end.getEndTime();
		Move move = new Move(moveId, previousStop, nextStop, startTime, endTime, initialIndex, endIndex - initialIndex, null);
		moves.putAll(move, mergedPoints);
		stops.removeAll(s);
		return stopPoints;
	}

	public void addMove(Move move, Collection<Integer> gids) {
		if(uncompletedMove != null) {
			ArrayList<Integer> list = new ArrayList<>(gids);
			list.addAll(moves.removeAll(uncompletedMove));
			gids = list;
			move = new Move(uncompletedMove.getMoveId(), uncompletedMove.getStart(), move.getEnd(), uncompletedMove.getStartTime(), move.getEndTime(), uncompletedMove.getBegin(), uncompletedMove.getLength() + move.getLength(), null);
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
