package br.ufsc.utils;

import java.sql.Timestamp;

import br.ufsc.core.base.Point;
import br.ufsc.core.trajectory.TPoint;

public class Distance {
	
	public static void main(String[] args) {
		
		
		
		TPoint p1 = new TPoint(1,0,0,new Timestamp(System.currentTimeMillis()));
		TPoint p2 = new TPoint(2,-1,-1,new Timestamp(System.currentTimeMillis()));
		
		
		System.out.println("### Angle: "+Math.toDegrees(angle(p1, p2)));
		
		showDistance(p1, p2);
		
		
		
		TPoint p3 = new TPoint(1,0,0,new Timestamp(System.currentTimeMillis()));
		TPoint p4 = new TPoint(2,0,1,new Timestamp(System.currentTimeMillis()));
		showDistance(p3, p4);
		
		TPoint p5 = new TPoint(1,0,0,new Timestamp(System.currentTimeMillis()));
		TPoint p6 = new TPoint(2,0.5,1,new Timestamp(System.currentTimeMillis()));
		showDistance(p5, p6);
		
		TPoint x = new TPoint(1,0,0,new Timestamp(System.currentTimeMillis()));
		TPoint y = new TPoint(2,0.0000000000000000000000000000000000000000001,0.00000000000000000000000000000000000000000000000000000000001,new Timestamp(System.currentTimeMillis()));
		TPoint z = new TPoint(1,999999999,999999999,new Timestamp(System.currentTimeMillis()));
		System.out.println(Math.sin(Math.toRadians(45)));
		System.out.println(triangular(x,z)+" < "+(triangular(x,y)+triangular(y,z)));
		
		Point a = new Point(-35.56666,-43.5555);
		Point b = new Point(-35.56666,43.5555);
		Point c = new Point(35.56666,43.5555);
		
		System.out.println(haversine(a, b));
		System.out.println(haversine(a, c));
		System.out.println(haversine(b, c));
		
	}
	
	private static void showDistance(TPoint p1, TPoint p2){
		System.out.println("### Angle: "+Math.toDegrees(angle(p1, p2)));
		System.out.println("# Euclidean: "+euclidean(p1,p2));
		System.out.println("# Manhattan: "+manhattan(p1,p2));
		System.out.println("# Chebyshev: "+chebyshev(p1,p2));
		System.out.println("# Canberra: "+canberra(p1,p2));
		System.out.println("# Triangular: "+triangular(p1,p2));
	}


	public static double euclidean(Point p1,Point p2){
		double distX = Math.abs(p1.getX()-p2.getX());
		double distXSquare = distX*distX;
		
		double distY = Math.abs(p1.getY()-p2.getY());
		double distYSquare = distY*distY;
		
		return Math.sqrt(distXSquare+distYSquare);
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
