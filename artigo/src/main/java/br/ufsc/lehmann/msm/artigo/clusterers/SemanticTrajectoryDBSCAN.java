package br.ufsc.lehmann.msm.artigo.clusterers;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;

public class SemanticTrajectoryDBSCAN extends DBSCANClusterer<ClusterableTrajectory> {

	private IMeasureDistance<SemanticTrajectory> similarity;

	public SemanticTrajectoryDBSCAN(double epsilon, int minPts, IMeasureDistance<SemanticTrajectory> similarity) {
		super(epsilon, minPts);
		this.similarity = similarity;
	}
	
	@Override
	protected double distance(Clusterable p1, Clusterable p2) {
		if(p1 instanceof ClusterableTrajectory && p2 instanceof ClusterableTrajectory) {
			SemanticTrajectory t1 = ((ClusterableTrajectory) p1).getTrajectory();
			SemanticTrajectory t2 = ((ClusterableTrajectory) p2).getTrajectory();
			return similarity.distance(t1, t2);
		}
		return super.distance(p1, p2);
	}

}
