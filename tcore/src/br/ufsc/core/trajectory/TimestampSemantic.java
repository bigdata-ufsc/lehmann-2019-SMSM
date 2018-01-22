package br.ufsc.core.trajectory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimestampSemantic extends Semantic<Instant, Number> {
	public static final TimestampSemantic TIMESTAMP_TEMPORAL = new TimestampSemantic(2);

	public TimestampSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j) <= threshlod.doubleValue();
	}

	@Override
	public Long distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		TemporalDuration a1 = (TemporalDuration) a.getDimensionData(index, i);
		TemporalDuration b1 = (TemporalDuration) b.getDimensionData(index, j);
		return (long) distance(a1.getStart(), b1.getStart());
	}

	@Override
	public double distance(Instant d1, Instant d2) {
		long until = d1.until(d2, ChronoUnit.MINUTES);
		if(until == 0) {
			return 0.0;
		}
		return 1 - (1 / Math.abs(until));
	}

	@Override
	public boolean match(Instant d1, Instant d2, Number threshold) {
		return distance(d1, d2) <= threshold.doubleValue();
	}

}
