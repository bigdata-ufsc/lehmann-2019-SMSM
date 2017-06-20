package br.ufsc.lehmann.msm.artigo.clusterers.evaluation;

import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra.IntraClusterEvaluation;

/**
 * Evaluates a cluster based on the sum of scores for some {@link IntraClusterEvaluation} applied to each cluster.
 * 
 * @author Edward Raff
 */
public class IntraClusterSumEvaluation<T> extends ClusterEvaluationBase<T> {
	private IntraClusterEvaluation ice;

	/**
	 * Creates a new cluster evaluation that returns the sum of the intra cluster evaluations
	 * 
	 * @param ice
	 *            the intra cluster evaluation to use
	 */
	public IntraClusterSumEvaluation(IntraClusterEvaluation ice) {
		this.ice = ice;
	}

	@Override
	public double evaluate(List<List<SemanticTrajectory>> dataSets) {
		double score = 0;
		for (List<SemanticTrajectory> list : dataSets)
			score += ice.evaluate(list);
		return score;
	}
}
