package br.ufsc.lehmann.method;

import org.joda.time.Interval;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class CVTI extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private CVTISemanticParameter parameter;

	public CVTI(CVTISemanticParameter<?, ?> parameter) {
		this.parameter = parameter;
	}

	@Override
	public double getDistance(SemanticTrajectory R, SemanticTrajectory S) {
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
					TemporalDuration temporalDuration = Semantic.TEMPORAL.getData(R, i);
					Interval interval = new Interval(temporalDuration.getStart().toEpochMilli(), temporalDuration.getEnd().toEpochMilli());
					TemporalDuration temporalDuration2 = Semantic.TEMPORAL.getData(S, j);
					Interval interval2 = new Interval(temporalDuration2.getStart().toEpochMilli(), temporalDuration2.getEnd().toEpochMilli());
					CVTIMetric[i][j] = CVTIMetric[i - 1][j - 1] + interval.overlap(interval2).toDurationMillis();
				} else {
					CVTIMetric[i][j] = Math.max(CVTIMetric[i][j - 1], CVTIMetric[i - 1][j]);
				}
			}
		}

		double result = ((double) CVTIMetric[R.length()][S.length()] / Math.min(R.length(), S.length()));

		return result;
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
}
