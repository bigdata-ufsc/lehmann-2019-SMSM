package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMEllipsesTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMEllipsesClassifierTest extends AbstractClassifierTest implements SMSMEllipsesTest {

	public SMSMEllipsesClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMEllipsesTest.super.measurer(problem);
	}

}
