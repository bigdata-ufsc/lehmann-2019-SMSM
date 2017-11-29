package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.SWALEClassifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface wDFTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_EUCLIDEAN);
		} else if(problem instanceof NewYorkBusProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof DublinBusProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof PatelProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_EUCLIDEAN);
		} else if(problem instanceof VehicleProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_EUCLIDEAN);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof SergipeTracksProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_LATLON);
		} else if(problem instanceof PrototypeProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_EUCLIDEAN);
		} else if(problem instanceof PisaProblem) {
			return new wDF(3, Semantic.GEOGRAPHIC_LATLON);
		}
		return null;
	}
}
