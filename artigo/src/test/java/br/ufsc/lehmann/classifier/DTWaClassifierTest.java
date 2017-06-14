package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWaClassifier;

public class DTWaClassifierTest extends AbstractClassifierTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new DTWaClassifier(problem);
	}
}
