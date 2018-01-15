package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.H_MSM_StopMove_PointsTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class H_MSM_StopMove_PointsCompactnessTest extends AbstractCompactnessTest implements H_MSM_StopMove_PointsTest {

	public H_MSM_StopMove_PointsCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return H_MSM_StopMove_PointsTest.super.measurer(problem);
	}

}
