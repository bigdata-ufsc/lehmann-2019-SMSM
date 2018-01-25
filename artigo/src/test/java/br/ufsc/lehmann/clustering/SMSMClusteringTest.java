package br.ufsc.lehmann.clustering;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMEllipsesTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class SMSMClusteringTest extends AbstractClusteringTest implements SMSMEllipsesTest {

	public SMSMClusteringTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMEllipsesTest.super.measurer(problem);
	}

}
