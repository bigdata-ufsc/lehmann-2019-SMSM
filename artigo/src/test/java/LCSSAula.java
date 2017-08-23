public class LCSSAula {

	public static void main(String[] args) {
		String A = "What tensions you";
		String B = "O a tem som de u";
		double distance = distance(A.toCharArray(), B.toCharArray());
		System.out.println("Distance: " + distance);
		System.out.println("Similarity: " + (1 - (distance / (Math.min(A.length(), B.length())))));
	}

	public static double distance(char[] r, char[]s) {
		int m = r.length + 1;
		int n = s.length + 1;
		int[][] sequenceTable = new int[m][n];
		for (int i = 0; i < m; i++) {
			sequenceTable[i][0] = 0;
		}
		for (int j = 0; j < n; j++) {
			sequenceTable[0][j] = 0;
		}

		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {
				if (r[i - 1] == s[j - 1]) {
					sequenceTable[i][j] = sequenceTable[i - 1][j - 1] + 1;
				} else {
					sequenceTable[i][j] = Math.max(sequenceTable[i][j - 1], sequenceTable[i - 1][j]);
				}
			}
		}

		double similarity = sequenceTable[r.length][s.length];
		return similarity;
	}

}
