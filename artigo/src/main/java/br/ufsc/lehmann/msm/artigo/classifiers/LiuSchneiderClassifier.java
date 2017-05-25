package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.LiuSchneider;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.Climate;
import br.ufsc.lehmann.msm.artigo.problems.ClimateWeatherSemantic;

public class LiuSchneiderClassifier {

	private static final class LiuSchneiderMeasurer implements IMeasureDistance<SemanticTrajectory> {
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new LiuSchneider(1).getDistance(t1.getX(), t2.getX());
		}

		@Override
		public String name() {
			return "Liu&Schneider";
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		for (SemanticTrajectory traj : trajectories.subList(trajectories.size() / 3, trajectories.size() - 1)) {
			List<Climate> data = weatherSemantic.getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory>(traj, data.get(0)));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new LiuSchneiderMeasurer());
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
