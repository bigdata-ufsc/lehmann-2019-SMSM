package br.ufsc.lehmann.clustering;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.LCSSTest;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public class LCSSClusteringTest extends AbstractClusteringTest implements LCSSTest {

	public LCSSClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return LCSSTest.super.measurer(problem);
	}

}
