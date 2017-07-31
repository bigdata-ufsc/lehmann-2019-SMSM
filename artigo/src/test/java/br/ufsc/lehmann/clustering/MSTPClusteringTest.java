package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSTPTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSTPClusteringTest extends AbstractClusteringTest implements MSTPTest {

	public MSTPClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSTPTest.super.measurer(problem);
	}

}
