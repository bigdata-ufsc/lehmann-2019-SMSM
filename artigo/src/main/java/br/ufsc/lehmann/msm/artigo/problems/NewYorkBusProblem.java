package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.Problem;
import smile.math.Random;

public class NewYorkBusProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private String[] lines;
	private Random random = new Random();
	private StopSemantic stopSemantic;
	private boolean onlyStops;
	
	public NewYorkBusProblem(String... lines) {
		this(NewYorkBusDataReader.STOP_CENTROID_SEMANTIC, lines);
	}
	
	public NewYorkBusProblem(StopSemantic stopSemantic, String... lines) {
		this(stopSemantic, false, lines);
	}
	
	public NewYorkBusProblem(StopSemantic stopSemantic, boolean onlyStops, String... lines) {
		this.stopSemantic = stopSemantic;
		this.onlyStops = onlyStops;
		this.lines = lines;
	}
	
	@Override
	public void initialize(Random r) {
		if(!random.equals(r)) {
			random = r;
			loaded = false;
			load();
		}
	}

	@Override
	public List<SemanticTrajectory> data() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public Semantic discriminator() {
		return NewYorkBusDataReader.ROUTE;
	}
	
	public StopSemantic stopSemantic() {
		return stopSemantic;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		if(!loaded) {
			load();
		}
		return trainingData;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		if(!loaded) {
			load();
		}
		return testingData;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		if(!loaded) {
			load();
		}
		return validatingData;
	}

	@Override
	public String shortDescripton() {
		return "New York bus" + (lines != null ? "(lines=" + lines.length + ")" : "") + "[" + stopSemantic.name() + "][onlyStops=" + onlyStops + "]";
	}
	
	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new NewYorkBusDataReader(onlyStops).read(lines));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
		Collections.shuffle(data, new java.util.Random() {
			@Override
			public int nextInt(int bound) {
				return random.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return random.nextInt();
			}
		});
//		data = data.subList(0, data.size() / 80);
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		loaded = true;
	}

}
