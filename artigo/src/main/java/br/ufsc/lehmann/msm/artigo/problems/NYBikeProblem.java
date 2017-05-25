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

	public NYBikeProblem() throws IOException, InterruptedException {
		data = new BikeDataReader().read();
		this.trainingData = data.subList(0, (int) (data.size() * (2.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (2.0 / 3)), data.size() - 1);
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
		return data;
	}

	@Override
	public Semantic discriminator() {
		return new ClimateWeatherSemantic(7);
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		return trainingData;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		return testingData;
	}

	@Override
	public String shortDescripton() {
		return "NYC Bike";
	}

}
