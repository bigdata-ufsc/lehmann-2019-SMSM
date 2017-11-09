package br.ufsc.utils;

import br.ufsc.core.trajectory.GeographicDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;

public class LatLongDistanceFunction implements GeographicDistanceFunction {

	@Override
	public double distance(TPoint p, TPoint d) {
		return Distance.distFrom(p, d);
	}

	@Override
	public double length(SemanticTrajectory trajectory) {
		double ret = 0;
		for (int i = 0; i < trajectory.length() - 2; i++) {
			ret += distance(Semantic.GEOGRAPHIC_LATLON.getData(trajectory, i),
					Semantic.GEOGRAPHIC_LATLON.getData(trajectory, i + 1));
		}
		return ret;
	}

	@Override
	public double convert(double units) {
		return units;
	}

	@Override
	public double maxDistance() {
		return Double.MAX_VALUE;
	}

	@Override
	public TPoint[] convertToMercator(TPoint[] p) {
		TPoint[] u = new TPoint[p.length];
		for (int i = 0; i < p.length; i++) {
			double y = lat2y(p[i].getX());
			double x = lon2x(p[i].getY());

			u[i] = new TPoint(x, y);
		}
		return u;
	}

	public static final double RADIUS = 6378137.0;
    final private static double R_MAJOR = 6378137.0;
    final private static double R_MINOR = 6356752.3142;

	public static double lat2y(double aLat) {
		return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(aLat) / 2)) * RADIUS;
	}

	public static double lon2x(double aLong) {
		return Math.toRadians(aLong) * RADIUS;
	}
	
    public double[] merc(double x, double y) {
        return new double[] {mercX(x), mercY(y)};
    }

    private double  mercX(double lon) {
        return R_MAJOR * Math.toRadians(lon);
    }

    private double mercY(double lat) {
        if (lat > 89.5) {
            lat = 89.5;
        }
        if (lat < -89.5) {
            lat = -89.5;
        }
        double temp = R_MINOR / R_MAJOR;
        double es = 1.0 - (temp * temp);
        double eccent = Math.sqrt(es);
        double phi = Math.toRadians(lat);
        double sinphi = Math.sin(phi);
        double con = eccent * sinphi;
        double com = 0.5 * eccent;
        con = Math.pow(((1.0-con)/(1.0+con)), com);
        double ts = Math.tan(0.5 * ((Math.PI*0.5) - phi))/con;
        double y = 0 - R_MAJOR * Math.log(ts);
        return y;
    }
}
