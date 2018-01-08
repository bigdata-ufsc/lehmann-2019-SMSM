package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.H_MSM_StopMove_TemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMTemporalDurationClassifierTest extends AbstractClassifierTest implements H_MSM_StopMove_TemporalDurationTest {

	public SMSMTemporalDurationClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return H_MSM_StopMove_TemporalDurationTest.super.measurer(problem);
	}

}
