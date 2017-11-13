package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.AbstractProblem;

public class NYBikeProblem extends AbstractProblem {
	
	public NYBikeProblem() throws IOException, InterruptedException {
		super(null);
	}
	
	@Override
	protected List<SemanticTrajectory> load() {
		try {
			return new BikeDataReader().read();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Semantic discriminator() {
		return Semantic.TEMPORAL;
		//return BikeDataReader.WEATHER;
	}

	@Override
	public String shortDescripton() {
		return "NYC Bike";
	}

}
