package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.wDFTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class wDFCompactnessTest extends AbstractCompactnessTest implements wDFTest {

	public wDFCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return wDFTest.super.measurer(problem);
	}

}
