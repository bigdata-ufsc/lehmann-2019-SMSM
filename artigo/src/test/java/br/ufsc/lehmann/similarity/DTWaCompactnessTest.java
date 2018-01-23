package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.DTWaTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class DTWaCompactnessTest extends AbstractCompactnessTest implements DTWaTest {

	public DTWaCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return DTWaTest.super.measurer(problem);
	}

}
