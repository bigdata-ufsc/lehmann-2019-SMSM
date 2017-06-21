package br.ufsc.lehmann.clustering;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.EDRClassifier;

public class EDRClusteringTest extends AbstractClusteringTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new EDRClassifier(new EDRSemanticParameter(problem.semantics()[0], null));
	}

}
