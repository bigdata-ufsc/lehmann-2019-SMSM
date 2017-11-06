package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.CVTI.CVTISemanticParameter;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface CVTITest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new CVTI(new CVTISemanticParameter<Number, Number>(NElementProblem.dataSemantic, null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(((NewYorkBusProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((NewYorkBusProblem) problem).stopSemantic())));
		} else if(problem instanceof DublinBusProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(((DublinBusProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((DublinBusProblem) problem).stopSemantic())));
		} else if(problem instanceof PatelProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(((PatelProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((PatelProblem) problem).stopSemantic())));
		} else if(problem instanceof VehicleProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(((VehicleProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((VehicleProblem) problem).stopSemantic())));
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(((SanFranciscoCabProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((SanFranciscoCabProblem) problem).stopSemantic())));
		} else if(problem instanceof SergipeTracksProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, Thresholds.calculateThreshold(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC)));
		} else if(problem instanceof PrototypeProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(PrototypeDataReader.STOP_SEMANTIC, null));
		} else if(problem instanceof PisaProblem) {
			return new CVTI(new CVTISemanticParameter<Stop, Number>(((PisaProblem) problem).stopSemantic(), Thresholds.calculateThreshold(((PisaProblem) problem).stopSemantic())));
		}
		return null;
	}
}
