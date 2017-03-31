package br.ufsc.lehmann.msm.artigo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.joda.time.Instant;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class MSMClassifier {

	private static final class MSMMeasurer implements IMeasureDistance<SemanticTrajectory> {
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new MSM(new MSMSemanticParameter(Semantic.GEOGRAPHIC, 100.0, .2),//
					new MSMSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L, .2),//
					new MSMSemanticParameter(new UserTypeSemantic(2), null, .2),//
					new MSMSemanticParameter(new GenderSemantic(3), null, .2),//
					new MSMSemanticParameter(new BirthYearSemantic(4), null, .2)).getDistance(t1.getX(), t2.getX());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Random y = new Random(trajectories.size());
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3), new MSMMeasurer());
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
