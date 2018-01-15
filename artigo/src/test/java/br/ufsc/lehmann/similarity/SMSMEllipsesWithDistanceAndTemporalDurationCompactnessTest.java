package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMEllipsesWithDistanceAndTemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMEllipsesWithDistanceAndTemporalDurationCompactnessTest extends AbstractCompactnessTest implements SMSMEllipsesWithDistanceAndTemporalDurationTest {

	public SMSMEllipsesWithDistanceAndTemporalDurationCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMEllipsesWithDistanceAndTemporalDurationTest.super.measurer(problem);
	}

}
