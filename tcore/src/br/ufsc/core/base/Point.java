package br.ufsc.core.base;
/**
 * 
 * @author Andre Salvaro Furtado
 *
 */
public class Point {
	
	
	protected double x;
	protected double y;
	
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}


	public double getX() {
		return x;
	}


	public void setX(double x) {
		this.x = x;
	}


	public double getY() {
		return y;
	}


	public void setY(double y) {
		this.y = y;
	}
	
	public String toWKT(){
		StringBuilder wkt = new StringBuilder();
		wkt.append("POINT (").append(this.x).append(" ").append(this.y).append(")");
		return wkt.toString();
		
	}
	
}
