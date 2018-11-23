package br.ufsc.ftsm.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.ETrajectory;
import br.ufsc.ftsm.base.Ellipse;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;

public class CreateEllipseMath {
	
	private SpatialDistanceFunction distanceFunction;
	private boolean fixedMultiplier;
	private double averageEllipseDistance;
	private double stdEllipseDistance;

	public CreateEllipseMath() {
		this(new EuclideanDistanceFunction());
	}

	public CreateEllipseMath(boolean fixedMultiplier) {
		this(new EuclideanDistanceFunction(), -1, -1, fixedMultiplier);
	}
	
	public CreateEllipseMath(SpatialDistanceFunction distanceFunction) {
		this(distanceFunction, -1, -1, true);
	}
	
	public CreateEllipseMath(SpatialDistanceFunction distanceFunction, double averageEllipseDistance, double stdEllipseDistance) {
		this(distanceFunction, averageEllipseDistance, stdEllipseDistance, false);
	}
	
	public CreateEllipseMath(SpatialDistanceFunction distanceFunction, double averageEllipseDistance, double stdEllipseDistance, boolean fixedMultiplier) {
		this.distanceFunction = distanceFunction;
		this.averageEllipseDistance = averageEllipseDistance;
		this.stdEllipseDistance = stdEllipseDistance;
		this.fixedMultiplier = fixedMultiplier;
	}
	
	public ETrajectory createEllipticalTrajectoryFixed(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid(), t.length());
		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);
			double x = (p1.getX() + p2.getX()) / 2;
			double y = (p1.getY() + p2.getY()) / 2;

			double fociDistance = distanceFunction.distance(p1, p2);
			double majorAxis = Distance.triangular(p1, p2) + 1;

			double fociDistanceSquare = fociDistance * fociDistance;
			double majorAxisSquare = majorAxis * majorAxis;

			double minorAxis = Math.sqrt(majorAxisSquare - fociDistanceSquare);

			double angleO = Distance.angle(p1, p2);

			Ellipse e = new Ellipse();
			e.setEid(i);
			e.setCenter(new TPoint(x, y));
			e.setF1(p1);
			e.setF2(p2);
			e.setSemiMajorAxis(majorAxis / 2);
			e.setSemiMinorAxis(minorAxis / 2);
			e.setMajorAxis(majorAxis);
			e.setMinorAxis(minorAxis);
			e.setAngle(angleO);
			e.setEccentricity(fociDistance);
			e.setSemiMajorAxisSquare(e.getSemiMinorAxis() * e.getSemiMinorAxis());
			e.setSemiMajorAxisSquare(e.getSemiMajorAxis() * e.getSemiMajorAxis());
			e.setXi((e.getSemiMinorAxisSquare() + e.getSemiMajorAxisSquare()) / e.getMajorAxis());
			T.addEllipse(e);
			i++;
		}
		T.setT(t);

		return T;
	}
	
	public ETrajectory createEllipticalTrajectory(int tid, TPoint[] points) {
		if(!fixedMultiplier) {
			return createEllipticalTrajectoryFixed(tid, points);
		}
		return createEllipticalTrajectoryDynamic(tid, points);
	}
	
	public ETrajectory createEllipticalTrajectoryDynamic(int tid, TPoint[] points) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 1; i < points.length; i++) {
			stats.addValue(distanceFunction.distance(points[i - 1], points[i]));
		}
		double avg = stats.getMean();
		double std = stats.getStandardDeviation();
		double stdLocalFromGlobal = (avg - averageEllipseDistance) / stdEllipseDistance;
		int i = 0;
		ETrajectory T = new ETrajectory(tid, points.length);
		while (i < points.length - 1) {

			TPoint p1 = points[i];
			TPoint p2 = points[i + 1];
			double x = (p1.getX() + p2.getX()) / 2;
			double y = (p1.getY() + p2.getY()) / 2;

			double fociDistance = distanceFunction.distance(p1, p2);
			double majorAxis = 0;
			if(averageEllipseDistance > 0) {
				assert stdEllipseDistance > 0;
//				double stdFromGlobalAvg = (fociDistance - averageEllipseDistance) / stdEllipseDistance;
				double stdFromLocalAvg = (fociDistance - avg) / std;
				
				double base = stdLocalFromGlobal > 1 ? 120 : stdLocalFromGlobal < -1 ? 60 : 90;
				double multiplier = stdFromLocalAvg * stdLocalFromGlobal > 1 ? 0.66 : stdFromLocalAvg * stdLocalFromGlobal < -1 ? 1.33 : 1;
				majorAxis = Distance.lawOfSines(p1, p2, base * multiplier);
			} else {
				double previousEllipse = i == 0? 1 : distanceFunction.distance(points[i - 1], p1);
				double difference = (fociDistance / previousEllipse);
				double multiplier = (i == 0 ? 1 : Math.min((Math.log(10) / Math.log(difference)) + 1, 2.0));
				majorAxis = Distance.lawOfSines(p1, p2, 90 * multiplier);
			}

			double fociDistanceSquare = Math.pow(fociDistance, (double) 2);
			double majorAxisSquare = Math.pow(majorAxis, (double) 2);

			double minorAxis = Math.sqrt(majorAxisSquare - fociDistanceSquare);

			double angleO = Distance.angle(p1, p2);

			Ellipse e = new Ellipse();
			e.setEid(i);
			e.setCenter(new TPoint(x, y));
			e.setF1(p1);
			e.setF2(p2);
			e.setSemiMajorAxis(majorAxis / 2);
			e.setSemiMinorAxis(minorAxis / 2);
			e.setMajorAxis(majorAxis);
			e.setMinorAxis(minorAxis);
			e.setAngle(angleO);
			e.setEccentricity(fociDistance);
			e.setSemiMinorAxisSquare(Math.pow(e.getSemiMinorAxis(), (double) 2));
			e.setSemiMajorAxisSquare(Math.pow(e.getSemiMajorAxis(), (double) 2));
			e.setXi((e.getSemiMinorAxisSquare() + e.getSemiMajorAxisSquare()) / e.getMajorAxis());
			T.addEllipse(e);
			i++;
		}
		return T;
	}
	
	public static double sigmoid(double x) {
		return (1 / (1 + Math.pow(Math.E, (-1 * x))));
	}
	
	public ETrajectory createEllipticalTrajectoryFixed(int tid, TPoint[] points) {
		int i = 0;
		ETrajectory T = new ETrajectory(tid, points.length);
		while (i < points.length - 1) {

			TPoint p1 = points[i];
			TPoint p2 = points[i + 1];
			double x = (p1.getX() + p2.getX()) / 2;
			double y = (p1.getY() + p2.getY()) / 2;

			double fociDistance = distanceFunction.distance(p1, p2);
			double majorAxis = Distance.triangular(p1, p2) + 1;

			double fociDistanceSquare = fociDistance * fociDistance;
			double majorAxisSquare = majorAxis * majorAxis;

			double minorAxis = Math.sqrt(majorAxisSquare - fociDistanceSquare);

			double angleO = Distance.angle(p1, p2);

			Ellipse e = new Ellipse();
			e.setEid(i);
			e.setCenter(new TPoint(x, y));
			e.setF1(p1);
			e.setF2(p2);
			e.setSemiMajorAxis(majorAxis / 2);
			e.setSemiMinorAxis(minorAxis / 2);
			e.setMajorAxis(majorAxis);
			e.setMinorAxis(minorAxis);
			e.setAngle(angleO);
			e.setEccentricity(fociDistance);
			e.setSemiMinorAxisSquare(e.getSemiMinorAxis() * e.getSemiMinorAxis());
			e.setSemiMajorAxisSquare(e.getSemiMajorAxis() * e.getSemiMajorAxis());
			e.setXi((e.getSemiMinorAxisSquare() + e.getSemiMajorAxisSquare()) / e.getMajorAxis());
			T.addEllipse(e);
			i++;
		}
		return T;
	}
	
	public ETrajectory createUpperBoundEllipticalTrajectory(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid(), t.length());

		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);
			long timeDiff = (t.getPoint(i + 1).getTime() - t.getPoint(i)
					.getTime()) / 1000;
			
			double minDistance = distanceFunction.distance(p1, p2);
			double majorAxis = 55.5 * (timeDiff+1);// Tolerance=1s
			majorAxis=Math.max(majorAxis, minDistance);// Noise in real data									
			
			if (timeDiff > 0) {
				if (Distance.euclidean(p1, p2) > 0) {
					Ellipse e = createEllipseByFoci(i, p1, p2, majorAxis);
					T.addEllipse(e);
				}
			}
			i++;
		}
		return T;
	}

	private Ellipse createEllipseByFoci(int eid, TPoint p1, TPoint p2,
			double majorAxis) {
		
		double x = (p1.getX() + p2.getX()) / 2;
		double y = (p1.getY() + p2.getY()) / 2;

		double fociDistance = distanceFunction.distance(p1, p2);
		double fociDistanceSquare = fociDistance * fociDistance;
		double majorAxisSquare = majorAxis * majorAxis;

		double minorAxis = Math.sqrt(majorAxisSquare - fociDistanceSquare);

		double angle = Distance.angle(p1, p2);

		angle = -angle - Math.PI / 2;

		Ellipse e = new Ellipse();
		e.setEid(eid);
		e.setCenter(new TPoint(x,y));
		e.setF1(p1);
		e.setF2(p2);
		e.setMajorAxis(majorAxis);
		e.setMinorAxis(minorAxis);
		e.setAngle(angle);
		e.setEccentricity(fociDistance);
		return e;
	}
}
