package br.ufsc.lehmann.msm.artigo.problems;

import java.util.List;

import com.google.common.collect.Sets;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public class ClimateWeatherSemantic extends Semantic<List<Climate>, Number>{

	public ClimateWeatherSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return distance(a, i, b, j).intValue() == 1;
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		List<Climate> climateA = (List<Climate>) getData(a, i);
		List<Climate> climateB = (List<Climate>) getData(b, j);
		return distance(climateA, climateB);
	}
	
	@Override
	public double distance(List<Climate> d1, List<Climate> d2) {
		return Sets.intersection(Sets.newHashSet(d1), Sets.newHashSet(d2)).size();
	}

}
