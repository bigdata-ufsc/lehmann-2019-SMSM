package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.method.SWALE;
import br.ufsc.lehmann.method.SWALE.SWALEParameters;
import br.ufsc.lehmann.msm.artigo.classifiers.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;

public class SWALEClassifier extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {

	private SWALE swale;
	
	public SWALEClassifier(SWALEParameters params) {
		swale = new SWALE(params);
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return swale.distance(t1, t2);
	}
	
	@Override
	public double getSimilarity(SemanticTrajectory t1, SemanticTrajectory t2) {
		return swale.getSimilarity(t1, t2);
	}

	@Override
	public String name() {
		return "SWALE";
	}
}
