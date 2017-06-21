package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.LiuSchneider;
import br.ufsc.lehmann.method.LiuSchneider.LiuSchneiderParameters;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.Climate;
import br.ufsc.lehmann.msm.artigo.problems.ClimateWeatherSemantic;

public class LiuSchneiderClassifier implements IMeasureDistance<SemanticTrajectory> {

	private LiuSchneider liuSchneider;
	
	public LiuSchneiderClassifier(LiuSchneiderParameters params) {
		this.liuSchneider = new LiuSchneider(params);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return liuSchneider.distance(t1, t2);
	}

	@Override
	public String name() {
		return "Liu&Schneider";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		ArrayList<DataEntry<SemanticTrajectory, Climate>> entries = new ArrayList<>();
		for (SemanticTrajectory traj : trajectories.subList(trajectories.size() / 3, trajectories.size() - 1)) {
			List<Climate> data = weatherSemantic.getData(traj, 0);
			entries.add(new DataEntry<>(traj, data.get(0)));
		}
		NearestNeighbour<SemanticTrajectory, Climate> nn = new NearestNeighbour<SemanticTrajectory, Climate>(entries, Math.min(trajectories.size(), 3),
				new LiuSchneiderClassifier(new LiuSchneiderParameters(0.5, BikeDataReader.BIRTH_YEAR, 100)));
		Object classified = nn.classify(new DataEntry<>(trajectories.get(0), null));
		System.out.println(classified);
	}
}
