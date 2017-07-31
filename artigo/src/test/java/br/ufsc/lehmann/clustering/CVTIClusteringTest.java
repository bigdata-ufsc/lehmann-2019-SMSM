package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.CVTITest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class CVTIClusteringTest extends AbstractClusteringTest implements CVTITest {

	public CVTIClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return CVTITest.super.measurer(problem);
	}
}
