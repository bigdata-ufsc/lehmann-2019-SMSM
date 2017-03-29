package br.ufsc.ftsm.util;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.ftsm.base.ETrajectory;
import br.ufsc.ftsm.base.Ellipse;
import br.ufsc.utils.Distance;

public class CreateEllipseMath {
	
	public static ETrajectory createEllipticalTrajectoryFixed(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid());
	//	System.out.println("tLast: "+t.getPoint(t.length()-1).getWKT());
		//t = getMovementTrajectory2(t);
	//	System.out.println("tLastMov: "+t.getPoint(t.length()-1).getWKT());
		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);
//System.out.println(p2.getWKT());
				double x = (p1.getX() + p2.getX()) / 2;
				double y = (p1.getY() + p2.getY()) / 2;

				double fociDistance = Distance.euclidean(p1, p2);
				double majorAxis = Distance.triangular(p1, p2)+1;

				double fociDistanceSquare = fociDistance * fociDistance;
				double majorAxisSquare = majorAxis * majorAxis;

				double minorAxis = Math.sqrt(majorAxisSquare
						- fociDistanceSquare);

				double angleO = Distance.angle(p1, p2);
				

		//		double angle = -angleO - Math.PI / 2;

//				Coordinate center = new Coordinate(x, y);
//
//				gsf.setCentre(center);
//				gsf.setWidth(majorAxis);
//				// double minorAxis2 = Math.min(200, minorAxis);
//				gsf.setHeight(minorAxis);
//				gsf.setNumPoints(16);
//				Polygon ellipse = gsf.createCircle();
//
//				AffineTransformation trans = AffineTransformation
//						.rotationInstance(angle, x, y);
//
//				ellipse.apply(trans);

			//	if (i > 0 && T.length() >= i) {
	
						Ellipse e = new Ellipse();
						e.setEid(i);
						//e.setStartTime(p1.getTimestamp());
						//e.setEndTime(p2.getTimestamp());
						//e.setGeom(ellipse);
						// e.setPreparedGeom(preparedEllipse);
						e.setCenter(new TPoint(x,y));
						e.setF1(p1);
						e.setF2(p2);
						e.setSemiMajorAxis(majorAxis/2);
						e.setSemiMinorAxis(minorAxis/2);
						e.setMajorAxis(majorAxis);
						e.setMinorAxis(minorAxis);
						e.setAngle(angleO);
						e.setEccentricity(fociDistance);
						e.setSemiMajorAxisSquare(e.getSemiMinorAxis() * e.getSemiMinorAxis());
						e.setSemiMajorAxisSquare (e.getSemiMajorAxis() * e.getSemiMajorAxis());
						e.setXi((e.getSemiMinorAxisSquare() + e.getSemiMajorAxisSquare()) / e.getMajorAxis());
						T.addEllipse(e);
			//	}
			i++;
		}
	//	System.out.println(t.size());
	//	System.out.println(T.length());
		T.setT(t);
		
		return T;
	}
	
	public static ETrajectory createUpperBoundEllipticalTrajectory(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid());
		//Trajectory t = getMovementTrajectory(T2);

		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);
			long timeDiff = (t.getPoint(i + 1).getTime() - t.getPoint(i)
					.getTime()) / 1000;
			
			double minDistance = Distance.euclidean(p1, p2);
			double majorAxis = 55.5 * (timeDiff+1);// Tolerance=1s
			majorAxis=Math.max(majorAxis, minDistance);// Noise in real data									
			
			if (timeDiff > 0) {
				Ellipse e;

				if (Distance.euclidean(p1, p2) > 0) {
					//e = createCircle(i, p1, p2, majorAxis);
				//} else {
					e = createEllipseByFoci(i, p1, p2, majorAxis);
					T.addEllipse(e);
				}
			}

			i++;
		}

		return T;
	}

	public static ETrajectory createManhattanEllipticalTrajectory(Trajectory t) {
		int i = 0;
		ETrajectory T = new ETrajectory(t.getTid());
	//	Trajectory t = getMovementTrajectory(T2);

		while (i < t.getPoints().size() - 1) {

			TPoint p1 = t.getPoint(i);
			TPoint p2 = t.getPoint(i + 1);

			double majorAxis = Distance.manhattan(p1, p2)+1; //tolerance
			// Math.max(10*timeDiff,fociDistance);

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

//		Coordinate center = new Coordinate(x, y);
//
//		gsf.setCentre(center);
//		gsf.setWidth(majorAxis);
//		gsf.setHeight(minorAxis);
//		gsf.setNumPoints(16);
//		Polygon ellipse = gsf.createCircle();
//
//		AffineTransformation trans = AffineTransformation.rotationInstance(
//				angle, x, y);
//
//		ellipse.apply(trans);

		Ellipse e = new Ellipse();
		e.setEid(eid);
//		e.setStartTime(p1.getTimestamp());
//		e.setEndTime(p2.getTimestamp());
		//e.setGeom(ellipse);
		// e.setPreparedGeom(preparedEllipse);
		e.setCenter(new TPoint(x,y));
		e.setF1(p1);
		e.setF2(p2);
		// e.setSemiMajorAxis(majorAxis/2);
		e.setMajorAxis(majorAxis);
		e.setMinorAxis(minorAxis);
		e.setAngle(angle);
		e.setEccentricity(fociDistance);
	//	T.addEllipse(e);
		//e.setGeom(ellipse);
		return e;
	}
	

	
	private static Trajectory getMovementTrajectory2(Trajectory T) {
		Trajectory T2 = new Trajectory(T.getTid());
		int len = T.length() - 1;
		int i = 1;

		T2.addPoint(T.getPoint(0));
		TPoint targetPoint = T.getPoint(0);
		while (i <= len) {
			double distance = Distance.euclidean(T.getPoint(i), targetPoint);
			 if (distance > 0) {
				T2.addPoint(T.getPoint(i));
				targetPoint = T.getPoint(i);
			}
			i++;
		}
		if (T2.length()<2){
			T2.addPoint(T.getPoint(len));
		}
		return T2;
	}
	
	


}
