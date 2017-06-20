package br.ufsc.core.trajectory;

import java.time.Instant;

import org.joda.time.Interval;

import br.ufsc.utils.Distance;

public abstract class Semantic<V, T> {
	public static final Semantic<Number, Number> GID = new Semantic<Number, Number>(0) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j).doubleValue() <= threshlod.doubleValue();
		}

		@Override
		public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return distance((Number) a.getDimensionData(index, i), (Number) b.getDimensionData(index, j));
		}
		
		public double distance(Number d1, Number d2) {
			return ((Comparable<Number>) d1).compareTo((Number) d2);
		}
		
	};
	public static final Semantic<TPoint, Number> GEOGRAPHIC_EUCLIDEAN = new Semantic<TPoint, Number>(1) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
			return distance(a, i, b, j) <= threshold.doubleValue();
		}

		@Override
		public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return distance((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
		}
		
		public double distance(TPoint d1, TPoint d2) {
			return Distance.euclidean(d1, d2);
		}
		
	};
	public static final Semantic<TPoint, Number> GEOGRAPHIC_LATLON = new Semantic<TPoint, Number>(1) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
			return distance(a, i, b, j) <= threshold.doubleValue();
		}

		@Override
		public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return distance((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
		}
		
		public double distance(TPoint d1, TPoint d2) {
			return Distance.distFrom(d1, d2);
		}
		
	};
	public static final Semantic<TemporalDuration, Number> TEMPORAL = new Semantic<TemporalDuration, Number>(2) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j) <= threshlod.longValue();
		}

		@Override
		public Long distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			TemporalDuration a1 = (TemporalDuration) a.getDimensionData(index, i);
			TemporalDuration b1 = (TemporalDuration) b.getDimensionData(index, j);
			return (long) distance(a1, b1);
		}
		
		public double distance(TemporalDuration d1, TemporalDuration d2) {
			long aStart = d1.getStart().toEpochMilli();
			long aEnd = d1.getEnd().toEpochMilli();
			long bStart = d2.getStart().toEpochMilli();
			long bEnd = d2.getEnd().toEpochMilli();
			Interval intervalA = new Interval(aStart, aEnd);
			Interval intervalB = new Interval(bStart, bEnd);
			Interval overlapAtoB = intervalA.overlap(intervalB);
			if(overlapAtoB == null) {
				return 1L;
			}
			long overlap = overlapAtoB.toDurationMillis();
			Instant lastEnd = d1.getEnd().isAfter(d2.getEnd()) ? d1.getEnd() : d2.getEnd();
			Instant firstStart = d1.getStart().isBefore(d2.getStart()) ? d1.getStart() : d2.getStart();
			long maxDuration = new Interval(firstStart.toEpochMilli(), lastEnd.toEpochMilli()).toDurationMillis();
			return (1 - overlap / maxDuration);
		}
		
	};
	
	public static final Semantic<TPoint, Number> GEOGRAPHIC = GEOGRAPHIC_EUCLIDEAN;
	
	protected int index;
	
	public Semantic(int index) {
		this.index = index;
	}

	public V getData(SemanticTrajectory p, int i) {
		return (V) p.getDimensionData(index, i);
	}

	public abstract T distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j);

	public abstract boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, T threshlod);

	public abstract double distance(V d1, V d2);
}
