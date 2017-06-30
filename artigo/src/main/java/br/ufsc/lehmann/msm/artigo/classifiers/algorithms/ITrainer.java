package br.ufsc.lehmann.msm.artigo.classifiers.algorithms;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

public interface ITrainer<Label> {

	IClassifier<Label> train(SemanticTrajectory[] trainx, Semantic<Label, ?> discriminator, IMeasureDistance<SemanticTrajectory> measure);

}
