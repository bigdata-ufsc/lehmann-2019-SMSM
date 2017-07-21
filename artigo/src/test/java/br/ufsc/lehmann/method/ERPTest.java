package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface ERPTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new ERP(null, problem.semantics()[0]);
		} else if(problem instanceof NewYorkBusProblem) {
			return new ERP(null, problem.semantics()[0]);
		} else if(problem instanceof DublinBusProblem) {
			return new ERP(null, problem.semantics()[0]);
		}
		if(problem instanceof PatelProblem) {
			return new ERP(null, Semantic.GEOGRAPHIC);
		}
		return null;
	
	}
}
