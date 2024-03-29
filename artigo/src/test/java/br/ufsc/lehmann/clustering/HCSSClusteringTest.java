package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.HCSSTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class HCSSClusteringTest extends AbstractClusteringTest implements HCSSTest {

	public HCSSClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return HCSSTest.super.measurer(problem);
	}
}
