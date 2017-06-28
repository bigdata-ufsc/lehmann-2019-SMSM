package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.method.CVTI;
import br.ufsc.lehmann.method.CVTI.CVTISemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public class CVTIClassifierTest extends AbstractClassifierTest {

	public CVTIClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new CVTI(new CVTISemanticParameter(problem.semantics()[0], null));
		} else if(problem instanceof NewYorkBusProblem) {
			return new CVTI(new CVTISemanticParameter(problem.semantics()[1], 100));
		}
		if(problem instanceof PatelProblem) {
			return new CVTI(new CVTISemanticParameter(problem.semantics()[2], null));
		}
		return null;
	}
}
