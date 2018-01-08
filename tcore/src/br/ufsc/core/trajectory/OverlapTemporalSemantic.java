package br.ufsc.core.trajectory;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.joda.time.Interval;

final class OverlapTemporalSemantic extends Semantic<TemporalDuration, Number> {
	OverlapTemporalSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j) <= threshlod.doubleValue();
	}

	@Override
	public boolean match(TemporalDuration d1, TemporalDuration d2, Number threshlod) {
		return distance(d1, d2) <= threshlod.doubleValue();
	}

	@Override
	public Long distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		TemporalDuration a1 = (TemporalDuration) a.getDimensionData(index, i);
		TemporalDuration b1 = (TemporalDuration) b.getDimensionData(index, j);
		return (long) distance(a1, b1);
	}

	public double distance(TemporalDuration d1, TemporalDuration d2) {
		long aStart = LocalTime.from(d1.getStart().atZone(ZoneId.of("GMT"))).toSecondOfDay();
		long aEnd = LocalTime.from(d1.getEnd().atZone(ZoneId.of("GMT"))).toSecondOfDay();
		aEnd = aEnd < aStart ? aEnd + (24 * 60 * 60) : aEnd;
		long bStart = LocalTime.from(d2.getStart().atZone(ZoneId.of("GMT"))).toSecondOfDay();
		long bEnd = LocalTime.from(d2.getEnd().atZone(ZoneId.of("GMT"))).toSecondOfDay();
		bEnd = bEnd < bStart ? bEnd + (24 * 60 * 60) : bEnd;
		Interval intervalA = new Interval(aStart * 1000, aEnd * 1000);
		Interval intervalB = new Interval(bStart * 1000, bEnd * 1000);
		Interval overlapAtoB = intervalA.overlap(intervalB);
		if(overlapAtoB == null) {
			if(intervalA.equals(intervalB)) {
				return 0;
			}
			return 1;
		}
		double overlap = overlapAtoB.toDurationMillis();
		Instant lastEnd = Instant.ofEpochSecond(aEnd > bEnd ? bEnd: aEnd);
		Instant firstStart = Instant.ofEpochSecond(aStart < bStart ? aStart: bStart);
		double maxDuration = new Interval(firstStart.toEpochMilli(), lastEnd.toEpochMilli()).toDurationMillis();
		return (1 - overlap / maxDuration);
	}
}