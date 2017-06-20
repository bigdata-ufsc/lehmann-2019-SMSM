
package br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra;

import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

/**
 * Evaluates a cluster's validity by returning the 
 * maximum distance between any two points in the cluster. 
 * 
 * @author Edward Raff
 */
public class MaxDistance implements IntraClusterEvaluation
{
    private IMeasureDistance<SemanticTrajectory> dm;

    /**
     * Creates a new MaxDistance
     * @param dm the metric to measure the distance between two points by
     */
    public MaxDistance(IMeasureDistance<SemanticTrajectory> dm)
    {
        this.dm = dm;
    }
    
    @Override
    public double evaluate(int[] designations, List<SemanticTrajectory> dataSet, int clusterID)
    {
        double maxDistance = 0;
        for (int i = 0; i < dataSet.size(); i++)
            for (int j = i + 1; j < dataSet.size(); j++)
                if (designations[i] == clusterID)
                    maxDistance = Math.max(
                            dm.distance(dataSet.get(i),
                                    dataSet.get(j)),
                            maxDistance);
        return maxDistance;
    }

    @Override
    public double evaluate(List<SemanticTrajectory> dataPoints)
    {
        double maxDistance = 0;
        for(int i = 0; i < dataPoints.size(); i++)
            for(int j = i+1; j < dataPoints.size(); j++ )
                maxDistance = Math.max(
                        dm.distance(dataPoints.get(i), 
                                dataPoints.get(j)), 
                        maxDistance);
        
        return maxDistance;
    }
}
