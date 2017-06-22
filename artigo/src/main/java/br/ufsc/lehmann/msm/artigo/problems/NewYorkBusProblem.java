package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Problem;

public class NewYorkBusProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;

	public NewYorkBusProblem() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		data = new NewYorkBusDataReader().read();
//		data = data.subList(0, data.size() / 80);
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
	}

	@Override
	public Semantic[] semantics() {
		return new Semantic[] {
			 Semantic.GEOGRAPHIC_LATLON, //
//			 Semantic.TEMPORAL,//
			// NewYorkBusDataReader.OPERATOR,
			NewYorkBusDataReader.STOP_SEMANTIC
		};
	}

	@Override
	public List<SemanticTrajectory> data() {
		return data;
	}

	@Override
	public Semantic discriminator() {
		return NewYorkBusDataReader.ROUTE;
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
	public List<SemanticTrajectory> validatingData() {
		return validatingData;
	}

	@Override
	public String shortDescripton() {
		return "New York bus";
	}

}
