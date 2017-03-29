package br.ufsc.ftsm.base;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Trajectory;



public class ETrajectory {
	private final int tid;
	private List<Ellipse> ellipses;
	private Trajectory T;
	
	public ETrajectory(int tid){
		this.tid = tid;
		this.ellipses=new ArrayList<Ellipse>();
	}
	
	public void addEllipse(Ellipse e){
		this.ellipses.add(e);
	}
	
	public Ellipse getEllipse(int index){
		return this.ellipses.get(index);
	}
	
	public List<Ellipse> getEllipses(){
		return this.ellipses;
	}

	
	public int length(){
		return this.ellipses.size();
	}

	public int getTid(){
		return this.tid;
	}

	public Trajectory getT() {
		return T;
	}

	public void setT(Trajectory t) {
		T = t;
	}
}
