package br.ufsc.lehmann.msm.artigo;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.clusterers.util.IntList;

public class Trajectories<V, T> {

	private List<SemanticTrajectory> datapoints;
	private IntList category;
	private Semantic<V, T> discriminator;
	private List<V> labels;

	public Trajectories(List<SemanticTrajectory> datapoints, Semantic<V, T> discriminator) {
		this.datapoints = datapoints;
		this.discriminator = discriminator;
		this.labels = new ArrayList<>();
		this.category = new IntList();
		for (SemanticTrajectory semanticTrajectory : datapoints) {
			V data = discriminator.getData(semanticTrajectory, 0);
			if (!labels.contains(data)) {
				labels.add(data);
			}
			category.add(labels.indexOf(data));
		}
	}

	/**
	 * Computes the prior probabilities of each class, and returns an array containing the values.
	 * 
	 * @param cds
	 *            the dataset
	 * @return the array of prior probabilities
	 */
	public double[] getPriors() {
		double[] priors = new double[getClassSize()];

		double sum = 0.0;
		for (int i = 0; i < datapoints.size(); i++) {
			double w = 1;
			priors[getClassIndex(i)] += w;
			sum += w;
		}

		for (int i = 0; i < priors.length; i++)
			priors[i] /= sum;

		return priors;
	}

	public int getClassIndex(int i) {
		return category.get(i);
	}

	public int getClassSize() {
		return labels.size();
	}

	public int sampleSize() {
		return datapoints.size();
	}

	public SemanticTrajectory get(int i) {
		return datapoints.get(i);
	}

}
