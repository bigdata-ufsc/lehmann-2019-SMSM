package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.DTWa;
import br.ufsc.lehmann.msm.artigo.MultiThreadClassificationExecutor;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.Problem;

public class DTWaClassifier implements IMeasureDistance<SemanticTrajectory> {

	private DTWa kernel;

	public DTWaClassifier(Problem problem) {
		kernel = new DTWa(1.0, problem.semantics());

		List<SemanticTrajectory> training = problem.trainingData();

		List<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		for (SemanticTrajectory traj : training) {
			Object data = problem.discriminator().getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory>(traj, data));
		}
		kernel.training(entries);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return kernel.distance(t1, t2);
	}

	@Override
	public String name() {
		return "DTWa";
	}

	public static void main(String[] args) throws IOException, InterruptedException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, ExecutionException {
		MultiThreadClassificationExecutor executor = new MultiThreadClassificationExecutor();
		DublinBusProblem problem = new DublinBusProblem();
		// NYBikeProblem problem = new NYBikeProblem();
		executor.classify(problem, new DTWaClassifier(problem));
	}

}
