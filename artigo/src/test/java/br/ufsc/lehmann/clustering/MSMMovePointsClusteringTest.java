package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMovePointsTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMovePointsClusteringTest extends AbstractClusteringTest implements MSMMovePointsTest {

	public MSMMovePointsClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMovePointsTest.super.measurer(problem);
	}

}
