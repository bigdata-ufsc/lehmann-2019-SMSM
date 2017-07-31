package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface ERPTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new ERP(null, NElementProblem.dataSemantic);
		} else if(problem instanceof NewYorkBusProblem) {
			return new ERP(null, NewYorkBusDataReader.STOP_SEMANTIC);
		} else if(problem instanceof DublinBusProblem) {
			return new ERP(null, DublinBusDataReader.STOP_SEMANTIC);
		} else if(problem instanceof PatelProblem) {
			return new ERP(null, PatelDataReader.STOP_SEMANTIC);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new ERP(null, SanFranciscoCabDataReader.STOP_SEMANTIC);
		}
		return null;
	
	}
}
