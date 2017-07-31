package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMoveClusteringTest extends AbstractClusteringTest implements MSMMoveTest {

	public MSMMoveClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveTest.super.measurer(problem);
	}

}
