package br.ufsc.ftsm.base;

import br.ufsc.core.trajectory.TPoint;

public class Ellipse {

	/**
	 * 
	 */
	private int eid;
//	private Timestamp startTime;
//	private Timestamp endTime;
	private TPoint center;
	private double majorAxis;
	private double minorAxis;
	private double angle;
	private double eccentricity;
	private TPoint f1;
	private TPoint f2;
	private double semiMajorAxis;
	private double semiMinorAxis;
	private double semiMinorAxisSquare;
	private double semiMajorAxisSquare;
	private double xi;
	
	public Ellipse(){
		
	}
	
	

	public int getEid() {
		return eid;
	}

	public void setEid(int eid) {
		this.eid = eid;
	}

//	public Timestamp getStartTime() {
//		return startTime;
//	}
//
//	public void setStartTime(Timestamp startTime) {
//		this.startTime = startTime;
//	}
//
//	public Timestamp getEndTime() {
//		return endTime;
//	}
//
//	public void setEndTime(Timestamp endTime) {
//		this.endTime = endTime;
//	}

	public TPoint getCenter() {
		return center;
	}



	public void setCenter(TPoint center) {
		this.center = center;
	}



	public double getMajorAxis() {
		return majorAxis;
	}



	public void setMajorAxis(double majorAxis) {
		this.majorAxis = majorAxis;
	}



	public double getMinorAxis() {
		return minorAxis;
	}



	public void setMinorAxis(double minorAxis) {
		this.minorAxis = minorAxis;
	}



	public TPoint getF1() {
		return this.f1;
	}



	public TPoint getF2() {
		return this.f2;
	}



	public void setF1(TPoint f1) {
		this.f1 = f1;
	}



	public void setF2(TPoint f2) {
		this.f2 = f2;
	}



	public double getAngle() {
		return angle;
	}



	public void setAngle(double angle) {
		this.angle = angle;
	}



	public double getEccentricity() {
		return eccentricity;
	}



	public void setEccentricity(double eccentricity) {
		this.eccentricity = eccentricity;
	}



	public void setSemiMajorAxis(double d) {
		this.semiMajorAxis=d;
		
	}
	
	public double getSemiMajorAxis() {
		return semiMajorAxis;
		
	}



	public double getSemiMinorAxis() {
		return semiMinorAxis;
	}



	public void setSemiMinorAxis(double semiMinorAxis) {
		this.semiMinorAxis = semiMinorAxis;
	}



	public double getXi() {
		return xi;
	}



	public void setXi(double xi) {
		this.xi = xi;
	}



	public double getSemiMinorAxisSquare() {
		return semiMinorAxisSquare;
	}



	public void setSemiMinorAxisSquare(double semiMinorAxisSquare) {
		this.semiMinorAxisSquare = semiMinorAxisSquare;
	}



	public double getSemiMajorAxisSquare() {
		return semiMajorAxisSquare;
	}



	public void setSemiMajorAxisSquare(double semiMajorAxisSquare) {
		this.semiMajorAxisSquare = semiMajorAxisSquare;
	}





	

}
