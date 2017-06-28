package br.ufsc.lehmann.msm.artigo.classifiers;

import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import smile.classification.KNN;

public class KNNSmileClassifier<Label> implements IClassifier<Label> {

	private KNN<SemanticTrajectory> nn;
	private List<Label> uniqueLabels;

	public KNNSmileClassifier(KNN<SemanticTrajectory> knn, List<Label> uniqueLabels) {
		nn = knn;
		this.uniqueLabels = uniqueLabels;
	}

	@Override
	public Label classify(SemanticTrajectory traj) {
		int prediction = nn.predict(traj);
		return uniqueLabels.get(prediction);
	}

}
