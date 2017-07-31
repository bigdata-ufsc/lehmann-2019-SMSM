package br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra;

import java.util.List;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;

/**
 * Evaluates a cluster's validity by computing the mean distance between all 
 * combinations of points. 
 * 
 * @author Edwar Raff
 */
public class MeanDistance implements IntraClusterEvaluation
{
    private IMeasureDistance<SemanticTrajectory> dm;

    /**
     * Creates a new MeanDistance
     * @param dm the metric to measure the distance between two points by
     */
    public MeanDistance(IMeasureDistance<SemanticTrajectory> dm)
    {
        this.dm = dm;
    }
    
    @Override
    public double evaluate(int[] designations, List<SemanticTrajectory> dataSet, int clusterID)
    {
        double distances = 0;
        for (int i = 0; i < dataSet.size(); i++)
            for (int j = i + 1; j < dataSet.size(); j++)
                if (designations[i] == clusterID)
                    distances += dm.distance(dataSet.get(i),
                                         dataSet.get(j));
        return distances/(dataSet.size()*(dataSet.size()-1));
    }

    @Override
    public double evaluate(List<SemanticTrajectory> dataPoints)
    {
        double distances = 0.0;
        for(int i = 0; i < dataPoints.size(); i++)
            for(int j = i+1; j < dataPoints.size(); j++ )
                distances += dm.distance(dataPoints.get(i),
                                     dataPoints.get(j));
        
        return distances/(dataPoints.size()*(dataPoints.size()-1));
    }
}
