package br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;

/**
 * An implementation of Ward's method for hierarchical clustering. This method 
 * merges clusters based on the minimum total variance of the resulting 
 * clusters. 
 * 
 * @author Edward Raff
 */
public class WardsDissimilarity extends LanceWilliamsDissimilarity
{

    public WardsDissimilarity(IMeasureDistance<SemanticTrajectory> dm) {
		super(dm);
	}

	@Override
    protected double aConst(boolean iFlag, int ni, int nj, int nk)
    {
        double totalPoints = ni+nj+nk;
        if(iFlag)
            return (ni+nk)/totalPoints;
        else
            return (nj+nk)/totalPoints;
    }

    @Override
    protected double bConst(int ni, int nj, int nk)
    {
        double totalPoints = ni+nj+nk;
        return -nk/totalPoints;
    }

    @Override
    protected double cConst(int ni, int nj, int nk)
    {
        return 0;
    }
    
}
