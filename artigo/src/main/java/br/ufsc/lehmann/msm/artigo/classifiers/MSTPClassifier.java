package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.method.MSTP;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;

public class MSTPClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private MSTP mstp;

	public MSTPClassifier(Semantic... semantics) {
		this.mstp = new MSTP(semantics);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return mstp.distance(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return mstp.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "MSTP";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory, String>> entries = new ArrayList<>();
		Random y = new Random(trajectories.size());
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory, String> nn = new NearestNeighbour<SemanticTrajectory, String>(entries, Math.min(trajectories.size(), 3),
				new MSTPClassifier(new Semantic[] { Semantic.SPATIAL, //
						Semantic.TEMPORAL, //
						BikeDataReader.USER, //
						BikeDataReader.GENDER, //
						BikeDataReader.BIRTH_YEAR//
				}));
		Object classified = nn.classify(new DataEntry<>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}
