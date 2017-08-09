package br.ufsc.ftsm.util;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.ETrajectory;
import br.ufsc.ftsm.base.Ellipse;
import br.ufsc.utils.Distance;

public class CreateEllipseMath {
	
	public static ETrajectory createEllipticalTrajectoryFixed(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid(), t.length());
		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);
			double x = (p1.getX() + p2.getX()) / 2;
			double y = (p1.getY() + p2.getY()) / 2;

			double fociDistance = Distance.euclidean(p1, p2);
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
	
	public static ETrajectory createEllipticalTrajectoryFixed(int tid, TPoint[] points) {
		int i = 0;
		ETrajectory T = new ETrajectory(tid, points.length);
		while (i < points.length - 1) {

			TPoint p1 = points[i];
			TPoint p2 = points[i + 1];
			double x = (p1.getX() + p2.getX()) / 2;
			double y = (p1.getY() + p2.getY()) / 2;

			double fociDistance = Distance.euclidean(p1, p2);
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
		return T;
	}
	
	public static ETrajectory createUpperBoundEllipticalTrajectory(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid(), t.length());

		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);
			long timeDiff = (t.getPoint(i + 1).getTime() - t.getPoint(i)
					.getTime()) / 1000;
			
			double minDistance = Distance.euclidean(p1, p2);
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

	public static ETrajectory createManhattanEllipticalTrajectory(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid(), t.length());
		while (i < t.getPoints().size() - 1) {
			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);

			double majorAxis = Distance.manhattan(p1, p2)+1; //tolerance

			Ellipse e = createEllipseByFoci(i, p1, p2, majorAxis);

			T.addEllipse(e);
			i++;
		}

		return T;
	}

	private static Ellipse createEllipseByFoci(int eid, TPoint p1, TPoint p2,
			double majorAxis) {
		
		double x = (p1.getX() + p2.getX()) / 2;
		double y = (p1.getY() + p2.getY()) / 2;

		double fociDistance = Distance.euclidean(p1, p2);
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
