
package br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra;

import java.util.List;
import java.util.stream.IntStream;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix;

/**
 * Evaluates a cluster's validity by returning the maximum distance between any two points in the cluster.
 * 
 * @author Edward Raff
 */
public class MaxDistance implements IntraClusterEvaluation {
	private IMeasureDistance<SemanticTrajectory> dm;
	private DistanceMatrix<SemanticTrajectory> matrix;

	/**
	 * Creates a new MaxDistance
	 * 
	 * @param dm
	 *            the metric to measure the distance between two points by
	 */
	public MaxDistance(IMeasureDistance<SemanticTrajectory> dm) {
		this.dm = dm;
	}

	/**
	 * Creates a new MaxDistance
	 * 
	 * @param dm
	 *            the metric to measure the distance between two points by
	 */
	public MaxDistance(DistanceMatrix<SemanticTrajectory> matrix) {
		this.matrix = matrix;
	}

	@Override
	public double evaluate(int[] designations, List<SemanticTrajectory> dataSet, int clusterID) {
		final double[] maxDistance = new double[dataSet.size()];
		IntStream.of(dataSet.size()).parallel().forEach(i -> {
			for (int j = i + 1; j < dataSet.size(); j++)
				if (designations[i] == clusterID)
					maxDistance[i] = Math.max(
							matrix != null ? matrix.retrieve(dataSet.get(i), dataSet.get(j)) : dm.distance(dataSet.get(i), dataSet.get(j)), 
							maxDistance[i]);
		});
		return smile.math.Math.max(maxDistance);
	}

	@Override
	public double evaluate(List<SemanticTrajectory> dataPoints) {
		final double[] maxDistance = new double[dataPoints.size()];
		IntStream.of(dataPoints.size()).parallel().forEach(i -> {
			for (int j = i + 1; j < dataPoints.size(); j++)
				maxDistance[i] = Math.max(
						matrix != null ? matrix.retrieve(dataPoints.get(i), dataPoints.get(j)) : dm.distance(dataPoints.get(i), dataPoints.get(j)), 
						maxDistance[i]);
		});
		return smile.math.Math.max(maxDistance);
	}
}
