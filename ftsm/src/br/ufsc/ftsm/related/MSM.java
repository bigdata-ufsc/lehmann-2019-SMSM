package br.ufsc.ftsm.related;

import java.util.Arrays;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class MSM extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private MSMSemanticParameter<?, ?>[] semantics;

	public MSM(MSMSemanticParameter<?, ?>... semantics) {
		this.semantics = semantics;
	}

	public double getDistance(Trajectory A, Trajectory B) {
		return distance(new SemanticTrajectory(A), new SemanticTrajectory(B));
	}

	public double getSimilarity(SemanticTrajectory A, SemanticTrajectory B) {
		int n = A.length();
		int m = B.length();
		double bScore[] = new double[m];
		
		double parityAB = 0.0;
		
		for (int i = 0; i < n; i++) {
			double maxScore = 0.0;
		
			for (int j = 0; j < m; j++) {
				double score = 0.0;
				double semanticScore = 0;
				for (int k = 0; k < semantics.length; k++) {
					Semantic semantic = semantics[k].semantic;
					Object threshlod = (Object) semantics[k].threshlod;
					double weight = semantics[k].weight;
					semanticScore += (semantic.match(A, i, B, j, threshlod) ? 1 : 0) * weight;
				}
				score = semanticScore;
		
				if (score >= maxScore) {
					maxScore = score;
					bScore[j] = maxScore > bScore[j] ? maxScore : bScore[j];
				}
			}
			parityAB += maxScore;
		}
		
		double parityBA = 0;
		for (int j = 0; j < m; j++) {
			parityBA += bScore[j];
		}
		
		double similarity = (parityAB + parityBA) / (n + m);

		return similarity;
	}

	public double distance(SemanticTrajectory A, SemanticTrajectory B) {
		return 1 - getSimilarity(A, B);
	}
	
	public static class MSMSemanticParameter<V, T> {
		public final double weight;
		public final Semantic<V, T> semantic;
		public final T threshlod;
		public MSMSemanticParameter(Semantic<V, T> semantic, T threshlod, double weight) {
			super();
			this.semantic = semantic;
			this.threshlod = threshlod;
			this.weight = weight;
		}
		public double getWeight() {
			return weight;
		}
		public Semantic<V, T> getSemantic() {
			return semantic;
		}
		public T getThreshlod() {
			return threshlod;
		}
	}
	
	@Override
	public String parametrization() {
		String semanticsString = "Params: ";
		for (MSMSemanticParameter<?, ?> d : this.semantics) {
			semanticsString += "(attr=" + d.semantic.description() + ", threshold=" + d.threshlod + ", weight=" + d.weight + ")\n\t";
		}
		return semanticsString;
	}
}
