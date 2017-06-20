
package br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity;

import java.util.List;
import java.util.Set;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

/**
 * Average similarity of all data point pairs between clusters, inter-cluster 
 * pairs are ignored. Also called UPGMC. 
 * 
 * @author Edward Raff
 */
public class CentroidDissimilarity extends LanceWilliamsDissimilarity implements UpdatableClusterDissimilarity
{
    /**
     * Creates a new CentroidDissimilarity
     * @param dm the distance measure to use between individual points
     */
    public CentroidDissimilarity(IMeasureDistance<SemanticTrajectory> dm)
    {
        super(dm);
    }
    
    @Override
    public double dissimilarity(List<SemanticTrajectory> a, List<SemanticTrajectory> b)
    {
        double sumDIss = 0;

        for (SemanticTrajectory ai : a)
            for (SemanticTrajectory bi : b)
                sumDIss += distance(ai, bi);

        return sumDIss/(a.size()*b.size());
    }

    @Override
    public double dissimilarity(Set<Integer> a, Set<Integer> b, double[][] distanceMatrix)
    {
        double sumDiss = 0;

        for (int ai : a)
            for (int bi : b)
                sumDiss += getDistance(distanceMatrix, ai, bi);

        return sumDiss/(a.size()*b.size());
    }

    @Override
    public double dissimilarity(int i, int ni, int j, int nj, double[][] distanceMatrix)
    {
        return getDistance(distanceMatrix, i, j);
    }

    @Override
    public double dissimilarity(int i, int ni, int j, int nj, int k, int nk, double[][] distanceMatrix)
    {
        double iPj = ni+nj;
        double ai = ni/iPj;
        double aj = nj/iPj;
        double b = - ni * nj / iPj*iPj;
        
        return ai* getDistance(distanceMatrix, i, k) + aj * getDistance(distanceMatrix, j, k) + b * getDistance(distanceMatrix, i, j);
    }

    @Override
    protected double aConst(boolean iFlag, int ni, int nj, int nk)
    {
        double denom = ni+nj;
        if(iFlag)
            return ni/denom;
        else
            return nj/denom;
    }

    @Override
    protected double bConst(int ni, int nj, int nk)
    {
        double nipj = ni + nj;
        return - ni*(double)nj/(nipj*nipj);
    }

    @Override
    protected double cConst(int ni, int nj, int nk)
    {
        return 0;
    }

    
}
