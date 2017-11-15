package br.ufsc.lehmann.msm.artigo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;

public abstract class AbstractProblem implements Problem {
	private boolean loaded;
	private Random random = new Random();
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private StopSemantic stopSemantic;
	
	public AbstractProblem(StopSemantic stopSemantic) {
		this.stopSemantic = stopSemantic;

		random = new Random() {
			smile.math.Random rnd = new smile.math.Random();
			@Override
			public int nextInt(int bound) {
				return rnd.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return rnd.nextInt();
			}
		};
	}

	@Override
	public List<SemanticTrajectory> data() {
		if(!loaded) {
			_load();
		}
		return data;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		if(!loaded) {
			_load();
		}
		return trainingData;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		if(!loaded) {
			_load();
		}
		return testingData;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		if(!loaded) {
			_load();
		}
		return validatingData;
	}
	
	@Override
	public List<SemanticTrajectory> balancedData() {
		List<SemanticTrajectory> allData = data();
		Multimap<Object, SemanticTrajectory> classifiedTrajs = MultimapBuilder.hashKeys().arrayListValues().build();
		Semantic semantic = discriminator();
		for (SemanticTrajectory traj : allData) {
			classifiedTrajs.put(semantic.getData(traj, 0), traj);
		}
		int c = Integer.MAX_VALUE;
		Map<Object, Collection<SemanticTrajectory>> asMap = classifiedTrajs.asMap();
		
		for (Map.Entry<Object, Collection<SemanticTrajectory>> entry: asMap.entrySet()) {
			if(entry.getValue().size() < c) {
				c = entry.getValue().size();
			}
		}
		List<SemanticTrajectory> ret = new ArrayList<>(asMap.size() * c);
		for (Map.Entry<Object, Collection<SemanticTrajectory>> entry : asMap.entrySet()) {
			ret.addAll(entry.getValue().stream().collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
				Collections.shuffle(list, AbstractProblem.this.random);
				return list;
			})).stream().limit(c).collect(Collectors.toList()));
		}
		Collections.shuffle(ret, this.random);
		return ret;
	}

	@Override
	public void initialize(smile.math.Random r) {
		if(!random.equals(r)) {
			random = new Random() {
				@Override
				public int nextInt(int bound) {
					return r.nextInt(bound);
				}
				
				@Override
				public int nextInt() {
					return r.nextInt();
				}
			};
			loaded = false;
			_load();
		}
	}
	
	public StopSemantic stopSemantic() {
		return stopSemantic;
	}

	private List<SemanticTrajectory> _load() {
		if(loaded) {
			return data;
		}

		data = this.load();
		Collections.shuffle(data, random);
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		this.loaded = true;
		return data;
	}
	
	protected abstract List<SemanticTrajectory> load();

}
