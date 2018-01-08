package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.H_MSM_StopMove_TemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class H_MSM_StopMove_TemporalDurationCompactnessTest extends AbstractCompactnessTest implements H_MSM_StopMove_TemporalDurationTest {

	public H_MSM_StopMove_TemporalDurationCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return H_MSM_StopMove_TemporalDurationTest.super.measurer(problem);
	}

}
