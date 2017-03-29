package br.ufsc.core.light;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight Trajectory Class
 * @author André Salvaro Furtado
 *
 */
@SuppressWarnings("serial")
public class LTrajectory extends ArrayList<LPoint>{

	private final int tid;
	
	
	public LTrajectory(int tid){
		this.tid = tid;
	}
	
	//XXX Basic Methods for Compatibility
	public LPoint getPoint(int index){
		return this.get(index);
	}
	
	public List<LPoint> getPoints(){
		return this;
	}
	
	public int length(){
		return this.size();
	}

	public int getTid(){
		return this.tid;
	}
	
	public LPoint first(){
		return this.get(0);
	}
	
	public LPoint last(){
		return this.get(this.size()-1);
	}
}
