package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface LiuSchneiderTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, NElementProblem.stop, 0.5));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, NewYorkBusDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof DublinBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, DublinBusDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof PatelProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, PatelDataReader.STOP_SEMANTIC, Thresholds.GEOGRAPHIC_LATLON));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, SanFranciscoCabDataReader.STOP_SEMANTIC, Thresholds.STOP_CENTROID_LATLON));
		}
		return null;
	}
}
