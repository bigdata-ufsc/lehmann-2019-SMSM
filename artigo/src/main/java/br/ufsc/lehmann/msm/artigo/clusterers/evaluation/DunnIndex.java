package br.ufsc.lehmann.msm.artigo.clusterers.evaluation;

import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Trajectories;
import br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity.ClusterDissimilarity;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra.IntraClusterEvaluation;
import br.ufsc.lehmann.msm.artigo.clusterers.util.ClustererHelper;


/**
 * Computes the Dunn Index (DI) using a customizable manner. Normally, a higher 
 * DI value indicates a better value. In order to conform to the interface 
 * contract of a lower value indicating a better result, the value of 1/(1+DI) 
 * is returned. 
 * 
 * @author Edward Raff
 */
public class DunnIndex<T> implements ClusterEvaluation<T> {
	private IntraClusterEvaluation ice;
	private ClusterDissimilarity cd;

	/**
	 * Creates a new DunnIndex
	 * 
	 * @param ice
	 *            the metric to measure the quality of a single cluster
	 * @param cd
	 *            the metric to measure the distance between two clusters
	 */
	public DunnIndex(IntraClusterEvaluation ice, ClusterDissimilarity cd) {
		this.ice = ice;
		this.cd = cd;
	}

	@Override
	public double evaluate(int[] designations, Trajectories<Number, T> dataSet) {
		return evaluate(ClustererHelper.createClusterListFromAssignmentArray(designations, dataSet));
	}

	@Override
	public double evaluate(List<List<SemanticTrajectory>> dataSets) {
		double minVal = Double.POSITIVE_INFINITY;
		double maxIntra = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < dataSets.size(); i++) {
			for (int j = i + 1; j < dataSets.size(); j++)
				minVal = Math.min(minVal, cd.dissimilarity(dataSets.get(i), dataSets.get(j)));
			maxIntra = Math.max(maxIntra, ice.evaluate(dataSets.get(i)));
		}
        
        /*
         * 
         * Instead of returning 1.0/(1.0+minVal/maxIntra) naivly
         * 
         *   1         y  
         * -----  =  -----
         *     x     x + y
         * 1 + -          
         *     y          
         * 
         * So return maxIntra/(minVal+maxIntra) instead, its numerically more 
         * stable and avoids an uneeded division. 
         * 
         */
        
        return maxIntra/(minVal+maxIntra);
    }
    
}
