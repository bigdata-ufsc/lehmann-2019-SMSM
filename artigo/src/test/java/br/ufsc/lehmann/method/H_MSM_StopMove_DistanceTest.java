package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.lehmann.H_MSM_StopMove;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.H_MSM_StopMove_Classifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface H_MSM_StopMove_DistanceTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new H_MSM_StopMove_Classifier(//
						new H_MSM_StopMove.H_MSM_MoveSemanticParameters(NElementProblem.move_distance, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
								new H_MSM_StopMove.H_MSM_DimensionParameters<>(NElementProblem.move_distance, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
							}),
						new H_MSM_StopMove.H_MSM_StopSemanticParameters(NElementProblem.stop, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
								new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC, AttributeType.STOP_GEOGRAPHIC, 0.5, 1.0/2.0),
								new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/2.0)
							})
						);
		} else if(problem instanceof NewYorkBusProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(NewYorkBusDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(NewYorkBusDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(((NewYorkBusProblem) problem).stopSemantic(), new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_LATLON, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(((NewYorkBusProblem) problem).stopSemantic(), AttributeType.STOP, Thresholds.STOP_CENTROID_LATLON, 1.0/3.0)
						})
					);
		} else if(problem instanceof DublinBusProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(DublinBusDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(DublinBusDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(((DublinBusProblem) problem).stopSemantic(), new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_LATLON, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(((DublinBusProblem) problem).stopSemantic(), AttributeType.STOP, Thresholds.STOP_CENTROID_LATLON, 1.0/3.0)
						})
					);
		} else if(problem instanceof PatelProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(PatelDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(PatelDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(((PatelProblem) problem).stopSemantic(), new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_EUCLIDEAN, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(((PatelProblem) problem).stopSemantic(), AttributeType.STOP, Thresholds.STOP_CENTROID_EUCLIDEAN, 1.0/3.0)
						})
					);
		} else if(problem instanceof VehicleProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(PatelDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(PatelDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(((VehicleProblem) problem).stopSemantic(), new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_EUCLIDEAN, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_EUCLIDEAN, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(((VehicleProblem) problem).stopSemantic(), AttributeType.STOP, Thresholds.STOP_CENTROID_EUCLIDEAN, 1.0/3.0)
						})
					);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(SanFranciscoCabDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(SanFranciscoCabDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(((SanFranciscoCabProblem) problem).stopSemantic(), new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_LATLON, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(((SanFranciscoCabProblem) problem).stopSemantic(), AttributeType.STOP, Thresholds.STOP_CENTROID_LATLON, 1.0/3.0)
						})
					);
		} else if(problem instanceof SergipeTracksProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(SergipeTracksDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(SergipeTracksDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_LATLON, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(SergipeTracksDataReader.STOP_CENTROID_SEMANTIC, AttributeType.STOP, Thresholds.STOP_CENTROID_LATLON, 1.0/3.0)
						})
					);
		} else if(problem instanceof PrototypeProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(PrototypeDataReader.MOVE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(PrototypeDataReader.MOVE_SEMANTIC, AttributeType.MOVE, null, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(PrototypeDataReader.STOP_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_EUCLIDEAN, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_PROTOTYPE, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(PrototypeDataReader.STOP_SEMANTIC, AttributeType.STOP, null, 1.0/3.0)
						})
					);
		} else if(problem instanceof PisaProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(PisaDataReader.MOVE_DISTANCE_SEMANTIC, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(PisaDataReader.MOVE_DISTANCE_SEMANTIC, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(((PisaProblem) problem).stopSemantic(), new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC_LATLON, AttributeType.STOP_GEOGRAPHIC, Thresholds.GEOGRAPHIC_LATLON, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(((PisaProblem) problem).stopSemantic(), AttributeType.STOP, Thresholds.STOP_CENTROID_LATLON, 1.0/3.0)
						})
					);
		}
		return null;
	}
}
