package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.LCSSTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class LCSSCompactnessTest extends AbstractCompactnessTest implements LCSSTest {

	public LCSSCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return LCSSTest.super.measurer(problem);
	}

}
