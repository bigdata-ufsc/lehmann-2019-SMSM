package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.MTM;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NYBikeProblem;

public class MTMClassifier implements IMeasureDistance<SemanticTrajectory> {

	private MTM mtm;

	public MTMClassifier(Problem problem) {
		mtm = new MTM(problem.discriminator(), problem.data(), 5.0 * 60 * 100);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return mtm.distance(t1, t2);
	}

	@Override
	public String name() {
		return "MTM";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Random y = new Random(trajectories.size());
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3),
				new MTMClassifier(new NYBikeProblem()), true);
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
