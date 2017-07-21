package br.ufsc.lehmann.msm.artigo;

import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import smile.math.Random;

public interface Problem extends Cloneable {

	Semantic[] semantics();
	List<SemanticTrajectory> data();
	Semantic discriminator();
	List<SemanticTrajectory> trainingData();
	List<SemanticTrajectory> testingData();
	List<SemanticTrajectory> validatingData();
	
	String shortDescripton();
	
	Problem clone(Random r);
}
