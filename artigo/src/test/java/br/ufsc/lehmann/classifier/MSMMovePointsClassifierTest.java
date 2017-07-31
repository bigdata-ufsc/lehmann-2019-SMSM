package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMovePointsTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMovePointsClassifierTest extends AbstractClassifierTest implements MSMMovePointsTest {

	public MSMMovePointsClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMovePointsTest.super.measurer(problem);
	}

}
