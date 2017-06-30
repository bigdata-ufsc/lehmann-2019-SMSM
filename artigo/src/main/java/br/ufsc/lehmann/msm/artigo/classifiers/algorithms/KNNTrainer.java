package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;

public class KNNTrainer<Label> implements ITrainer<Label> {

	@Override
	public IClassifier<Label> train(SemanticTrajectory[] trainx, Semantic<Label, ?> discriminator, IMeasureDistance<SemanticTrajectory> measure) {
		ArrayList<SemanticTrajectory> train = new ArrayList<>(Arrays.asList(trainx));
		List<DataEntry<SemanticTrajectory, Label>> entries = new ArrayList<>();
		for (SemanticTrajectory traj : train) {
			Label data = discriminator.getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory, Label>(traj, data));
		}
		NearestNeighbour<SemanticTrajectory, Label> nn = new NearestNeighbour<>(entries, Math.min(trainx.length, 3), measure, true);
		return new KNNClassifier<Label>(nn);
	}
}
