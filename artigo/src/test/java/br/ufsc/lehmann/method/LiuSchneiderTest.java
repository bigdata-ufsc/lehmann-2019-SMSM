package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;

public interface LiuSchneiderTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, NElementProblem.stop, 0.5));
		} else if(problem instanceof NewYorkBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((NewYorkBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof DublinBusProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, ((DublinBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof PatelProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, PatelDataReader.STOP_CENTROID_SEMANTIC, Thresholds.GEOGRAPHIC_LATLON));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, SanFranciscoCabDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof SergipeTracksProblem) {
			return new LiuSchneider(new LiuSchneiderParameters(0.5, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_LATLON));
			}
		return null;
	}
}
