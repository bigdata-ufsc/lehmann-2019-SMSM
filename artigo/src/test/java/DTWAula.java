import org.apache.commons.lang3.ArrayUtils;

public class DTWAula {

	public static void main(String[] args) {
		String A = "Restaurante Universitario";
		String B = "Mirantes Restaurante";
		System.out.println(Math.min(A.length(), B.length()) * (('Z' - 'A')));
		double distance = distance(A.toCharArray(), B.toCharArray());
		System.out.println("Distance: " + distance);
		System.out.println("Similarity: " + (1 - (distance / (Math.min(A.length(), B.length()) * (('Z' - 'A'))))));
	}
	
	public static double distance(char[] r, char[]s) {
		double[][] dist = new double[r.length + 1][s.length + 1];

		for (int i = 0; i <= r.length; ++i) {
			dist[i][0] = Double.MAX_VALUE;
		}
		for (int j = 0; j <= s.length; ++j) {
			dist[0][j] = Double.MAX_VALUE;
		}
		dist[0][0] = 0;

		for (int i = 1; i <= r.length; ++i) {
			for (int j = 1; j <= s.length; ++j) {
				char rp = Character.toUpperCase(r[i - 1]);
				char sp = Character.toUpperCase(s[j - 1]);
				double edd = Math.abs(rp - sp);
				double temp = edd + Math.min(dist[i - 1][j - 1], Math.min(dist[i - 1][j], dist[i][j - 1]));
				dist[i][j] = temp;
			}
		}
		
		System.out.println(toString(r.length + 1, s.length + 1, dist));

		return dist[r.length][s.length];
	}

	public static double distanceOptimized(char[] A, char[]B) {
		char[] p,q;
		if (A.length>=B.length){
			p = A;
			q = B;
		} else {
			p = B;
			q = A;
		}
		
		// "DTW matrix" in linear space.
		double[][] dtwMatrix = new double[2][p.length+1];
		// The absolute size of the warping window (to each side of the main diagonal)
		int w = (int) Math.ceil(p.length);

		// Initialization (all elements of the first line are INFINITY, except the 0th, and
		// the same value is given to the first element of the first analyzed line).
		for (int i = 0; i <= p.length; i++) {
			dtwMatrix[0][i] = Double.POSITIVE_INFINITY;
			dtwMatrix[1][i] = Double.POSITIVE_INFINITY;
		}
		dtwMatrix[0][0] = 0;

		// Distance calculation
		for (int i = 1; i <= q.length; i++) {
			int beg = Math.max(1,i-w);
			int end = Math.min(i+w,p.length);

			int thisI = i % 2;
			int prevI = (i-1) % 2;

			// Fixing values to this iteration
			dtwMatrix[i%2][beg-1] = Double.POSITIVE_INFINITY;

			for (int j = beg; j <= end; j++) {
				// DTW(i,j) = c(i-1,j-1) + min(DTW(i-1,j-1), DTW(i,j-1), DTW(i-1,j)).
				dtwMatrix[i%2][j] = Math.abs(Character.toUpperCase(q[i-1]) - Character.toUpperCase(p[j-1]))
					+ Math.min(dtwMatrix[thisI][j-1],Math.min(dtwMatrix[prevI][j], dtwMatrix[prevI][j-1]));
			}
		}
		return dtwMatrix[q.length%2][p.length];
	}

	public static String toString(int rows, int cols, double[][] mat) {

	    String str = "";

	    for (int i = 0 ; i<rows ; i ++ ){
	        for (int j = 0 ; j < cols ; j++){
	            str += mat[i][j]+"\t";
	        }
	        str += "\n";
	    }
	    return str;
	}
}
