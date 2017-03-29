package br.ufsc.core.trajectory;

import java.time.Instant;

import org.joda.time.Interval;

import br.ufsc.utils.Distance;

public abstract class Semantic<V, T> {
	public static final Semantic<TPoint, Double> GEOGRAPHIC = new Semantic<TPoint, Double>(0) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Double threshlod) {
			return Distance.euclidean((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j)) <= threshlod;
		}
		
	};
	public static final Semantic<TemporalDuration, Long> TEMPORAL = new Semantic<TemporalDuration, Long>(1) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Long threshlod) {
			TemporalDuration a1 = (TemporalDuration) a.getDimensionData(index, i);
			TemporalDuration b1 = (TemporalDuration) b.getDimensionData(index, j);
			long aStart = a1.getStart().toEpochMilli();
			long aEnd = a1.getEnd().toEpochMilli();
			long bStart = b1.getStart().toEpochMilli();
			long bEnd = b1.getEnd().toEpochMilli();
			Interval intervalA = new Interval(aStart, aEnd);
			Interval intervalB = new Interval(bStart, bEnd);
			long overlap = intervalA.overlap(intervalB).toDurationMillis();
			Instant lastEnd = a1.getEnd().isAfter(b1.getEnd()) ? a1.getEnd() : b1.getEnd();
			Instant firstStart = a1.getStart().isBefore(b1.getStart()) ? a1.getStart() : b1.getStart();
			long maxDuration = new Interval(firstStart.toEpochMilli(), lastEnd.toEpochMilli()).toDurationMillis();
			return (1 - overlap / maxDuration) <= threshlod;
		}
		
	};
	protected int index;
	
	public Semantic(int index) {
		this.index = index;
	}

	public V getData(SemanticTrajectory p, int i) {
		return (V) p.getDimensionData(index, i);
	}

	public abstract boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, T threshlod);
}
