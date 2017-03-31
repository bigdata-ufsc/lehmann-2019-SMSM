package br.ufsc.lehmann.method;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class DTWa extends TrajectorySimilarityCalculator<SemanticTrajectory> {
	
	private NearestNeighbour<SemanticTrajectory> dwtiNN;
	private NearestNeighbour<SemanticTrajectory> dwtdNN;
	private Semantic<?, Number>[] semantics;
	private double threshold;

	public DTWa(double threshold, Semantic<?, Number>... semantics) {
		this.semantics = semantics;
		this.threshold = threshold;
	}
	
	public void training(List<DataEntry<SemanticTrajectory>> trajectories) {
		dwtiNN = new NearestNeighbour<SemanticTrajectory>(trajectories, Math.min(trajectories.size(), 3), new DTWiMeasurer(semantics));
		dwtdNN = new NearestNeighbour<SemanticTrajectory>(trajectories, Math.min(trajectories.size(), 3), new DTWdMeasurer(semantics));
	}

	@Override
	public double getDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		double distanceI = dwtiNN.distance(new DataEntry<SemanticTrajectory>(t1, null), new DataEntry<SemanticTrajectory>(t2, null));
		double distanceD = dwtdNN.distance(new DataEntry<SemanticTrajectory>(t1, null), new DataEntry<SemanticTrajectory>(t2, null));
		if(distanceD / distanceI > threshold) {
			return new DTWi(semantics).getDistance(t1, t2);
		} else {
			return new DTWd(semantics).getDistance(t1, t2);
		}
	}

	private static final class DTWiMeasurer implements IMeasureDistance<SemanticTrajectory> {
		
		private Semantic<?, Number>[] semantics;

		DTWiMeasurer(Semantic<?, Number>... semantics) {
			this.semantics = semantics;
		}
		
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new DTWi(semantics).getDistance(t1.getX(), t2.getX());
		}
	}

	private static final class DTWdMeasurer implements IMeasureDistance<SemanticTrajectory> {
		
		private Semantic<?, Number>[] semantics;

		DTWdMeasurer(Semantic<?, Number>... semantics) {
			this.semantics = semantics;
		}
		
		@Override
		public double distance(DataEntry<SemanticTrajectory> t1, DataEntry<SemanticTrajectory> t2) {
			return new DTWd(semantics).getDistance(t1.getX(), t2.getX());
		}
	}
}
