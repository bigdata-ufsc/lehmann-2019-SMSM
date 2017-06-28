package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Problem;

public class NYBikeProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;

	public NYBikeProblem() {
	}

	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new BikeDataReader().read();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		this.loaded = true;
	}

	@Override
	public Semantic[] semantics() {
		return new Semantic[] {
				Semantic.GEOGRAPHIC, //
				Semantic.TEMPORAL,//
				BikeDataReader.USER,//
				BikeDataReader.GENDER,//
				BikeDataReader.BIRTH_YEAR
		};
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
		return new ClimateWeatherSemantic(7);
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
		return "NYC Bike";
	}

}
