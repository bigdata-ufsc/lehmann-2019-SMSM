
package br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;

/**
 * A base class for Dissimilarity measures that are build ontop the use of some {@link DistanceMetric distance metric}. 
 * 
 * @author Edward Raff
 */
public abstract class DistanceMetricDissimilarity extends AbstractClusterDissimilarity 
{
    /**
     * The distance metric that will back this dissimilarity measure. 
     */
    protected final IMeasureDistance<SemanticTrajectory> dm;
    public DistanceMetricDissimilarity(IMeasureDistance<SemanticTrajectory> dm)
    {
        this.dm = dm;
    }

    @Override
    public double distance(SemanticTrajectory a, SemanticTrajectory b)
    {
        return dm.distance(a, b);
    }    
}
