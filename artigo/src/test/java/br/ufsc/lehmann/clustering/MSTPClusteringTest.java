package br.ufsc.lehmann.clustering;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.ComparableStopSemantic;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSTPClassifier;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;

public class MSTPClusteringTest extends AbstractClusteringTest {

	@Override
	IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSTPClassifier(problem.semantics()[0]);
		} else if(problem instanceof NewYorkBusProblem) {
			return new MSTPClassifier(problem.semantics()[0], new ComparableStopSemantic(NewYorkBusDataReader.STOP_SEMANTIC));
		}
		return null;
	}

}
