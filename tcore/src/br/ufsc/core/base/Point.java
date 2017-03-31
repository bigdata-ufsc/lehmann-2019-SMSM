package br.ufsc.core.base;

/**
 * 
 * @author Andre Salvaro Furtado
 *
 */
public class Point implements Comparable<Point> {

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

	public String toWKT() {
		StringBuilder wkt = new StringBuilder();
		wkt.append("POINT (").append(this.x).append(" ").append(this.y).append(")");
		return wkt.toString();

	}

	@Override
	public int compareTo(Point p) {
		if (p == null) {
			return 1;
		}
		if (p == this) {
			return 0;
		}
		if(this.equals(p)) {
			return 0;
		}
		double delta = (p.x * p.x + p.y * p.y) - (this.x * this.x + this.y * this.y);
		if (delta < 0)
			return -1;
		if (delta > 0)
			return +1;
		return 0;
	}

}
