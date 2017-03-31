package br.ufsc.lehmann.msm.artigo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class LCSSClassifier {

	private static final class LCSSMeasurer implements IMeasureDistance<SemanticTrajectory> {
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new LCSS(new LCSSSemanticParameter(Semantic.GEOGRAPHIC, 100.0),//
					new LCSSSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L),//
					new LCSSSemanticParameter(new UserTypeSemantic(2), null),//
					new LCSSSemanticParameter(new GenderSemantic(3), null),//
					new LCSSSemanticParameter(new BirthYearSemantic(4), null)).getDistance(t1.getX(), t2.getX());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Random y = new Random(trajectories.size());
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new LCSSMeasurer());
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
