package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMDistanceTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMDistanceClassifierTest extends AbstractClassifierTest implements SMSMDistanceTest {

	public SMSMDistanceClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMDistanceTest.super.measurer(problem);
	}

}
