package br.ufsc.utils;

import br.ufsc.core.trajectory.TPoint;

public class Angle {

	public static double getAngle(TPoint p1, TPoint p2) {
		double angle = Math.toDegrees(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));

		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}

}
