package br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

/**
 * Also known as Group-Average Agglomerative Clustering (GAAC) and UPGMA, this
 * measure computer the dissimilarity by summing the distances between all
 * possible data point pairs in the union of the clusters.
 *
 * @author Edward Raff
 */
public class AverageLinkDissimilarity extends LanceWilliamsDissimilarity implements UpdatableClusterDissimilarity
{
    /**
     * Creates a new AverageLinkDissimilarity 
     * @param dm the distance measure to use on individual points
     */
    public AverageLinkDissimilarity(IMeasureDistance<SemanticTrajectory> dm)
    {
        super(dm);
    }
    
    @Override
    public double dissimilarity(List<SemanticTrajectory> a, List<SemanticTrajectory> b)
    {
        double disSum = 0;
        
        int allSize = a.size()+b.size();
        
        List<SemanticTrajectory> allPoints = new ArrayList<>(allSize);
        allPoints.addAll(a);
        allPoints.addAll(b);
        
        for(int i = 0; i < allPoints.size(); i++)
            for(int j = i+1; j < allPoints.size(); j++)
                disSum += distance(allPoints.get(i), allPoints.get(j));
        
        return disSum/(allSize*(allSize-1));
    }

    @Override
    public double dissimilarity(Set<Integer> a, Set<Integer> b, double[][] distanceMatrix)
    {
        double disSum = 0;
        
        int allSize = a.size()+b.size();
        
        int[] allPoints = new int[allSize];
        int z = 0;
        for(int val : a)
            allPoints[z++] = val;
        for(int val : b)
            allPoints[z++] = val;
        
        for(int i = 0; i < allPoints.length; i++)
            for(int j = i+1; j < allPoints.length; j++)
                disSum += getDistance(distanceMatrix, allPoints[i], allPoints[j]);
        
        return disSum/(allSize*(allSize-1));
    }

    @Override
    public double dissimilarity(int i, int ni, int j, int nj, double[][] distanceMatrix)
    {
        return getDistance(distanceMatrix, i, j);
    }

    @Override
    public double dissimilarity(int i, int ni, int j, int nj, int k, int nk, double[][] distanceMatrix)
    {
        double ai = ni/(double)(ni+nj);
        double aj = nj/(double)(ni+nj);
        return ai * getDistance(distanceMatrix, i, k) + aj * getDistance(distanceMatrix, j, k);
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
        return 0;
    }

    @Override
    protected double cConst(int ni, int nj, int nk)
    {
        return 0;
    }
    
}
