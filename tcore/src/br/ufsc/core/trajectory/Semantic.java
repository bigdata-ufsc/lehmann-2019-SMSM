package br.ufsc.core.trajectory;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.joda.time.Interval;

import br.ufsc.utils.Distance;

public abstract class Semantic<Element, Threshold> {
	public static final Semantic<Number, Number> GID = new Semantic<Number, Number>(0) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j).doubleValue() <= threshlod.doubleValue();
		}

		@Override
		public boolean match(Number d1, Number d2, Number threshlod) {
			return distance(d1, d2) <= threshlod.doubleValue();
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
		public boolean match(TPoint d1, TPoint d2, Number threshold) {
			return distance(d1, d2) <= threshold.doubleValue();
		}

		@Override
		public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return distance((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
		}
		
		public double distance(TPoint d1, TPoint d2) {
			return Distance.euclidean(d1, d2);
		}
		
		public double similarity(TPoint d1, TPoint d2) {
			return 1 / Math.max(1, distance(d1, d2));
		}
		
		public double similarity(TPoint d1, TPoint d2, Number threshold) {
			if(threshold == null) {
				return similarity(d1, d2);
			}
			double distance = distance(d1, d2);
			double t = threshold.doubleValue();
			return 1 / (distance <= t ? 1 : Math.max(1, distance));
		}
	};
	public static final Semantic<TPoint, Number> GEOGRAPHIC_LATLON = new Semantic<TPoint, Number>(1) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshold) {
			return distance(a, i, b, j) <= threshold.doubleValue();
		}

		@Override
		public boolean match(TPoint d1, TPoint d2, Number threshold) {
			return distance(d1, d2) <= threshold.doubleValue();
		}

		@Override
		public Double distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
			return distance((TPoint) a.getDimensionData(index, i), (TPoint) b.getDimensionData(index, j));
		}
		
		public double distance(TPoint d1, TPoint d2) {
			return Distance.distFrom(d1, d2);
		}
		
		public double similarity(TPoint d1, TPoint d2) {
			return 1 / Math.max(1, distance(d1, d2));
		}
		
		public double similarity(TPoint d1, TPoint d2, Number threshold) {
			if(threshold == null) {
				return similarity(d1, d2);
			}
			double distance = distance(d1, d2);
			double t = threshold.doubleValue();
			return 1 / (distance <= t ? 1 : Math.max(1, distance));
		}
		
	};
	public static final Semantic<TemporalDuration, Number> TEMPORAL = new Semantic<TemporalDuration, Number>(2) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j) <= threshlod.longValue();
		}

		@Override
		public boolean match(TemporalDuration d1, TemporalDuration d2, Number threshlod) {
			return distance(d1, d2) <= threshlod.longValue();
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
		
	};
	public static final Semantic<TemporalDuration, Number> TIMESTAMP = new Semantic<TemporalDuration, Number>(2) {

		@Override
		public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
			return distance(a, i, b, j) <= threshlod.longValue();
		}

		@Override
		public boolean match(TemporalDuration d1, TemporalDuration d2, Number threshlod) {
			return distance(d1, d2) <= threshlod.longValue();
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
				return 1L;
			}
			double overlap = overlapAtoB.toDurationMillis();
			Instant lastEnd = d1.getEnd().isAfter(d2.getEnd()) ? d1.getEnd() : d2.getEnd();
			Instant firstStart = d1.getStart().isBefore(d2.getStart()) ? d1.getStart() : d2.getStart();
			double maxDuration = new Interval(firstStart.toEpochMilli(), lastEnd.toEpochMilli()).toDurationMillis();
			return (1 - overlap / maxDuration);
		}
		
	};
	
	public static final Semantic<TPoint, Number> GEOGRAPHIC = GEOGRAPHIC_EUCLIDEAN;
	
	protected int index;
	
	public Semantic(int index) {
		this.index = index;
	}

	public Element getData(SemanticTrajectory p, int i) {
		return (Element) p.getDimensionData(index, i);
	}

	public double similarity(Element d1, Element d2) {
		return 1 - distance(d1, d2);
	}
	
	public double similarity(Element d1, Element d2, Number threshold) {
		if(threshold == null) {
			return similarity(d1, d2);
		}
		double similarity = similarity(d1, d2);
		double t = threshold.doubleValue();
		return similarity >= t ? 1 : similarity;
	}

	public abstract Threshold distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j);

	public abstract boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Threshold threshold);

	public abstract double distance(Element d1, Element d2);

	public abstract boolean match(Element d1, Element d2, Threshold threshold);
}
