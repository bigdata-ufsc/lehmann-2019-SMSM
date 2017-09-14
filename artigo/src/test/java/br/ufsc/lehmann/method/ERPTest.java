package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.NElementProblem;
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

public interface ERPTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new ERP<Number>(null, NElementProblem.dataSemantic);
		} else if(problem instanceof NewYorkBusProblem) {
			return new ERP<Stop>(null, ((NewYorkBusProblem) problem).stopSemantic());
		} else if(problem instanceof DublinBusProblem) {
			return new ERP<Stop>(null, ((DublinBusProblem) problem).stopSemantic());
		} else if(problem instanceof PatelProblem) {
			return new ERP<Stop>(null, ((PatelProblem) problem).stopSemantic());
		} else if(problem instanceof VehicleProblem) {
			return new ERP<Stop>(null, ((VehicleProblem) problem).stopSemantic());
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new ERP<Stop>(null, ((SanFranciscoCabProblem) problem).stopSemantic());
		} else if(problem instanceof SergipeTracksProblem) {
			return new ERP<Stop>(null, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC);
		} else if(problem instanceof PrototypeProblem) {
			return new ERP<Stop>(null, PrototypeDataReader.STOP_SEMANTIC);
		} else if(problem instanceof PisaProblem) {
			return new ERP<Stop>(null, ((PisaProblem) problem).stopSemantic());
		}
		return null;
	
	}
}
