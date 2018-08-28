package br.ufsc.lehmann.msm.artigo.classifiers;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.ITrainable;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.method.DTWa;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;

public class DTWaClassifier<Label> extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory>, ITrainable<SemanticTrajectory> {

	private DTWa<Label> kernel;
	private Semantic discriminator;
	
	public DTWaClassifier(Problem problem, Semantic<?, Number>... semantics) {
		this(problem.trainingData(), problem.discriminator(), semantics);
	}
	public DTWaClassifier(Semantic discriminator, Semantic<?, Number>... semantics) {
		this.discriminator = discriminator;
		kernel = new DTWa<>(1.0, semantics);
	}

	public DTWaClassifier(List<SemanticTrajectory> training, Semantic discriminator, Semantic<?, Number>... semantics) {
		this(discriminator, semantics);
		train(training);
	}

	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return kernel.distance(t1, t2);
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return kernel.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "DTWa";
	}

	@Override
	public void train(List<SemanticTrajectory> train) {
		List<DataEntry<SemanticTrajectory, Label>> entries = new ArrayList<DataEntry<SemanticTrajectory, Label>>();
		for (SemanticTrajectory traj : train) {
			Label data = (Label) discriminator.getData(traj, 0);
			entries.add(new DataEntry<>(traj, data));
		}
		kernel.training(entries);
	}
}
