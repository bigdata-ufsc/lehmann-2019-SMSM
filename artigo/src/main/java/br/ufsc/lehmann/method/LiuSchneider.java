package br.ufsc.lehmann.method;

import br.ufsc.core.base.Point;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.utils.Distance;

public class LiuSchneider extends TrajectorySimilarityCalculator<SemanticTrajectory> {

	private double semanticWeight;

	public LiuSchneider(double semanticWeight) {
		this.semanticWeight = semanticWeight;
	}

	@Override
	public double getDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return geoDist(t1, t2) * (1 / (1 + semanticWeight * sem(t1, t2)));
	}

	public double geoDist(SemanticTrajectory t1, SemanticTrajectory t2) {
		double crtDist = centroidDistance(t1, t2);
		double cosineSimilarity = cosineSimilarity(t1, t2);
		return crtDist + (crtDist * (Math.abs(t1.length() - t2.length()) / Math.max((double) t1.length(), t2.length())))
				- ((t1.length() + t2.length()) / 2.0) * cosineSimilarity;
	}

	public double sem(SemanticTrajectory t1, SemanticTrajectory t2) {
		Trajectory a = new Trajectory(-1);
		Trajectory b = new Trajectory(-1);
		for (int i = 0; i < t1.length(); i++) {
			TPoint data = Semantic.GEOGRAPHIC.getData(t1, i);
			a.addPoint(data);
		}
		for (int i = 0; i < t2.length(); i++) {
			TPoint data = Semantic.GEOGRAPHIC.getData(t2, i);
			b.addPoint(data);
		}
		double distance = new LCSS(100).getDistance(a, b);
		return distance / Math.min(t1.length(), t2.length());
	}

	public static double cosineSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		TPoint startA = Semantic.GEOGRAPHIC.getData(t1, 0);
		TPoint endA = Semantic.GEOGRAPHIC.getData(t1, t1.length() - 1);
		TPoint startB = Semantic.GEOGRAPHIC.getData(t2, 0);
		TPoint endB = Semantic.GEOGRAPHIC.getData(t2, t2.length() - 1);
		double slope1 = startA.getY() - endA.getY() / startA.getX() - endA.getX();
		double slope2 = startB.getY() - endB.getY() / startB.getX() - endB.getX();
		double angle = Math.atan((slope1 - slope2) / (1 - (slope1 * slope2)));
		return Math.cos(angle);
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
		double xQuo = 0.0;
		double yQuo = 0.0;
		double xNum = 0.0;
		double yNum = 0.0;
		for (int i = 0; i < pA.length - 1; i++) {
			xQuo += Math.pow(pA[i + 1].getX(), 2.0) - Math.pow(pA[i].getX(), 2.0);
			xNum += pA[i + 1].getX() - pA[i].getX();
			yQuo += Math.pow(pA[i + 1].getY(), 2.0) - Math.pow(pA[i].getY(), 2.0);
			yNum += pA[i + 1].getY() - pA[i].getY();
		}
		return new double[] { xQuo / (2 * xNum), yQuo / (2 * yNum) };
	}
}
