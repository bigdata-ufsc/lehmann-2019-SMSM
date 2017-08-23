package br.ufsc.lehmann.prototype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.lehmann.msm.artigo.Problem;
import smile.math.Random;

public class PrototypeProblem implements Problem {

	private List<SemanticTrajectory> data;
	private boolean loaded;
	private Random random = new Random();
	private StopSemantic stopSemantic = PrototypeDataReader.STOP_SEMANTIC;

	public PrototypeProblem() {
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
		return PrototypeDataReader.USER_ID;
	}
	
	public StopSemantic stopSemantic() {
		return stopSemantic;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public String shortDescripton() {
		return "Prototype" + "[" + stopSemantic.name() + "]";
	}

	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new PrototypeDataReader().read());
		} catch (IOException e) {
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
		this.loaded = true;
	}

}
