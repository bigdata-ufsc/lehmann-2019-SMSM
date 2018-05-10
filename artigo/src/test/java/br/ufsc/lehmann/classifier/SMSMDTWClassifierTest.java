package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMDTWTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMDTWClassifierTest extends AbstractClassifierTest implements SMSMDTWTest {

	public SMSMDTWClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMDTWTest.super.measurer(problem);
	}

}
