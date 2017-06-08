package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.utils.Distance;

public class FastCBSMoT {
	
	private GeographicDistanceFunction distance;

	public FastCBSMoT(GeographicDistanceFunction distance) {
		this.distance = distance;
	}

	public StopAndMove findStops(SemanticTrajectory T, double maxDist, int minTime, int timeTolerance, int mergeTolerance, double ratio,
			MutableInt sid) {
		int size = T.length();
		int[] neighborhood = new int[size];
	
		for (int i = 0; i < neighborhood.length; i++) {
			neighborhood[i] = 0;
		}
	
		for (int i = 0; i < T.length(); i++) {
			int value = countNeighbors(i, T, ratio);
			neighborhood[i] = value;
			i += value;
		}
	
		StopAndMove ret = new StopAndMove(T);
		for (int i = 0; i < neighborhood.length; i++) {
			if (neighborhood[i] > 0) {
				Instant p1 = Semantic.TEMPORAL.getData(T, i).getStart();
				Instant p2 = Semantic.TEMPORAL.getData(T, i + neighborhood[i] - 1).getStart();
	
				long p2Milli = p2.toEpochMilli();
				long p1Milli = p1.toEpochMilli();
				if ((p2Milli - p1Milli) >= timeTolerance) {
					List<Integer> points = new ArrayList<>(neighborhood[i]);
					Stop s = new Stop(T, sid.getAndIncrement(), new Timestamp(p1Milli), new Timestamp(p2Milli));
					s.setCentroid(centroid(T, i, i + neighborhood[i] - 1));
	
					for (int x = 0; x < neighborhood[i]; x++) {
						TPoint p = Semantic.GEOGRAPHIC.getData(T, i + x);
						s.addPoint(p);
						points.add(Semantic.GID.getData(T, i + x).intValue());
					}
					ret.addStop(s, points);
				}
			}
		}
	
		ret = mergeStops(ret, maxDist, mergeTolerance);
		ret = cleanStops(ret, minTime);
		return ret;
	}
	StopAndMove cleanStops(StopAndMove stopAndMove, int minTime) {
		List<Stop> S = new ArrayList<>(stopAndMove.getStops());
		for (int i = 0; i < S.size(); i++) {
			Stop s = S.get(i);

			if ((s.getEndTime().getTime() - s.getStartTime().getTime()) < minTime) {
				Collection<Integer> oldStopPoints = stopAndMove.remove(s);
				stopAndMove.addMove(new Move(0, 0, 0, null, 0, null, 0.0, 0.0, 0, 0), oldStopPoints);
			}
		}
		return stopAndMove;
	}

	// TODO: adicionar atividades na lista
	StopAndMove mergeStops(StopAndMove stopAndMove, double maxDist, int timeTolerance) {
		List<Stop> S = new ArrayList<>(stopAndMove.getStops());
		for (int i = 0; i < S.size(); i++) {
			if (i + 1 != S.size()) {
				Stop s1 = S.get(i);
				Stop s2 = S.get(i + 1);
				if (s2.getStartTime().getTime() - s1.getEndTime().getTime() <= timeTolerance) {

					TPoint c1 = S.get(i).getCentroid();
					TPoint c2 = S.get(i + 1).getCentroid();

					if (Distance.euclidean(c1, c2) <= maxDist) {
						stopAndMove.mergeStops(s1, s2);
						
						S.remove(s2);
						i--;
					}
				}
			}
		}
		return stopAndMove;
	}

	TPoint centroid(SemanticTrajectory T, int start, int end) {
		double x = 0;
		double y = 0;

		int i = start;
		int total = 0;
		while (i >= start && i <= end) {
			total++;
			TPoint point = Semantic.GEOGRAPHIC.getData(T, i);
			x += point.getX();
			y += point.getY();
			i++;
		}

		TPoint p = new TPoint(0, x / total, y / total, new Timestamp(0));
		return p;
	}

	int countNeighbors(int i, SemanticTrajectory T, double maxDist) {
		int neighbors = 0;
		boolean yet = true;
		int j = i + 1;
		while (j < T.length() && yet) {
			TPoint p = Semantic.GEOGRAPHIC.getData(T, i);
			TPoint d = Semantic.GEOGRAPHIC.getData(T, j);
			if (distance.distanceInMeters(p, d) < maxDist) {
				neighbors++;
			} else {
				yet = false;
			}
			j++;
		}
		return neighbors;
	}
}
