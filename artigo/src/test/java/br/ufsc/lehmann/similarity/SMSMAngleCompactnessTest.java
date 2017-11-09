package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveAngleTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMAngleCompactnessTest extends AbstractCompactnessTest implements MSMMoveAngleTest {

	public SMSMAngleCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveAngleTest.super.measurer(problem);
	}

}
