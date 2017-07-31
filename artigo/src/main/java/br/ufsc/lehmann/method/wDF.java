package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
/**
 * 
 * @author Andre Salvaro Furtado
 *
 */
public class wDF implements IMeasureDistance<SemanticTrajectory> {
    private int w;
    
    public wDF(int w) {
		this.w = w;
    }

    @Override
    public double distance(SemanticTrajectory R, SemanticTrajectory S) {
        int n = R.length();
        int m = S.length();
        double DF[][] = new double[n][m];
       
        for (int i = 0; i<n; i++){
            for (int j = 0; j<m; j++){
                DF[i][j]=-1.0;
            }
        }
        return computeWDF(R, S, DF, n-1, m-1);
    }

    private double computeWDF(SemanticTrajectory R, SemanticTrajectory S, double DF[][], int i, int j) {
        if (DF[i][j] > -1.0) {
            return DF[i][j];
        } else if (Math.abs(i - j) > w) {
            DF[i][j] = Double.POSITIVE_INFINITY;
        } else if (i > 0 && j > 0) {
        	TPoint p1 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(R, i);
        	TPoint p2 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(S, j);
        	DF[i][j] = Math.max(
        			Math.min(computeWDF(R, S, DF, i - 1, j), Math.min(computeWDF(R, S, DF, i - 1, j - 1), computeWDF(R, S, DF, i, j - 1))),
        			Semantic.GEOGRAPHIC_EUCLIDEAN.distance(p1, p2));
        } else if (i > 0 && j == 0) {
        	TPoint p1 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(R, i);
        	TPoint p2 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(S, 0);
            DF[i][j] = Math.max(computeWDF(R, S, DF, i - 1, 0), Semantic.GEOGRAPHIC_EUCLIDEAN.distance(p1, p2));
        } else if (i == 0 && j > 0) {
        	TPoint p1 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(R, 0);
        	TPoint p2 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(S, j);
            DF[i][j] = Math.max(computeWDF(R, S, DF, 0, j - 1), Semantic.GEOGRAPHIC_EUCLIDEAN.distance(p1, p2));
        } else if (i == 0 && j == 0) {
        	TPoint p1 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(R, 0);
        	TPoint p2 = Semantic.GEOGRAPHIC_EUCLIDEAN.getData(S, 0);
        	DF[i][j] = Semantic.GEOGRAPHIC_EUCLIDEAN.distance(p1, p2);
        }
        return DF[i][j];
    }
    
    @Override
    public String name() {
    	return "wDF";
    }

}