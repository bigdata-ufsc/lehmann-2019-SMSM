package br.ufsc.core.trajectory;

import java.sql.Timestamp;

import br.ufsc.core.base.Point;
/**
 * 
 * @author André Lehmann
 *
 */
public class ThreeDimensionalPoint extends TPoint {
	
	private double z;

	public ThreeDimensionalPoint(long gid,double x, double y, double z, Timestamp time) {
		super(gid, x, y, time);
		this.z = z;
	}
	
	public ThreeDimensionalPoint(double x, double y, double z, Timestamp time) {
		super(x, y, time);
		this.z = z;
	}
	
	public ThreeDimensionalPoint(double x, double y, double z) {
		super(x,y);
		this.z = z;
	}
	
	public double getZ() {
		return z;
	}
	
	public double[] getCoord(){
		return new double[] {this.x, this.y, this.z};
	}
	
	public String getWKT(){
		StringBuilder wkt = new StringBuilder();
		wkt.append("POINT (").append(this.x).append(" ").append(this.y).append(" ").append(this.z).append(")");
		return wkt.toString();
	}
	
	@Override
	public String toString() {
		return this.x + " " + this.y + " " + this.z;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
		
		ThreeDimensionalPoint p = (ThreeDimensionalPoint) obj;
		return this.x == p.x && this.y == p.y && this.z == p.z;
	}

	public TPoint to2D() {
		return new TPoint(this.getGid(), this.x, this.y, this.getTimestamp());
	}
	
}
