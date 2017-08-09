package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MSMMoveEllipsesTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MSMMoveEllipsesClassifierTest extends AbstractClassifierTest implements MSMMoveEllipsesTest {

	public MSMMoveEllipsesClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MSMMoveEllipsesTest.super.measurer(problem);
	}

}
