package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMEllipsesTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class H_MSM_StopMove_EllipsesCompactnessTest extends AbstractCompactnessTest implements SMSMEllipsesTest {

	public H_MSM_StopMove_EllipsesCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMEllipsesTest.super.measurer(problem);
	}

}
