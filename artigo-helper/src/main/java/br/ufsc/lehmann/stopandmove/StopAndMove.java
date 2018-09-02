package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

	private Multimap<Stop, Long> stops = MultimapBuilder.linkedHashKeys().hashSetValues().build();
	private Map<Move, List<Long>> moves = new HashMap<>();
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
		s1.getPoints().addAll(s2.getPoints());

		Set<Move> removedMoves = moves.entrySet().parallelStream().filter((Map.Entry<Move, List<Long>> entry) -> {
			return entry.getKey().getStart() == s1 && entry.getKey().getEnd() == s2;
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();

		Set<Move> movesToUpdate = moves.entrySet().parallelStream().filter((Map.Entry<Move, List<Long>> entry) -> {
			return entry.getKey().getStart() == s2;
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
		removedMoves.forEach((Move m) -> {
			stops.putAll(s1, moves.remove(m));
			s1.getPoints().addAll(Arrays.asList(m.getPoints()));
		});
		movesToUpdate.forEach((Move m) -> {
			m.setStart(s1);
		});
		s1.setLength((s2.getBegin() + s2.getLength()) - s1.getBegin());
	}

	public Collection<Long> remove(Stop s, Stop previousStop, Stop nextStop, AtomicInteger mid) {
		Set<Move> movesToMerge = moves.entrySet().parallelStream().filter((Map.Entry<Move, List<Long>> entry) -> {
			return entry.getKey().getStart() == s || entry.getKey().getEnd() == s;
		}).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.counting())).keySet();
		if(movesToMerge.isEmpty()) {
			Collection<Long> stopPoints = stops.removeAll(s);
			Move move = new Move(mid.incrementAndGet(), previousStop, nextStop, s.getStartTime(), s.getEndTime(), s.getBegin(), stopPoints.size(), s.getPoints().toArray(new TPoint[s.getPoints().size()]));
//			move.setUser(s.getUser());
//			move.setDimensaoData(s.getDimensaoData());

			moves.computeIfAbsent(move, (l -> new ArrayList<>())).addAll(stopPoints);
			return stopPoints;
		}
		Collection<Long> stopPoints = stops.get(s);
		List<Long> mergedGids = new ArrayList<>(stopPoints);
		List<TPoint> mergedPoints = new ArrayList<>();
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
			mergedGids.addAll(moves.get(move));
			mergedPoints.addAll(Arrays.asList(move.getPoints()));
		}
		if(initial == null && end == null) {
			throw new RuntimeException("initial and end is null. trajId = " + this.trajectory.getTrajectoryId());
		}
		//remove o mapeamento antigo
		for (Move it : movesToMerge) {
			moves.remove(it);
		}
		int moveId = (initial == null ? end : initial).getMoveId();
		long startTime = initial == null ? (previousStop == null ? Semantic.TEMPORAL.getData(trajectory, trajectory.length() - 1).getEnd().toEpochMilli() : previousStop.getEndTime()) : initial.getStartTime();
		long endTime = end == null ? (nextStop == null ? Semantic.TEMPORAL.getData(trajectory, trajectory.length() - 1).getStart().toEpochMilli() : nextStop.getStartTime()) : end.getEndTime();
		Move move = new Move(moveId, previousStop, nextStop, startTime, endTime, initialIndex, endIndex - initialIndex, mergedPoints.toArray(new TPoint[mergedPoints.size()]));
//		move.setUser((initial == null ? end : initial).getUser());
//		move.setDimensaoData((initial == null ? end : initial).getDimensaoData());
		moves.computeIfAbsent(move, (l -> new ArrayList<>())).addAll(mergedGids);
		stops.removeAll(s);
		return stopPoints;
	}

	public void remove(Move move) {
		moves.remove(move);
	}

	public void addMove(Move move, Collection<Long> gids) {
		if(uncompletedMove != null) {
			ArrayList<Long> list = new ArrayList<>(gids);
			list.addAll(moves.remove(uncompletedMove));
			List<TPoint> newPoints = new ArrayList<>(Arrays.asList(uncompletedMove.getPoints()));
			newPoints.addAll(Arrays.asList(move.getPoints()));
			gids = list;
			move = new Move(uncompletedMove.getMoveId(), uncompletedMove.getStart(), move.getEnd(), uncompletedMove.getStartTime(), move.getEndTime(), uncompletedMove.getBegin(), uncompletedMove.getLength() + move.getLength(), newPoints.toArray(new TPoint[newPoints.size()]));
			move.setUser(uncompletedMove.getUser());
			move.setDimensaoData(uncompletedMove.getDimensaoData());
		}
		moves.computeIfAbsent(move, (l -> new ArrayList<>())).addAll(gids);
		this.uncompletedMove = move;
	}

	public void addStop(Stop s, Collection<Long> gids) {
		if(!stops.isEmpty() && uncompletedMove == null) {
			Stop lastStop = lastStop();
			Move ghostMove = new Move(-1, lastStop, s, lastStop.getEndTime(), s.getStartTime(), s.getBegin(), 0, new TPoint[0], 0.0, 0.0);
//			ghostMove.setUser(s.getUser());
//			ghostMove.setDimensaoData(s.getDimensaoData());
			addMove(ghostMove, Collections.emptyList());
		}
		if(uncompletedMove != null) {
			uncompletedMove.setEnd(s);
			uncompletedMove = null;
		}
		stops.putAll(s, gids);
	}

	public List<Long> getGids(Stop stop) {
		return new ArrayList<>(stops.get(stop));
	}

	public List<Long> getGids(Move move) {
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
