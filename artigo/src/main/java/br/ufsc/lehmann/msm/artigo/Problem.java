package br.ufsc.lehmann.msm.artigo;

import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public interface Problem {

	Semantic[] semantics();
	List<SemanticTrajectory> data();
	Semantic discriminator();
	List<SemanticTrajectory> trainingData();
	List<SemanticTrajectory> testingData();
	
	String shortDescripton();
}
