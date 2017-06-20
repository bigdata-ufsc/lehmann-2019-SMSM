
package br.ufsc.lehmann.msm.artigo.clusterers.evaluation;

import br.ufsc.lehmann.msm.artigo.Trajectories;
import br.ufsc.lehmann.msm.artigo.clusterers.util.ClustererHelper;

/**
 * Base implementation for one of the methods in {@link ClusterEvaluation} to make life easier.
 * 
 * @author Edward Raff
 */
abstract public class ClusterEvaluationBase<T> implements ClusterEvaluation<T> {

	@Override
	public double evaluate(int[] designations, Trajectories<Number, T> dataSet) {
		return evaluate(ClustererHelper.createClusterListFromAssignmentArray(designations, dataSet));
	}
}
