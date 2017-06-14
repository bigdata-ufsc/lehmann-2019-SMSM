package br.ufsc.lehmann;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;

public class TestProblem implements Problem {
	
	List<SemanticTrajectory> data;
	List<SemanticTrajectory> testing;
	List<SemanticTrajectory> validating;
	List<SemanticTrajectory> training;
	BasicSemantic<Number> dataSemantic = new BasicSemantic<>(0);
	BasicSemantic<String> discriminator = new BasicSemantic<>(2);
	
	public TestProblem() {
		data = new ArrayList<>(15);
		testing = new ArrayList<>(5);
		validating = new ArrayList<>(5);
		training = new ArrayList<>(5);
		for (int i = 0; i < 15; i++) {
			SemanticTrajectory t = new SemanticTrajectory(i, 3);
			int k = i%5;
			for (int j = 0; j < 5; j++) {
				t.addData(j, dataSemantic, k * k);
				t.addData(j, Semantic.GEOGRAPHIC, new TPoint(k + (j / 10.0), k + (j / 10.0)));
				t.addData(j, discriminator, String.valueOf(k));
			}
			data.add(t);
			if (i % 3 == 0) {
				training.add(t);
			} else if (i % 3 == 1) {
				testing.add(t);
			} else if (i % 3 == 2) {
				validating.add(t);
			}
		}
	}

	@Override
	public Semantic[] semantics() {
		return new Semantic[] {
			dataSemantic,
			Semantic.GEOGRAPHIC
		};
	}

	@Override
	public Semantic discriminator() {
		return discriminator;
	}

	@Override
	public List<SemanticTrajectory> data() {
		return data;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		return training;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		return testing;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		return validating;
	}

	@Override
	public String shortDescripton() {
		return "Test problem";
	}

}
