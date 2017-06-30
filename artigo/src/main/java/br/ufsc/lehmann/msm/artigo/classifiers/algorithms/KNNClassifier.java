package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;

public class KNNClassifier<Label> implements IClassifier<Label> {

	private NearestNeighbour<SemanticTrajectory, Label> nn;

	public KNNClassifier(NearestNeighbour<SemanticTrajectory, Label> nn) {
		this.nn = nn;
	}

	@Override
	public Label classify(SemanticTrajectory traj) {
		return nn.classify(new DataEntry<SemanticTrajectory, Label>(traj, null));
	}

}
