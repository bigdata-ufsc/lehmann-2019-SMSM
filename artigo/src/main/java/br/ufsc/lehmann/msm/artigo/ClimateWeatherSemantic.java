package br.ufsc.lehmann.msm.artigo;

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
		List<Climate> climateA = (List<Climate>) a.getDimensionData(index, i);
		List<Climate> climateB = (List<Climate>) b.getDimensionData(index, j);
		return Sets.intersection(Sets.newHashSet(climateA), Sets.newHashSet(climateB)).size();
	}

}
