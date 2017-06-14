package br.ufsc.lehmann.classifier;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;

public class LCSSClassifierTest extends AbstractClassifierTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return new LCSSClassifier(new LCSSSemanticParameter(problem.semantics()[0], null));
	}

}
