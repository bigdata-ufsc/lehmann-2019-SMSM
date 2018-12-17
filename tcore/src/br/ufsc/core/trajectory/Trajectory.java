package br.ufsc.core.trajectory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class Trajectory {

	private final Object tid;
	private List<TPoint> points;
	
	public Trajectory(SemanticTrajectory st){
		this.tid = st.getTrajectoryId();
		this.points=new ArrayList<TPoint>();
		for (int i = 0; i < st.length(); i++) {
			this.addPoint(Semantic.SPATIAL.getData(st, i));
		}
	}
	
	public Trajectory(Object tid){
		this.tid = tid;
		this.points=new ArrayList<TPoint>();
	}
	
	public void addPoint(int gid, double x, double y, Timestamp t, double transfX, double transfY){
		this.points.add(new TPoint(gid,x, y, t, transfX, transfY));
	}
	
	public void addPoint(int gid, double x, double y, Timestamp t, double speed){
		this.points.add(new TPoint(gid,x, y, t, speed));
	}
	
	public void addPoint(TPoint p){
		this.points.add(p);
	}

	public TPoint getPoint(int index){
		return this.points.get(index);
	}
	
	public List<TPoint> getPoints(){
		return this.points;
	}
	
	public int length(){
		return this.points.size();
	}

	public Object getTid(){
		return this.tid;
	}
	
	public TPoint first(){
		return this.points.get(0);
	}
	
	public TPoint last(){
		return this.points.get(this.points.size()-1);
	}
	
	@Override
	public String toString() {
		return this.toString(0, this.points.size());
	}
	
	public String toString(int from, int to){
		String  ret = "";
		
		for(int i=from; i<=to; i++)
			ret += this.points.get(i).toString() + ",";
		ret = ret.substring(0,ret.length()-1);
		
		return ret;
	}
	
	public Trajectory clone(){
		Trajectory T = new Trajectory(this.tid);
		for (TPoint p : this.getPoints()){
			T.addPoint(p);
		}
		return T;
	}

	public SemanticTrajectory toSemanticTrajectory() {
		return new SemanticTrajectory(this);
	}

}
