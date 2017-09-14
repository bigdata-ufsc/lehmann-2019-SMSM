package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.Problem;
import smile.math.Random;

public class VehicleProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private Random random;
	private StopSemantic stopSemantic;
	private boolean onlyStops;

	public VehicleProblem() {
		this(VehicleDataReader.STOP_CENTROID_SEMANTIC);
	}

	public VehicleProblem(StopSemantic stopSemantic) {
		this(stopSemantic, false);
	}

	public VehicleProblem(StopSemantic stopSemantic, boolean onlyStops) {
		this.stopSemantic = stopSemantic;
		this.onlyStops = onlyStops;
		this.random = new Random();
	}
	
	@Override
	public void initialize(Random r) {
		if(!random.equals(r)) {
			random = r;
			loaded = false;
			load();
		}
	}

	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new VehicleDataReader(onlyStops).read());
		} catch (IOException | ParseException e) {
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
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		this.loaded = true;
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
		return VehicleDataReader.CLASS;
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
		return "Vehicle [" + stopSemantic.name() + "][onlyStops=" + onlyStops + "]";
	}

}
