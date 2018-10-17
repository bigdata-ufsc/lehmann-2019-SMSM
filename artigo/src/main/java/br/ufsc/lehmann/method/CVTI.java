package br.ufsc.lehmann.method;

import org.joda.time.Interval;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class CVTI extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private CVTISemanticParameter parameter;
	private Semantic<TemporalDuration, Number> temporal = Semantic.TEMPORAL;

	public CVTI(CVTISemanticParameter<?, ?> parameter) {
		this(parameter, Semantic.TEMPORAL);
	}

	public CVTI(CVTISemanticParameter<?, ?> parameter, Semantic<TemporalDuration, Number> temp) {
		this.parameter = parameter;
		this.temporal = temp;
	}

	@Override
	public double getSimilarity(SemanticTrajectory R, SemanticTrajectory S) {
		return similarity(R, S);
	}

	@Override
	public double distance(SemanticTrajectory R, SemanticTrajectory S) {
		return 1 - similarity(R, S);
	}

	private double similarity(SemanticTrajectory R, SemanticTrajectory S) {
		long[][] CVTIMetric = new long[R.length() + 1][S.length() + 1];

		for (int i = 0; i <= R.length(); i++) {
			CVTIMetric[i][0] = 0;
		}
		for (int i = 0; i <= S.length(); i++) {
			CVTIMetric[0][i] = 0;
		}

		CVTIMetric[0][0] = 0;

		for (int i = 1; i <= R.length(); i++) {
			for (int j = 1; j <= S.length(); j++) {
				if (parameter.semantic.match(R, i - 1, S, j - 1, parameter.threshlod)) {
					TemporalDuration temporalDuration = temporal.getData(R, i - 1);
					Interval interval = new Interval(temporalDuration.getStart().toEpochMilli(), temporalDuration.getEnd().toEpochMilli());
					TemporalDuration temporalDuration2 = temporal.getData(S, j - 1);
					long epochMilli = temporalDuration2.getStart().toEpochMilli();
					long epochMilli2 = temporalDuration2.getEnd().toEpochMilli();
					Interval interval2 = new Interval(epochMilli, epochMilli2);
					Interval overlap = interval.overlap(interval2);
					if(overlap != null && overlap.toDurationMillis() > 0) {
						long durationMillis = overlap.toDurationMillis();
						long durationMillis2 = interval.toDurationMillis();
						long durationMillis3 = interval2.toDurationMillis();
						CVTIMetric[i][j] = CVTIMetric[i - 1][j - 1] + (durationMillis / Math.min(durationMillis2, durationMillis3));
					} else {
						CVTIMetric[i][j] = CVTIMetric[i - 1][j - 1];
					}
				} else {
					CVTIMetric[i][j] = Math.max(CVTIMetric[i][j - 1], CVTIMetric[i - 1][j]);
				}
			}
		}

		double distance = (double) CVTIMetric[R.length()][S.length()];
		return distance / Math.min(R.length(), S.length());
	}

	@Override
	public String name() {
		return "CVTI";
	}

	public static class CVTISemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private T threshlod;

		public CVTISemanticParameter(Semantic<V, T> semantic, T threshlod) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
		}
	}

	@Override
	public String parametrization() {
		return "Semantic: " + parameter.semantic.description() + ", threshold: " + parameter.threshlod;
	}
}
