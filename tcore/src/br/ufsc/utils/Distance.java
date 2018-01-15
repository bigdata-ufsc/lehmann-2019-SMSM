package br.ufsc.utils;

import br.ufsc.core.base.Point;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.TPoint;

public class Distance {
	
	public static final double EARTH_RADIUS = 6371000d;

	public static double getDistance(TPoint[] points, SpatialDistanceFunction func) {
		double ret = 0;
		for (int i = 0; i < points.length - 1; i++) {
			ret += func.distance(points[i], points[i + 1]);
		}
	    return ret;
	}

	public static double euclidean(double[] p1, double[] p2){
		double distX = Math.abs(p1[0]-p2[0]);
		double distXSquare = distX*distX;
		
		double distY = Math.abs(p1[1]-p2[1]);
		double distYSquare = distY*distY;
		
		return Math.sqrt(distXSquare+distYSquare);
	}

	public static double euclidean(Point p1,Point p2){
		double distX = Math.abs(p1.getX()-p2.getX());
		double distXSquare = distX*distX;
		
		double distY = Math.abs(p1.getY()-p2.getY());
		double distYSquare = distY*distY;
		
		return Math.sqrt(distXSquare+distYSquare);
	}
	
	public static float distFrom(Point p1,Point p2) {
		 // meters
		double dLat = Math.toRadians(p2.getX() - p1.getX());
		double dLng = Math.toRadians(p2.getY() - p2.getY());
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(p1.getX())) * Math.cos(Math.toRadians(p2.getX())) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (EARTH_RADIUS * c);

		return dist;
	}
	
	public static double sqEuclidean(Point p1,Point p2){
		double distX = Math.abs(p1.getX()-p2.getX());
		double distXSquare = distX*distX;
		
		double distY = Math.abs(p1.getY()-p2.getY());
		double distYSquare = distY*distY;
		
		return distXSquare+distYSquare;
	}
	
	public static double manhattan(Point p1,Point p2){
		return Math.abs(p2.getX()-p1.getX()) + Math.abs(p2.getY()-p1.getY());
	}
	
	public static double minkowski(Point p1,Point p2,double factor){	
		return Math.pow((Math.pow(Math.abs(p2.getX()-p1.getX()),factor) + Math.pow(Math.abs(p2.getY()-p1.getY()),factor)),(1/factor));
	}
	
	public static double chebyshev(Point p1,Point p2){
		return Math.max(Math.abs(p2.getX()-p1.getX()),Math.abs(p2.getY()-p1.getY()));
	}
	
	public static double triangular(Point p1, Point p2){
		//double h = euclidean(p1, p2);
		double distX = Math.abs(p1.getX()-p2.getX());
		double distXSquare = distX*distX;
		
		double distY = Math.abs(p1.getY()-p2.getY());
		double distYSquare = distY*distY;
		//return (h*1.41421356237);
		return Math.sqrt(2*(distXSquare+distYSquare));
		//return (h*Math.sin(Math.toRadians(45)))*2;
	}
	
	public static double retangular(Point p1,Point p2){
		double distX = Math.abs(p1.getX()-p2.getX());
		double distXSquare = distX*distX;
		
		double distY = Math.abs(p1.getY()-p2.getY());
		double distYSquare = distY*distY;
		
		double euclidean = Math.sqrt(distXSquare+distYSquare);
		double side = euclidean*(Math.sqrt(2)/2);
		
		double height= Math.sqrt((euclidean*euclidean)+(side*side));
		
//		System.out.println(h*Math.sin(Math.toRadians(80))+h*Math.sin(Math.toRadians(10)));
//		System.out.println(h*Math.sin(Math.toRadians(60))+h*Math.sin(Math.toRadians(30)));
//		System.out.println(h*Math.sin(Math.toRadians(45))+h*Math.sin(Math.toRadians(45)));
		
		//return (h*1.41421356237);
		return 2*height+euclidean;
	}
	
	public static double time(TPoint p1, TPoint p2){
		return (p2.getTimestamp().getTime()-p1.getTimestamp().getTime())/1000;
	}
	
	public static double canberra(TPoint p1,TPoint p2){
		double x1 = Math.abs(p1.getX()-p2.getX())+1;
		double x2 = Math.abs(p1.getX()+p2.getX())+1;
		
		double y1 = Math.abs(p1.getY()-p2.getY())+1;
		double y2 = Math.abs(p1.getY()+p2.getY())+1;
		
		
		return (x1/x2)+(y1/y2);
	}
	
	public static double angle(TPoint p1,TPoint p2){
		return Math.atan2(p2.getX() - p1.getX(), p2.getY()-p1.getY());
	}
	
	// Haversine distance between two LonLat points in Meters
	public static double haversine(Point p1,Point p2){
		final int R = 6371000; //Earth mean radius in meters
		double lat1 = Math.toRadians(p1.getY());
		double lat2 = Math.toRadians(p2.getY());
		double latDiff = Math.toRadians(p2.getY()-p1.getY());
		double lonDiff = Math.toRadians(p2.getX()-p1.getX());

		double A = Math.sin(latDiff/2) * Math.sin(latDiff/2) +
		        Math.cos(lat1) * Math.cos(lat2) *
		        Math.sin(lonDiff/2) * Math.sin(lonDiff/2);
		
		double B = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1-A));

		return R * B;

	}
	
	public static double pointToLineAB(TPoint p, TPoint A, TPoint B) {
        double dist = crossProduct(A, B, p) / euclidean(A, B);

        double dot1 = dotProduct(A, B, p);
        if (dot1 > 0)
            return euclidean(B, p);

        double dot2 = dotProduct(B, A, p);
        if (dot2 > 0) {
            return euclidean(A, p);
        }
        return Math.abs(dist);
    }

    private static double dotProduct(TPoint A, TPoint B, TPoint C) {
        double[] AB = new double[2];
        double[] BC = new double[2];
        AB[0] = B.getX() - A.getX();
        AB[1] = B.getY() - A.getY();
        BC[0] = C.getX() - B.getX();
        BC[1] = C.getY() - B.getY();
        double dot = AB[0] * BC[0] + AB[1] * BC[1];

        return dot;
    }

    private static double crossProduct(TPoint A, TPoint B, TPoint C) {
        double[] AB = new double[2];
        double[] AC = new double[2];
        AB[0] = B.getX() - A.getX();
        AB[1] = B.getY() - A.getY();
        AC[0] = C.getX() - A.getX();
        AC[1] = C.getY() - A.getY();
        double cross = AB[0] * AC[1] - AB[1] * AC[0];

        return cross;
    }
	
	
}
