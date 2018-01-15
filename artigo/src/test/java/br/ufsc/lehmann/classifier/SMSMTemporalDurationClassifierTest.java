package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMTemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMTemporalDurationClassifierTest extends AbstractClassifierTest implements SMSMTemporalDurationTest {

	public SMSMTemporalDurationClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMTemporalDurationTest.super.measurer(problem);
	}

}
