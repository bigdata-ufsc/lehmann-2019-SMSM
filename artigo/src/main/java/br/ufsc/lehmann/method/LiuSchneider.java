package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.base.Point;
import br.ufsc.core.trajectory.GeographicDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;

public class LiuSchneider extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private double semanticWeight;
	private LCSS lcss;
	private GeographicDistanceFunction distance;

	public LiuSchneider(LiuSchneiderParameters<?, ?> params) {
		this.semanticWeight = params.semanticWeight;
		lcss = new LCSS(params.semanticDimension);
		this.distance = new EuclideanDistanceFunction();
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		//		double geoDist = geoSimilarity(t1, t2);
		//		double sem = sem(t1, t2);
		//		return geoDist / (1 + semanticWeight * sem);
		return 1 - getSimilarity(t1, t2);
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return geoSimilarity(t1, t2) / (1 + semanticWeight * sem(t1, t2));
	}

	public double geoSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		double crtDist = centroidDistance(t1, t2);
		double cosineSimilarity = cosineSimilarity(t1, t2);
//		return crtDist + (crtDist * (Math.abs(t1.length() - t2.length()) / Math.max((double) t1.length(), t2.length())))
//				- ((t1.length() + t2.length()) / 2.0) * cosineSimilarity;
		double t1_length = distance.length(t1);
		double t2_length = distance.length(t2);
		return (crtDist * (1 + (Math.abs(t1_length - t2_length) / Math.max(t1_length, t2_length))))
				- ((t1_length + t2_length) / 2) * cosineSimilarity;
	}

	public double sem(SemanticTrajectory t1, SemanticTrajectory t2) {
		double distance = lcss.getSimilarity(t1, t2);
		return distance;
	}
	
	@Override
	public String name() {
		return "Liu & Schneider";
	}

	public double cosineSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		double s1 = Semantic.GEOGRAPHIC.distance(t1, 0, t1, t1.length() - 1).doubleValue();
		double s2 = Semantic.GEOGRAPHIC.distance(t2, 0, t2, t2.length() - 1).doubleValue();
		return (s1 * s2) / (distance.length(t1) * distance.length(t2));
	}
	
	public static double centroidDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		Point[] pA = new Point[t1.length()];
		for (int i = 0; i < t1.length(); i++) {
			pA[i] = Semantic.GEOGRAPHIC.getData(t1, i);
		}
		double[] centroidA = centroid(pA);
		Point[] pB = new Point[t2.length()];
		for (int i = 0; i < t2.length(); i++) {
			pB[i] = Semantic.GEOGRAPHIC.getData(t2, i);
		}
		double[] centroidB = centroid(pB);
		return Distance.euclidean(centroidA, centroidB);
	}

	public static double[] centroid(Point[] pA) {
		double centroidX = 0, centroidY = 0;
		for (Point knot : pA) {
			centroidX += knot.getX();
			centroidY += knot.getY();
		}
		return new double[] { centroidX / pA.length, centroidY / pA.length };
	}
	
	public static class LiuSchneiderParameters<V, T> {
		double semanticWeight;
		LCSSSemanticParameter<V, T> semanticDimension;
		public LiuSchneiderParameters(double semanticWeight, Semantic<V, T> semanticDimension, T threshold) {
			this.semanticWeight = semanticWeight;
			this.semanticDimension = new LCSSSemanticParameter<V, T>(semanticDimension, threshold);
		} 
		
	}
}
