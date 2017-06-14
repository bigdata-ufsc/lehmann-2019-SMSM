package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.ERP;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;

public class ERPClassifierTest extends AbstractClassifierTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new ERP("", problem.semantics()[0]);
	}

}
