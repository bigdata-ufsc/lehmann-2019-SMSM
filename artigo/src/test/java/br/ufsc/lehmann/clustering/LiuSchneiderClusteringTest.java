package br.ufsc.lehmann.clustering;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.LiuSchneiderTest;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public class LiuSchneiderClusteringTest extends AbstractClusteringTest implements LiuSchneiderTest {

	public LiuSchneiderClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return LiuSchneiderTest.super.measurer(problem);
	}

}
