package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.CVTI.CVTISemanticParameter;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface CVTITest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new CVTI(new CVTISemanticParameter(NElementProblem.dataSemantic, null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new CVTI(new CVTISemanticParameter(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof DublinBusProblem) {
			return new CVTI(new CVTISemanticParameter(((DublinBusProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof PatelProblem) {
			return new CVTI(new CVTISemanticParameter(PatelDataReader.STOP_CENTROID_SEMANTIC, Thresholds.GEOGRAPHIC_LATLON));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new CVTI(new CVTISemanticParameter(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof SergipeTracksProblem) {
			return new CVTI(new CVTISemanticParameter(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.STOP_CENTROID_LATLON));
		} else if(problem instanceof PrototypeProblem) {
			return new CVTI(new CVTISemanticParameter(PrototypeDataReader.STOP_SEMANTIC, null));
		}
		return null;
	}
}
