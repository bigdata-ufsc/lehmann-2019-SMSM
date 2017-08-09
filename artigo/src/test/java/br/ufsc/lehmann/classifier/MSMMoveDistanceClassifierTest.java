package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveDistanceTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMoveDistanceClassifierTest extends AbstractClassifierTest implements MSMMoveDistanceTest {

	public MSMMoveDistanceClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveDistanceTest.super.measurer(problem);
	}

}
