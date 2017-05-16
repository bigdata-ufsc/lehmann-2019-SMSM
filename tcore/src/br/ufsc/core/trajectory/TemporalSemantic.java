package br.ufsc.core.trajectory;

import java.time.Instant;

import org.joda.time.Interval;

public final class TemporalSemantic extends Semantic<TemporalDuration, Number> {
	TemporalSemantic(int index) {
		super(index);
	}

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
}