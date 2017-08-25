package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import smile.classification.KNN;

public class KNNSmileTrainer<Label> implements ITrainer<Label> {

	@Override
	public IClassifier<Label> train(SemanticTrajectory[] trainx, Semantic<Label, ?> discriminator, IMeasureDistance<SemanticTrajectory> measure) {
		ArrayList<SemanticTrajectory> train = new ArrayList<>(Arrays.asList(trainx));
		List<Label> labels = new ArrayList<>();
		for (SemanticTrajectory traj : train) {
			Label data = discriminator.getData(traj, 0);
			labels.add(data);
		}
		int[] y = new int[labels.size()];
		List<Label> uniqueLabels = new ArrayList<>(new LinkedHashSet<>(labels));
		if(uniqueLabels.size() == 1) {
			throw new IllegalStateException("Only one class");
		}
		for (int i = 0; i < y.length; i++) {
			y[i] = uniqueLabels.indexOf(labels.get(i));
		}
		
		KNN<SemanticTrajectory> knn = new KNN<>(trainx, y, new SmileDistanceWrapper<Label>(measure));
		return new KNNSmileClassifier<Label>(knn, uniqueLabels);
	}
}
