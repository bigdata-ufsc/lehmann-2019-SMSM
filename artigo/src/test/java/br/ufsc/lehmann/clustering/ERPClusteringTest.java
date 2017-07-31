package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.ERPTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class ERPClusteringTest extends AbstractClusteringTest implements ERPTest {

	public ERPClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return ERPTest.super.measurer(problem);
	}

}
