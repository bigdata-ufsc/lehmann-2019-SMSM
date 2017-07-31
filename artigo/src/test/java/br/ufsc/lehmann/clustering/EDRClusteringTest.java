package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.EDRTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class EDRClusteringTest extends AbstractClusteringTest implements EDRTest {

	public EDRClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return EDRTest.super.measurer(problem);
	}

}
