package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.DTWaTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class DTWaClusteringTest extends AbstractClusteringTest implements DTWaTest {

	public DTWaClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return DTWaTest.super.measurer(problem);
	}
}
