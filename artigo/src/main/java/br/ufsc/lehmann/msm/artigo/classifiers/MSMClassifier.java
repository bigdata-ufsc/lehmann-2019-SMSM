package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.msm.artigo.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.Climate;
import br.ufsc.lehmann.msm.artigo.ClimateWeatherSemantic;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class MSMClassifier {

	public static final class MSMMeasurer implements IMeasureDistance<SemanticTrajectory> {
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new MSM(new MSMSemanticParameter(Semantic.GEOGRAPHIC, 100.0, .2),//
					new MSMSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L, .2),//
					new MSMSemanticParameter(BikeDataReader.USER, null, .2),//
					new MSMSemanticParameter(BikeDataReader.GENDER, null, .2),//
					new MSMSemanticParameter(BikeDataReader.BIRTH_YEAR, null, .2)).getDistance(t1.getX(), t2.getX());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		for (SemanticTrajectory traj : trajectories) {
			Climate[] data = weatherSemantic.getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory>(traj, data[0]));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new MSMMeasurer(), true);
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
