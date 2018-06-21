package br.ufsc.core.trajectory;

import java.sql.Timestamp;

import br.ufsc.core.base.Point;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class TPoint extends Point {
	
	private Timestamp t;
	private long gid;
	private int occupation;
	
	public TPoint(long gid, double x, double y, Timestamp time, double transfX, double transfY) {
		super(x,y);
		this.t = time;
		this.gid=gid;
	}
	
	public TPoint(long gid,double x, double y, Timestamp time, double speed) {
		super(x,y);
		this.t = time;

		this.gid=gid;
	}
	
	public TPoint(long gid,double x, double y, Timestamp time) {
		super(x,y);
		this.t = time;
		this.gid=gid;
	}
	
	public TPoint(long gid,double x, double y, Timestamp time,int occupation) {
		super(x,y);
		this.t = time;
		this.gid=gid;
		this.occupation=occupation;
	}
	
	public TPoint(double x, double y, Timestamp time) {
		super(x,y);
		t = time;
	}
	
	public TPoint(double x, double y) {
		super(x,y);
	}
	
	public int getOccupation(){
		return this.occupation;
	}

	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public long getTime(){
		return this.t.getTime();
	}
	
	public Timestamp getTimestamp(){
		return this.t;
	}
	
	public double[] getCoord(){
		return new double[] {this.x, this.y};
	}
	
	public String getWKT(){
		StringBuilder wkt = new StringBuilder();
		wkt.append("POINT (").append(this.x).append(" ").append(this.y).append(")");
		return wkt.toString();
		
	}
	
//	public Geom2D getTransformedGeom(){
//		return transformedGeom;
//	}
	
	@Override
	public String toString() {
		return this.x + " " + this.y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
		
		TPoint p = (TPoint) obj;
		return this.x == p.x && this.y == p.y /*&& this.t.equals(p.t)*/;
	}
	
	@Override
	public int compareTo(Point p) {
		int compareTo = super.compareTo(p);
		if(compareTo != 0) {
			return compareTo;
		}
		if(!(p instanceof TPoint)) {
			return -1;
		}
		return t.compareTo(((TPoint) p).t);
	}
	
//	@Override
//	public int hashCode() {
//		return Double.valueOf(x).hashCode() ^ Double.valueOf(y).hashCode() ^ Double.valueOf(t.getTime()).hashCode();
//	}

	public long getGid() {
		return gid;
	}

	public void setGid(long gid) {
		this.gid = gid;
	}
	
//	public double getSpeed() {
//		return speed;
//	}
}
