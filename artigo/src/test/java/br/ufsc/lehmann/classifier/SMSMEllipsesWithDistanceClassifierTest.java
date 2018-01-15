package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMEllipsesWithDistanceTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMEllipsesWithDistanceClassifierTest extends AbstractClassifierTest implements SMSMEllipsesWithDistanceTest {

	public SMSMEllipsesWithDistanceClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMEllipsesWithDistanceTest.super.measurer(problem);
	}

}
