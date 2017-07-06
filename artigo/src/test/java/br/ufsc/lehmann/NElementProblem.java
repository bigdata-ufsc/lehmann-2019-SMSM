package br.ufsc.lehmann;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.stopandmove.EuclideanDistanceFunction;

public class NElementProblem implements Problem {
	
	List<SemanticTrajectory> data;
	List<SemanticTrajectory> testing;
	List<SemanticTrajectory> validating;
	List<SemanticTrajectory> training;
	public static BasicSemantic<Number> dataSemantic = new BasicSemantic<>(0);
	public static BasicSemantic<String> discriminator = new BasicSemantic<>(3);
	public static StopSemantic stop = new StopSemantic(4, new EuclideanDistanceFunction());
	public static MoveSemantic move = new MoveSemantic(5);
	private int elements;
	private int classes;
	
	public NElementProblem(int elements, int classes) {
		this.elements = elements;
		this.classes = classes;
		if(elements < 1) {
			throw new IllegalArgumentException("No mínimo 1 elemento deve ser testado");
		}
		data = new ArrayList<>(elements);
		testing = new ArrayList<>();
		validating = new ArrayList<>();
		training = new ArrayList<>();
		for (int i = 0; i < elements; i++) {
			SemanticTrajectory t = new SemanticTrajectory(i, 6);
			int k = i%classes;
			Instant now = java.time.Instant.now();
			long nowMilli = now.toEpochMilli();
			Stop startStop = null, endStop = null;
			int initMove = -1;
			for (int j = 0; j < 15; j++) {
				t.addData(j, dataSemantic, k * k);
				t.addData(j, Semantic.GEOGRAPHIC, new TPoint(k + (j / 20.0), k + (j / 20.0)));
				t.addData(j, Semantic.TEMPORAL, new TemporalDuration(now.plus(j, ChronoUnit.MINUTES), now.plus(j + 1, ChronoUnit.MINUTES)));
				t.addData(j, discriminator, String.valueOf(k));
				long future = now.plus(j, ChronoUnit.MINUTES).toEpochMilli();
				if(j % 3 == 0) {
					if(startStop == null) {
						startStop = new Stop(t, j, nowMilli, nowMilli);
					} else {
						startStop = endStop;
					}
					endStop = new Stop(t, j + 2, nowMilli, nowMilli);
					t.addData(j, stop, startStop);
				} else {
					t.addData(j, move, new Move(t, j, startStop, endStop, nowMilli, future, initMove, 4));
				}
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
		if(validating.isEmpty()) {
			validating.add(data.get(0));
		}
	}

	@Override
	public Semantic[] semantics() {
		return new Semantic[] {
			dataSemantic,
			Semantic.GEOGRAPHIC,
			move
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
		return "Synthetic test problem(samples=" + elements + ", classes=" + classes + ")";
	}

}
