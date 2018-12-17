package br.ufsc.lehmann.testexecution;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;

public class FilteredDataReader implements IDataReader {

	private IDataReader origin;
	private Semantic groundtruthSemantic;
	private int minTrajPerClass;

	public FilteredDataReader(IDataReader origin, Semantic groundtruth, int minTrajPerClass) {
		this.origin = origin;
		this.groundtruthSemantic = groundtruth;
		this.minTrajPerClass = minTrajPerClass;
	}

	@Override
	public List<SemanticTrajectory> read() {
		List<SemanticTrajectory> trajs = this.origin.read();
		Multimap<Object, SemanticTrajectory> m = MultimapBuilder.hashKeys().arrayListValues().build();
		trajs.stream().forEach(item -> {
	    	m.put(groundtruthSemantic.getData(item, 0), item);
	    });
		//only getting the trajectories of classes with less than 'minTrajPerClass' trajectories
		List<SemanticTrajectory> ret = m.asMap().entrySet().stream().filter(e -> {
			return e.getValue().size() < minTrajPerClass;
		}).flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
		
		//removing the groundtruth information
		ret.stream().forEach(t -> {
			for (int i = 0; i < t.length(); i++) {
				t.setDimensionData(i, groundtruthSemantic.getIndex(), null);
			}
		});
		return m.asMap().entrySet().stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
	}

}
