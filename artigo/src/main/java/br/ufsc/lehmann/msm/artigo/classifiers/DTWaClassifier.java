package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.method.DTWa;
import br.ufsc.lehmann.msm.artigo.MultiThreadClassificationExecutor;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;

public class DTWaClassifier<Label> extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private DTWa<Label> kernel;

	public DTWaClassifier(Problem problem, Semantic<?, Number>... semantics) {
		kernel = new DTWa<>(1.0, semantics);

		List<SemanticTrajectory> training = problem.trainingData();

		List<DataEntry<SemanticTrajectory, Label>> entries = new ArrayList<DataEntry<SemanticTrajectory, Label>>();
		for (SemanticTrajectory traj : training) {
			Label data = (Label) problem.discriminator().getData(traj, 0);
			entries.add(new DataEntry<>(traj, data));
		}
		kernel.training(entries);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return kernel.distance(t1, t2);
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return kernel.getSimilarity(t1, t2);
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
		executor.classifyProblem(problem, new DTWaClassifier<>(problem, problem.semantics()));
	}

}
