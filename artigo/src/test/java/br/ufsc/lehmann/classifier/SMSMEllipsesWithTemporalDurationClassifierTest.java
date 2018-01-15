package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMEllipsesWithTemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMEllipsesWithTemporalDurationClassifierTest extends AbstractClassifierTest implements SMSMEllipsesWithTemporalDurationTest {

	public SMSMEllipsesWithTemporalDurationClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMEllipsesWithTemporalDurationTest.super.measurer(problem);
	}

}
