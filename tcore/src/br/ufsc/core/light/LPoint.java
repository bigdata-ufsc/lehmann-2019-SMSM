package br.ufsc.core.light;

import java.sql.Timestamp;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class LPoint {
	
	private double x,y;
	private Timestamp t;
	private int gid;

	public LPoint(int gid,double x, double y, Timestamp time) {
		this.x = x;
		this.y = y;
		this.t = time;
		this.gid=gid;
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
		
		LPoint p = (LPoint) obj;
		return this.x == p.x && this.y == p.y && this.t.equals(p.t);
	}
	

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

}
