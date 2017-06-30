package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import smile.classification.RBFNetwork;

public class RBFSmileClassifier<Label> implements IClassifier<Label> {

	private RBFNetwork<SemanticTrajectory> nn;
	private List<Label> uniqueLabels;

	public RBFSmileClassifier(RBFNetwork<SemanticTrajectory> knn, List<Label> uniqueLabels) {
		nn = knn;
		this.uniqueLabels = uniqueLabels;
	}

	@Override
	public Label classify(SemanticTrajectory traj) {
		int prediction = nn.predict(traj);
		return uniqueLabels.get(prediction);
	}

}
