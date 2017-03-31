package br.ufsc.core.trajectory;

import java.time.Instant;

import org.joda.time.Interval;

import br.ufsc.utils.Distance;

public abstract class Semantic<V, T> {
	public static final Semantic<TPoint, Number> GEOGRAPHIC = new Semantic<TPoint, Number>(0) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j) <= threshlod.doubleValue();
		}

		@Override
		public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return Distance.euclidean((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
		}
		
	};
	public static final Semantic<TemporalDuration, Number> TEMPORAL = new Semantic<TemporalDuration, Number>(1) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j) <= threshlod.longValue();
		}

		@Override
		public Long distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			TemporalDuration a1 = (TemporalDuration) a.getDimensionData(index, i);
			TemporalDuration b1 = (TemporalDuration) b.getDimensionData(index, j);
			long aStart = a1.getStart().toEpochMilli();
			long aEnd = a1.getEnd().toEpochMilli();
			long bStart = b1.getStart().toEpochMilli();
			long bEnd = b1.getEnd().toEpochMilli();
			Interval intervalA = new Interval(aStart, aEnd);
			Interval intervalB = new Interval(bStart, bEnd);
			Interval overlapAtoB = intervalA.overlap(intervalB);
			if(overlapAtoB == null) {
				return 1L;
			}
			long overlap = overlapAtoB.toDurationMillis();
			Instant lastEnd = a1.getEnd().isAfter(b1.getEnd()) ? a1.getEnd() : b1.getEnd();
			Instant firstStart = a1.getStart().isBefore(b1.getStart()) ? a1.getStart() : b1.getStart();
			long maxDuration = new Interval(firstStart.toEpochMilli(), lastEnd.toEpochMilli()).toDurationMillis();
			return (1 - overlap / maxDuration);
		}
		
	};
	protected int index;
	
	public Semantic(int index) {
		this.index = index;
	}

	public V getData(SemanticTrajectory p, int i) {
		return (V) p.getDimensionData(index, i);
	}

	public abstract T distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j);

	public abstract boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, T threshlod);
}
