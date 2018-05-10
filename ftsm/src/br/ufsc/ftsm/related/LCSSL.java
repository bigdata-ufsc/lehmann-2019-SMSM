package br.ufsc.ftsm.related;

import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;


public class LCSSL extends TrajectorySimilarityCalculator<Trajectory> {

    double threshold;

    // int subcostCalls=0;
    public LCSSL(double spaceThreshold) {
        this.threshold = spaceThreshold;
    }

    public double getSimilarity(Trajectory R, Trajectory S) {

        int m = R.length();
        int n = S.length();
        int[][] LCSS = new int[2][n + 1];
     
        for (int i = m - 1; i >= 0; i--) {
            //System.out.println(Arrays.toString(T[0]));
          int ndx = i & 1;//odd or even
          for (int j = n - 1; j >= 0; j--) {
            if (Distance.euclidean(R.getPoint(i), S.getPoint(j))<threshold) {
              LCSS[ndx][j] = 1 + LCSS[1 - ndx][j + 1];
            } else {
              LCSS[ndx][j] = Math.max(LCSS[1 - ndx][j], LCSS[ndx][j + 1]);
            }
          }
        }
      //  System.out.println(Arrays.toString(T[0]));
        return (double)LCSS[0][0]/Math.min(n, m);
      }

}