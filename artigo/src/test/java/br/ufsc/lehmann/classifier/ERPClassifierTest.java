package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.ERP;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class ERPClassifierTest extends AbstractClassifierTest {

	public ERPClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new ERP(null, problem.semantics()[0]);
		} else if(problem instanceof NewYorkBusProblem) {
			return new ERP(null, problem.semantics()[1]);
		}
		if(problem instanceof PatelProblem) {
			return new ERP(null, problem.semantics()[2]);
		}
		return null;
	
	}

}
