package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.LiuSchneiderTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class LiuSchneiderClassifierTest extends AbstractClassifierTest implements LiuSchneiderTest {

	public LiuSchneiderClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return LiuSchneiderTest.super.measurer(problem);
	}

}
