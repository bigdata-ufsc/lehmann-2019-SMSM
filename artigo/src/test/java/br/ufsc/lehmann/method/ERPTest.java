package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
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

public interface ERPTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new ERP(null, NElementProblem.dataSemantic);
		} else if(problem instanceof NewYorkBusProblem) {
			return new ERP(null, ((NewYorkBusProblem) problem).stopSemantic());
		} else if(problem instanceof DublinBusProblem) {
			return new ERP(null, ((DublinBusProblem) problem).stopSemantic());
		} else if(problem instanceof PatelProblem) {
			return new ERP(null, PatelDataReader.STOP_CENTROID_SEMANTIC);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new ERP(null, ((SanFranciscoCabProblem) problem).stopSemantic());
		} else if(problem instanceof SergipeTracksProblem) {
			return new ERP(null, SergipeTracksDataReader.STOP_CENTROID_SEMANTIC);
		} else if(problem instanceof PrototypeProblem) {
			return new ERP(null, PrototypeDataReader.STOP_SEMANTIC);
		}
		return null;
	
	}
}
