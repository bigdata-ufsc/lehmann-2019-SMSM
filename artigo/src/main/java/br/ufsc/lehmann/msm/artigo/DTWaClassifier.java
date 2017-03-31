package br.ufsc.lehmann.msm.artigo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Enums;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.DTWa;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class DTWaClassifier {

	private static final class DTWaMeasurer implements IMeasureDistance<SemanticTrajectory> {
		
		private DTWa kernel;

		public DTWaMeasurer(List<DataEntry<SemanticTrajectory>> traines) {
			kernel = new DTWa(1.0, //
					Semantic.GEOGRAPHIC, //
					Semantic.TEMPORAL,//
					new UserTypeSemantic(2),//
					new GenderSemantic(3),//
					new BirthYearSemantic(4));
			kernel.training(traines);
		}
		
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return kernel.getDistance(t1.getX(), t2.getX());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		BikeDataReader readers = new BikeDataReader();
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		List<SemanticTrajectory> trajectories = readers.read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		for (SemanticTrajectory traj : trajectories) {
			Climate[] data = weatherSemantic.getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory>(traj, data[0]));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new DTWaMeasurer(entries.subList(0, entries.size() / 2)));
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
