package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveTemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMTemporalDurationClassifierTest extends AbstractClassifierTest implements MSMMoveTemporalDurationTest {

	public SMSMTemporalDurationClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveTemporalDurationTest.super.measurer(problem);
	}

}
