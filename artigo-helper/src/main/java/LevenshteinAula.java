public class LevenshteinAula {

	public static void main(String[] args) {
		String A = "What tensions you";
		String B = "O a tem som de u";
		
		double distance = distance(A, B);
		System.out.println("Distance: " + distance);
		System.out.println("Similarity: " + (1 - (distance / (Math.max(A.length(), B.length())))));
	}

    public static float distance(final String s, final String t) {
        final float[][] d; // matrix
        final int n; // length of s
        final int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        float cost; // cost

        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new float[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            // Step 4
            for (j = 1; j <= m; j++) {
                // Step 5
                cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;

                // Step 6
                d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + cost));
            }
        }

        // Step 7
        return d[n][m];
    }

}
