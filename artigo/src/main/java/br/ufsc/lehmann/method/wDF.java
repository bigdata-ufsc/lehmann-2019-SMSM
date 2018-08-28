package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.ComputableDouble;
/**
 * 
 * @author Andre Salvaro Furtado
 *
 */
public class wDF extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {
    private Number w;
	private SpatialDistanceFunction distanceFunction;
    
    public wDF(Number w, SpatialDistanceFunction distanceFunction) {
		this.w = w;
		this.distanceFunction = distanceFunction;
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
        int window = w.intValue();
        if(w instanceof ComputableDouble) {
        	window = ((ComputableDouble) w).compute(R, S).intValue();
        }
        return computeWDF(R, S, DF, n-1, m-1, window);
    }

    private double computeWDF(SemanticTrajectory R, SemanticTrajectory S, double DF[][], int i, int j, int w) {
        if (DF[i][j] > -1.0) {
            return DF[i][j];
        } else if (Math.abs(i - j) > w) {
            DF[i][j] = Double.POSITIVE_INFINITY;
        } else if (i > 0 && j > 0) {
        	TPoint p1 = Semantic.SPATIAL.getData(R, i);
        	TPoint p2 = Semantic.SPATIAL.getData(S, j);
        	DF[i][j] = Math.max(
        			Math.min(computeWDF(R, S, DF, i - 1, j, w), Math.min(computeWDF(R, S, DF, i - 1, j - 1, w), computeWDF(R, S, DF, i, j - 1, w))),
        			distanceFunction.distance(p1, p2));
        } else if (i > 0 && j == 0) {
        	TPoint p1 = Semantic.SPATIAL.getData(R, i);
        	TPoint p2 = Semantic.SPATIAL.getData(S, 0);
            DF[i][j] = Math.max(computeWDF(R, S, DF, i - 1, 0, w), distanceFunction.distance(p1, p2));
        } else if (i == 0 && j > 0) {
        	TPoint p1 = Semantic.SPATIAL.getData(R, 0);
        	TPoint p2 = Semantic.SPATIAL.getData(S, j);
            DF[i][j] = Math.max(computeWDF(R, S, DF, 0, j - 1, w), distanceFunction.distance(p1, p2));
        } else if (i == 0 && j == 0) {
        	TPoint p1 = Semantic.SPATIAL.getData(R, 0);
        	TPoint p2 = Semantic.SPATIAL.getData(S, 0);
        	DF[i][j] = distanceFunction.distance(p1, p2);
        }
        return DF[i][j];
    }
    
    @Override
    public String name() {
    	return "wDF";
    }

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		double distance = distance(t1, t2);
		if(distance == Double.POSITIVE_INFINITY) {
			return 0;
		}
		return 1 - (distance / Math.max(distanceFunction.length(t1), distanceFunction.length(t2)));
	}

}