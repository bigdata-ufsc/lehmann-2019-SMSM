package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.wDF;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public class wDFClassifierTest extends AbstractClassifierTest {

	public wDFClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new wDF(3);
	}
}
