package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.UMSTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class UMSClassifierTest extends AbstractClassifierTest implements UMSTest {

	public UMSClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return UMSTest.super.measurer(problem);
	}
}
