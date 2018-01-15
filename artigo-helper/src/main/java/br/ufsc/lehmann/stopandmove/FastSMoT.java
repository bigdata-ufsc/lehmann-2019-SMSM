package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;

public class FastSMoT<E, T> {
	
	private Semantic<E, T> segmentationSemantic;
	private StopDetector<E> detector;
	private Semantic stopNameSemantic;
	private long stopTime;
	
	public FastSMoT(Semantic<E, T> segmentationSemantic) {
		this(segmentationSemantic, 0);
	}

	public FastSMoT(Semantic<E, T> segmentationSemantic, long stopTime) {
		this(segmentationSemantic, segmentationSemantic, stopTime);
	}

	public FastSMoT(Semantic<E, T> segmentationSemantic, Semantic stopNameSemantic, long stopTime) {
		this(segmentationSemantic, stopNameSemantic, (StopDetector<E>) NOT_NULL_DETECTOR, stopTime);
	}

	public FastSMoT(Semantic<E, T> segmentationSemantic, Semantic stopNameSemantic, StopDetector<E> detector, long stopTime) {
		this.segmentationSemantic = segmentationSemantic;
		this.stopNameSemantic = stopNameSemantic;
		this.detector = detector;
		this.stopTime = stopTime;
	}

	public StopAndMove findStops(SemanticTrajectory T, AtomicInteger sid, AtomicInteger mid) {
		int size = T.length();
		int[] neighborhood = new int[size];
	
		for (int i = 0; i < neighborhood.length; i++) {
			neighborhood[i] = 0;
		}
	
		for (int i = 0; i < T.length(); i++) {
			int value = countNeighbors(i, T);
			neighborhood[i] = value;
			i += value;
		}
	
		StopAndMove ret = new StopAndMove(T);
		for (int i = 0; i < neighborhood.length; i++) {
			Instant p1 = Semantic.TEMPORAL.getData(T, i).getStart();
			Instant p2 = Semantic.TEMPORAL.getData(T, i + neighborhood[i]).getStart();

			long p1Milli = p1.toEpochMilli();
			long p2Milli = p2.toEpochMilli();
			E data = segmentationSemantic.getData(T, i);
			if (detector.isStop(data)) {
				String stopName = null;
				for (int j = i; j <= i + neighborhood[i]; j++) {
					Object stopNameValue = stopNameSemantic.getData(T, j);
					if(stopNameValue != null) {
						stopName = String.valueOf(stopNameValue);
						break;
					}
				}
				Stop s = new Stop(sid.incrementAndGet(), i, p1Milli, neighborhood[i] + 1, p2Milli);
				s.setCentroid(centroid(T, i, i + neighborhood[i]));
				s.setStopName(stopName);

				List<Integer> points = new ArrayList<>(neighborhood[i]);
				for (int x = 0; x <= neighborhood[i]; x++) {
					TPoint p = Semantic.SPATIAL.getData(T, i + x);
					points.add(Semantic.GID.getData(T, i + x).intValue());
					s.addPoint(p);
				}
				ret.addStop(s, points);
				i += neighborhood[i];
			} else {
				List<Integer> gids = new ArrayList<>();
				int init = i, j = i;
				for (; j < neighborhood.length && j < i + neighborhood[i] + 1; j++) {
					gids.add(Semantic.GID.getData(T, j).intValue());
				}
				ret.addMove(new Move(mid.incrementAndGet(), ret.lastStop(), null, p1Milli, p2Milli, init, j - init, null), gids);
				i += neighborhood[i];
			}
		}
	
		return ret;
	}

	private TPoint centroid(SemanticTrajectory T, int start, int end) {
		double x = 0;
		double y = 0;

		int i = start;
		int total = 0;
		while (i >= start && i <= end) {
			total++;
			TPoint point = Semantic.SPATIAL.getData(T, i);
			x += point.getX();
			y += point.getY();
			i++;
		}

		TPoint p = new TPoint(0, x / total, y / total, new Timestamp(0));
		return p;
	}

	private int countNeighbors(int i, SemanticTrajectory T) {
		int neighbors = 0;
		boolean yet = true;
		int j = i + 1;
		E p = segmentationSemantic.getData(T, i);
		while (j < T.length() && yet) {
			E d = segmentationSemantic.getData(T, j);
			if (Objects.equals(p, d)) {
				neighbors++;
			} else {
				Instant startAtStop = Semantic.TEMPORAL.getData(T, i).getStart();
				Instant endAtStop = Semantic.TEMPORAL.getData(T, j).getEnd();
				if(startAtStop.plus(this.stopTime, ChronoUnit.MILLIS).isAfter(endAtStop)) {
					return 0;
				}
				yet = false;
			}
			j++;
		}
		return neighbors;
	}
	
	public static interface StopDetector<E> {
		boolean isStop(E data);
	}
	
	private final static StopDetector<Object> NOT_NULL_DETECTOR = new StopDetector<Object>() {
		
		@Override
		public boolean isStop(Object data) {
			return data != null;
		}

	};
}
