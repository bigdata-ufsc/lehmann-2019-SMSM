package br.ufsc.lehmann;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.EqualsDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.core.trajectory.semantic.StopMove;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.StopMoveSemantic;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.stopandmove.EuclideanDistanceFunction;
import br.ufsc.lehmann.stopandmove.angle.AngleInference;
import br.ufsc.utils.Distance;
import smile.math.Random;

public class NElementProblem implements Problem {
	
	List<SemanticTrajectory> data;
	List<SemanticTrajectory> testing;
	List<SemanticTrajectory> validating;
	List<SemanticTrajectory> training;
	public static final BasicSemantic<Number> dataSemantic = new BasicSemantic<>(0);
	public static final BasicSemantic<String> discriminator = new BasicSemantic<>(3);
	public static final StopSemantic stop = new StopSemantic(4, new AttributeDescriptor<Stop, TPoint>(AttributeType.STOP_CENTROID, new EuclideanDistanceFunction()));
	
	public static final MoveSemantic move = new MoveSemantic(5, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_ANGLE, new AngleDistance()));
	public static final MoveSemantic move_distance = new MoveSemantic(5, new AttributeDescriptor<Move, Double>(AttributeType.MOVE_TRAVELLED_DISTANCE, new NumberDistance()));
	public static final MoveSemantic move_points = new MoveSemantic(5, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new DTWDistance(new EuclideanDistanceFunction(), 10)));
	public static final MoveSemantic move_ellipses = new MoveSemantic(5, new AttributeDescriptor<Move, TPoint[]>(AttributeType.MOVE_POINTS, new EllipsesDistance()));
	
	public static final StopMoveSemantic stopmove = new StopMoveSemantic(stop, move, new AttributeDescriptor<StopMove, Object>(AttributeType.STOP_STREET_NAME_MOVE_ANGLE, new EqualsDistanceFunction<Object>()));
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
			Move previousMove = null;
			for (int j = 0; j < 15; j++) {
				t.addData(j, dataSemantic, k * k);
				TPoint point = new TPoint(k + (j / 20.0), k + (j / 20.0));
				t.addData(j, Semantic.GEOGRAPHIC, point);
				t.addData(j, Semantic.TEMPORAL, new TemporalDuration(now.plus(j, ChronoUnit.MINUTES), now.plus(j + 1, ChronoUnit.MINUTES)));
				t.addData(j, discriminator, String.valueOf(k));
				long future = now.plus(j, ChronoUnit.MINUTES).toEpochMilli();
				int id = Integer.parseInt(i + "0" + j);
				if(j % 3 == 0) {
					if(startStop == null) {
						startStop = new Stop(id, j, nowMilli, 2, nowMilli, new TPoint(id, id));
					} else {
						startStop = endStop;
					}
					int endStopId = Integer.parseInt(i + "0" + (j + 2));
					endStop = new Stop(endStopId, j, nowMilli, 2, nowMilli, new TPoint(endStopId, endStopId));
					t.addData(j, stop, startStop);
					previousMove = null;
				} else {
					double angle = AngleInference.getAngle(startStop.getCentroid(), endStop.getCentroid());
					if(previousMove == null) {
						previousMove = new Move(id, startStop, endStop, nowMilli, future, j, 2,//
										new TPoint[] {point, new TPoint(k + ((j + 1) / 20.0), k + ((j + 1) / 20.0))},//
										angle, Distance.euclidean(startStop.getCentroid(), endStop.getCentroid()));
					}
					t.addData(j, move, previousMove);
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
	public void initialize(Random r) {
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
