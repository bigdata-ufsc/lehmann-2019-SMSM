package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public interface ITrainer<Label> {

	IClassifier<Label> train(SemanticTrajectory[] trainx, Semantic<Label, ?> discriminator, IMeasureDistance<SemanticTrajectory> measure);

}
