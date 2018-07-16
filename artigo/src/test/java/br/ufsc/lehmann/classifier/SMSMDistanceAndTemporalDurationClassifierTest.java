package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMDistanceAndTemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMDistanceAndTemporalDurationClassifierTest extends AbstractClassifierTest implements SMSMDistanceAndTemporalDurationTest {

	public SMSMDistanceAndTemporalDurationClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMDistanceAndTemporalDurationTest.super.measurer(problem);
	}

}
