package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveTest;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMoveClassifierTest extends AbstractClassifierTest implements MSMMoveTest {

	public MSMMoveClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveTest.super.measurer(problem);
	}

}
