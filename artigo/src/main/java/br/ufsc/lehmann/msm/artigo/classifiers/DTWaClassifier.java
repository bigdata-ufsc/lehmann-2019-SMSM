package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.DTWa;
import br.ufsc.lehmann.msm.artigo.ClassificationExecutor;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.Problem;

public class DTWaClassifier {

	private static final class DTWaMeasurer implements IMeasureDistance<SemanticTrajectory> {

		private DTWa kernel;

		public DTWaMeasurer(Problem problem) {
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
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return kernel.getDistance(t1.getX(), t2.getX());
		}

		@Override
		public String name() {
			return "DTWa";
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, ExecutionException {
		ClassificationExecutor executor = new ClassificationExecutor();
		DublinBusProblem problem = new DublinBusProblem();
//		NYBikeProblem problem = new NYBikeProblem();
		executor.classify(problem, new DTWaMeasurer(problem));
	}

}
