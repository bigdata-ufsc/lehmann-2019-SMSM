package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveAngleTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMoveClusteringTest extends AbstractClusteringTest implements MSMMoveAngleTest {

	public MSMMoveClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveAngleTest.super.measurer(problem);
	}

}
