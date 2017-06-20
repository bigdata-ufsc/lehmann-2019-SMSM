
package br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity;

import java.util.List;
import java.util.Set;

import org.junit.experimental.theories.DataPoint;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

/**
 * Measures the dissimilarity of two clusters by returning the value of the 
 * maximal dissimilarity of any two pairs of data points where one is from 
 * each cluster. 
 * 
 * @author Edward Raff
 */
public class CompleteLinkDissimilarity extends LanceWilliamsDissimilarity implements UpdatableClusterDissimilarity
{
    /**
     * Creates a new CompleteLinkDissimilarity 
     * @param dm the distance metric to use between individual points
     */
    public CompleteLinkDissimilarity(IMeasureDistance<SemanticTrajectory> dm)
    {
        super(dm);
    }
    
    @Override
    public double dissimilarity(List<SemanticTrajectory> a, List<SemanticTrajectory> b)
    {
        double maxDiss = Double.MIN_VALUE;

        double tmpDist;
        for (SemanticTrajectory ai : a)
            for (SemanticTrajectory bi : b)
                if ((tmpDist = distance(ai, bi)) > maxDiss)
                    maxDiss = tmpDist;

        return maxDiss;
    }

    @Override
    public double dissimilarity(Set<Integer> a, Set<Integer> b, double[][] distanceMatrix)
    {
        double maxDiss = Double.MIN_VALUE;

        for (int ai : a)
            for (int bi : b)
                if (getDistance(distanceMatrix, ai, bi) > maxDiss)
                    maxDiss = getDistance(distanceMatrix, ai, bi);

        return maxDiss;
    }

    @Override
    public double dissimilarity(int i, int ni, int j, int nj, double[][] distanceMatrix)
    {
        return getDistance(distanceMatrix, i, j);
    }

    @Override
    public double dissimilarity(int i, int ni, int j, int nj, int k, int nk, double[][] distanceMatrix)
    {
        return Math.max(getDistance(distanceMatrix, i, k), getDistance(distanceMatrix, j, k));
    }

    @Override
    public double dissimilarity(int ni, int nj, int nk, double d_ij, double d_ik, double d_jk)
    {
        return Math.max(d_ik, d_jk);
    }

    @Override
    protected double aConst(boolean iFlag, int ni, int nj, int nk)
    {
        return 0.5;
    }

    @Override
    protected double bConst(int ni, int nj, int nk)
    {
        return 0;
    }

    @Override
    protected double cConst(int ni, int nj, int nk)
    {
        return 0.5;
    }

}
