package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.method.ERP;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.Climate;
import br.ufsc.lehmann.msm.artigo.problems.ClimateWeatherSemantic;

public class ERPClassifier<V> implements IMeasureDistance<SemanticTrajectory> {
	
	private ERP<V> erp;

	public ERPClassifier(ERPSemanticParameter<V, Number> parameters) {
		erp = new ERP<V>(parameters.threshlod, parameters.semantic);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return erp.distance(t1, t2);
	}

	@Override
	public String name() {
		return "ERP";
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
				new ERPClassifier(new ERPSemanticParameter(BikeDataReader.BIRTH_YEAR, "1980")));
		Climate classified = nn.classify(new DataEntry<>(trajectories.get(0), null));
		System.out.println(classified);
	}

	public static class ERPSemanticParameter<V, T> {
		private Semantic<V, T> semantic;
		private V threshlod;

		public ERPSemanticParameter(Semantic<V, T> semantic, V threshlod) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
		}
	}
}
