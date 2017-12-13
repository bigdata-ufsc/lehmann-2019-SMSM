package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.method.CVTI.CVTISemanticParameter;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface CVTITest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		if(problem instanceof NElementProblem) {
			return new CVTI(new CVTISemanticParameter<Number, Number>(NElementProblem.dataSemantic, null));
		} else if(problem instanceof NewYorkBusProblem) {
			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
		} else if(problem instanceof DublinBusProblem) {
			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
		} else if(problem instanceof PatelProblem) {
			stopSemantic = ((PatelProblem) problem).stopSemantic();
		} else if(problem instanceof VehicleProblem) {
			stopSemantic = ((VehicleProblem) problem).stopSemantic();
		} else if(problem instanceof SanFranciscoCabProblem) {
			stopSemantic = ((SanFranciscoCabProblem) problem).stopSemantic();
		} else if(problem instanceof SergipeTracksProblem) {
			stopSemantic = ((SergipeTracksProblem) problem).stopSemantic();
		} else if(problem instanceof PrototypeProblem) {
			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
		} else if(problem instanceof PisaProblem) {
			stopSemantic = ((PisaProblem) problem).stopSemantic();
		} else if(problem instanceof HermoupolisProblem) {
			stopSemantic = ((HermoupolisProblem) problem).stopSemantic();
		}
		return new CVTI(new CVTISemanticParameter<Stop, Number>(stopSemantic, Thresholds.calculateThreshold(stopSemantic)));
	}
}
