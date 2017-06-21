package br.ufsc.lehmann.clustering;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWClassifier;

public class DTWClusteringTest extends AbstractClusteringTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new DTWClassifier(problem.semantics()[0]);
	}
}
