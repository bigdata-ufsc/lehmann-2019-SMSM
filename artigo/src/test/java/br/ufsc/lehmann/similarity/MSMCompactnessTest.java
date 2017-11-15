package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMCompactnessTest extends AbstractCompactnessTest implements MSMTest {

	public MSMCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMTest.super.measurer(problem);
	}

}
