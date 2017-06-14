package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.DTW;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;

public class DTWClassifier {

	private static final class DTWMeasurer implements IMeasureDistance<SemanticTrajectory> {
		DTW dtw = new DTW();
		@Override
		public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
			return dtw.getSimilarity(t1, t2);
		}

		@Override
		public String name() {
			return "DTW";
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Random y = new Random(trajectories.size());
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new DTWMeasurer());
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
