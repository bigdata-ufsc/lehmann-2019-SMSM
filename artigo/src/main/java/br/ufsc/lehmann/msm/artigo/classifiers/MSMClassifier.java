package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.Climate;
import br.ufsc.lehmann.msm.artigo.problems.ClimateWeatherSemantic;

public class MSMClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	MSM msm;
	
	public MSMClassifier(MSMSemanticParameter... params) {
		msm = new MSM(params);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return msm.distance(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return msm.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "MSM";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		ArrayList<DataEntry<SemanticTrajectory, Climate>> entries = new ArrayList<>();
		for (SemanticTrajectory traj : trajectories) {
			List<Climate> data = weatherSemantic.getData(traj, 0);
			entries.add(new DataEntry<>(traj, data.get(0)));
		}
		NearestNeighbour<SemanticTrajectory, Climate> nn = new NearestNeighbour<SemanticTrajectory, Climate>(entries, Math.min(trajectories.size(), 3),
				new MSMClassifier(new MSMSemanticParameter[] {//
						new MSMSemanticParameter(Semantic.GEOGRAPHIC, 100.0, .2), //
						new MSMSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L, .2), //
						new MSMSemanticParameter(BikeDataReader.USER, null, .2), //
						new MSMSemanticParameter(BikeDataReader.GENDER, null, .2), //
						new MSMSemanticParameter(BikeDataReader.BIRTH_YEAR, null, .2)//
					}), true);
		Climate classified = nn.classify(new DataEntry<>(trajectories.get(0), null));
		System.out.println(classified);
	}
}
