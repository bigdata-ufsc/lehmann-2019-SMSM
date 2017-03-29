package br.ufsc.lehmann.msm.artigo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.lehmann.method.LiuSchneider;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class LiuSchneiderClassifier {

	private static final class LiuSchneiderMeasurer implements IMeasureDistance<SemanticTrajectory> {
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new LiuSchneider(1).getDistance(t1.getX(), t2.getX());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Random y = new Random();
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new LiuSchneiderMeasurer());
		Trajectory t1 = new Trajectory(1);
		t1.addPoint(new TPoint(40.76095756,-73.96724467));
		t1.addPoint(new TPoint(40.76096756,-73.96754467));
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(new SemanticTrajectory(t1), "descubra"));
		System.out.println(classified);
	}
}
