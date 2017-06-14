package br.ufsc.lehmann.method;

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
	private DTWi dtWi;
	private DTWd dtWd;

	public DTWa(double threshold, Semantic<?, Number>... semantics) {
		this.semantics = semantics;
		this.threshold = threshold;
		dtWi = new DTWi(semantics);
		dtWd = new DTWd(semantics);
	}
	
	public void training(List<DataEntry<SemanticTrajectory>> trajectories) {
		dwtiNN = new NearestNeighbour<SemanticTrajectory>(trajectories, Math.min(trajectories.size(), 3), new DTWiMeasurer(semantics));
		dwtdNN = new NearestNeighbour<SemanticTrajectory>(trajectories, Math.min(trajectories.size(), 3), new DTWdMeasurer(semantics));
	}

	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		double distanceI = dwtiNN.distance(new DataEntry<SemanticTrajectory>(t1, null), new DataEntry<SemanticTrajectory>(t2, null));
		double distanceD = dwtdNN.distance(new DataEntry<SemanticTrajectory>(t1, null), new DataEntry<SemanticTrajectory>(t2, null));
		if(distanceD / distanceI > threshold) {
			return dtWi.getSimilarity(t1, t2);
		} else {
			return dtWd.getSimilarity(t1, t2);
		}
	}

	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		double distanceI = dwtiNN.distance(new DataEntry<SemanticTrajectory>(t1, null), new DataEntry<SemanticTrajectory>(t2, null));
		double distanceD = dwtdNN.distance(new DataEntry<SemanticTrajectory>(t1, null), new DataEntry<SemanticTrajectory>(t2, null));
		if(distanceD / distanceI > threshold) {
			return dtWi.distance(t1, t2);
		} else {
			return dtWd.distance(t1, t2);
		}
	}

	private final class DTWiMeasurer implements IMeasureDistance<SemanticTrajectory> {
		
		private Semantic<?, Number>[] semantics;

		DTWiMeasurer(Semantic<?, Number>... semantics) {
			this.semantics = semantics;
		}
		
		@Override
		public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
			return dtWi.distance(t1, t2);
		}

		@Override
		public String name() {
			return "DTWi";
		}
	}

	private final class DTWdMeasurer implements IMeasureDistance<SemanticTrajectory> {
		
		private Semantic<?, Number>[] semantics;

		DTWdMeasurer(Semantic<?, Number>... semantics) {
			this.semantics = semantics;
		}
		
		@Override
		public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
			return dtWd.distance(t1, t2);
		}

		@Override
		public String name() {
			return "DTWd";
		}
	}
}
