package br.ufsc.lehmann.testexecution;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

import br.ufsc.core.trajectory.EqualsDistanceFunction;
import br.ufsc.core.trajectory.IDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.lehmann.SMSM;
import br.ufsc.lehmann.SMSM.H_MSM_DimensionParameters;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMClassifier;
import br.ufsc.utils.EuclideanDistanceFunction;
import br.ufsc.utils.LatLongDistanceFunction;

public class Measures {

	public static TrajectorySimilarityCalculator<SemanticTrajectory> createMeasure(Measure measure) {
		if(measure.getName().equalsIgnoreCase("SMSM")) {
			return createSMSM(measure);
		}
		if(measure.getName().equalsIgnoreCase("MSM")) {
			return createMSM(measure);
		}
		return null;
	}

	private static TrajectorySimilarityCalculator<SemanticTrajectory> createSMSM(Measure measure) {
		List<Param> params = measure.getParams();
		Double stopWeight = 0.0, moveWeight = 0.0;
		StopSemantic stop = null;
		MoveSemantic move = null;
		List<SMSM.H_MSM_DimensionParameters<Stop>> stopDimensions = new ArrayList<>();
		List<SMSM.H_MSM_DimensionParameters<Move>> moveDimensions = new ArrayList<>();
		for (Param param : params) {
			if(param.getType().equalsIgnoreCase("stop")) {
				stopWeight = param.getWeight();
				stop = new StopSemantic(param.getIndex().intValue(), null);
				List<Param> stopParams = param.getParams();
				for (Param stopParam : stopParams) {
					AttributeType attr = null;
					boolean isSpatial = false;
					String d = stopParam.getDistance();
					double threshold = Double.parseDouble(Strings.isNullOrEmpty(stopParam.getThreshold()) ? "0.0" : stopParam.getThreshold());
					double weight = stopParam.getWeight().doubleValue();
					
					IDistanceFunction distance = null;
					if(!Strings.isNullOrEmpty(d)) {
						distance = createDistance(stopParam, d);
					}
						
					String type = stopParam.getType().toUpperCase();
					Long index = stopParam.getIndex();
					Semantic semantic = null;
					AttributeType mainAttribute = AttributeType.STOP;
					switch(type) {
						case "SPATIAL":
							attr = AttributeType.STOP_SPATIAL;
							mainAttribute = AttributeType.STOP_SPATIAL;
							isSpatial = true;
							switch(stopParam.getDistance().toUpperCase()) {
							case "LATLON":
								semantic = Semantic.SPATIAL_LATLON;
								break;
							case "EUCLIDEAN":
								semantic = Semantic.SPATIAL_EUCLIDEAN;
								break;
							}
							break;
						case "TEMPORAL":
							attr = AttributeType.STOP_TEMPORAL;
							mainAttribute = AttributeType.STOP_TEMPORAL;
							semantic = Semantic.TEMPORAL;
							break;
						case "SEMANTIC":
							List<Param> semanticParams = stopParam.getParams();
							Param semParam = semanticParams.get(0);
							String semType = semParam.getType().toUpperCase();
							switch(semType) {
								case "NAME":
									attr = AttributeType.STOP_NAME;
									break;
								case "REGION":
									attr = AttributeType.STOP_REGION;
									break;
								case "CENTROID":
									attr = AttributeType.STOP_CENTROID;
									break;
								case "DURATION":
									attr = AttributeType.STOP_DURATION;
									break;
								default:
									attr = AttributeType.STOP;
							}
							String semDistance = semParam.getDistance().toUpperCase();
							distance = createDistance(semParam, semDistance);
							AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
							semantic = new StopSemantic(index.intValue(), desc);
							threshold = Double.parseDouble(Strings.isNullOrEmpty(semParam.getThreshold()) ? "0.0" : semParam.getThreshold());
							break;
						}
					H_MSM_DimensionParameters dimension = new SMSM.H_MSM_DimensionParameters(semantic, mainAttribute, threshold, weight, isSpatial);
					stopDimensions.add(dimension);
				}
			} else if(param.getType().equalsIgnoreCase("move")) {
				moveWeight = param.getWeight();
				move = new MoveSemantic(param.getIndex().intValue(), null);
				List<Param> moveParams = param.getParams();
				for (Param moveParam : moveParams) {
					AttributeType attr = null;
					boolean isSpatial = false;
					String d = moveParam.getDistance();
					double threshold = Double.parseDouble(Strings.isNullOrEmpty(moveParam.getThreshold()) ? "0.0" : moveParam.getThreshold());
					double weight = moveParam.getWeight().doubleValue();
					IDistanceFunction distance = null;
					if(!Strings.isNullOrEmpty(d)) {
						distance = createDistance(moveParam, d);
					}
					String type = moveParam.getType().toUpperCase();
					Long index = moveParam.getIndex();
					switch(type) {
						case "POINTS":
							attr = AttributeType.MOVE_POINTS;
							List<Param> pointsParams = moveParam.getParams();
							Param pointsParam = pointsParams.get(0);
							String pointsDistance1 = pointsParam.getDistance().toUpperCase();
							SpatialDistanceFunction spatialDistance = null;
							switch(pointsDistance1) {
								case "LATLON":
									spatialDistance = new LatLongDistanceFunction();
									break;
								case "EUCLIDEAN":
									spatialDistance = new EuclideanDistanceFunction();
									break;
							}
							if(pointsDistance1.equals("UMS")) {
								distance = new EllipsesDistance(spatialDistance);
							} else {
								distance = new DTWDistance(spatialDistance);
							}
							isSpatial = true;
							break;
						case "DURATION":
							attr = AttributeType.MOVE_DURATION;
							break;
						case "SEMANTIC":
							List<Param> semanticParams = moveParam.getParams();
							Param semParam = semanticParams.get(0);
							String semType = semParam.getType().toUpperCase();
							switch(semType) {
								case "ACTIVITY":
									attr = AttributeType.MOVE_ACTIVITY;
									break;
								case "STREET_NAME":
									attr = AttributeType.MOVE_STREET_NAME;
									break;
								case "TRAVELLED_DISTANCE":
									attr = AttributeType.MOVE_TRAVELLED_DISTANCE;
									break;
								case "TRANSPORTATION_MODE":
									attr = AttributeType.MOVE_TRANSPORTATION_MODE;
									break;
								case "USER":
									attr = AttributeType.MOVE_USER;
									break;
								default:
									attr = AttributeType.MOVE;
							}
							String semDistance = semParam.getDistance().toUpperCase();
							distance = createDistance(semParam, semDistance);
						threshold = Double.parseDouble(Strings.isNullOrEmpty(semParam.getThreshold()) ? "0.0" : semParam.getThreshold());
						break;
					}
					AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
					MoveSemantic moveSemantic = new MoveSemantic(index.intValue(), desc);
					H_MSM_DimensionParameters<Move> dimension = new SMSM.H_MSM_DimensionParameters<>(moveSemantic, AttributeType.MOVE, threshold, weight, isSpatial);
					moveDimensions.add(dimension);
				}
			}
		}
		return new SMSMClassifier(//
				new SMSM.H_MSM_MoveSemanticParameters(move, moveDimensions.toArray(new SMSM.H_MSM_DimensionParameters[moveDimensions.size()]), moveWeight),
				new SMSM.H_MSM_StopSemanticParameters(stop, stopDimensions.toArray(new SMSM.H_MSM_DimensionParameters[stopDimensions.size()]), stopWeight)
				);
	}

	private static TrajectorySimilarityCalculator<SemanticTrajectory> createMSM(Measure measure) {
		List<Param> params = measure.getParams();
		Double stopWeight = 0.0, moveWeight = 0.0;
		List<MSMSemanticParameter> stopDimensions = new ArrayList<>();
		for (Param param : params) {
			if(param.getType().equalsIgnoreCase("stop")) {
				stopWeight = param.getWeight();
				List<Param> stopParams = param.getParams();
				for (Param stopParam : stopParams) {
					AttributeType attr = null;
					String d = stopParam.getDistance();
					double threshold = Double.parseDouble(Strings.isNullOrEmpty(stopParam.getThreshold()) ? "0.0" : stopParam.getThreshold());
					double weight = stopParam.getWeight().doubleValue();
					
					IDistanceFunction distance = null;
					if(!Strings.isNullOrEmpty(d)) {
						distance = createDistance(stopParam, d);
					}
						
					String type = stopParam.getType().toUpperCase();
					Long index = stopParam.getIndex();
					Semantic semantic = null;
					switch(type) {
						case "SPATIAL":
							attr = AttributeType.STOP_SPATIAL;
							switch(stopParam.getDistance().toUpperCase()) {
							case "LATLON":
								semantic = Semantic.SPATIAL_LATLON;
								break;
							case "EUCLIDEAN":
								semantic = Semantic.SPATIAL_EUCLIDEAN;
								break;
							}
							break;
						case "TEMPORAL":
							attr = AttributeType.STOP_TEMPORAL;
							semantic = Semantic.TEMPORAL;
							break;
						case "SEMANTIC":
							List<Param> semanticParams = stopParam.getParams();
							Param semParam = semanticParams.get(0);
							String semType = semParam.getType().toUpperCase();
							switch(semType) {
								case "NAME":
									attr = AttributeType.STOP_NAME;
									break;
								case "REGION":
									attr = AttributeType.STOP_REGION;
									break;
								case "CENTROID":
									attr = AttributeType.STOP_CENTROID;
									break;
								case "DURATION":
									attr = AttributeType.STOP_DURATION;
									break;
								default:
									attr = AttributeType.STOP;
							}
							String semDistance = semParam.getDistance().toUpperCase();
							distance = createDistance(semParam, semDistance);
							AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
							semantic = new StopSemantic(index.intValue(), desc);
							threshold = Double.parseDouble(Strings.isNullOrEmpty(semParam.getThreshold()) ? "0.0" : semParam.getThreshold());
							break;
						}
					MSMSemanticParameter dimension = new MSMSemanticParameter(semantic, threshold, weight);
					stopDimensions.add(dimension);
				}
			}
		}
		return new MSMClassifier(//
				stopDimensions.toArray(new MSMSemanticParameter[stopDimensions.size()])
				);
	}

	private static IDistanceFunction createDistance(Param stopParam, String d) {
		IDistanceFunction distance;
		String paramDistance = d.toUpperCase();
		switch(paramDistance) {
			case "LATLON":
				distance = new LatLongDistanceFunction();
				break;
			case "EUCLIDEAN":
				distance = new EuclideanDistanceFunction();
				break;
			case "EQUAL":
				distance = new EqualsDistanceFunction<>();
				break;
			case "ANGLE":
				distance = new AngleDistance();
				break;
			case "NUMBER":
				distance = new NumberDistance();
				break;
			case "DTW":
			case "UMS":
				List<Param> dtwParams = stopParam.getParams();
				Param dtwParam = dtwParams.get(0);
				String dtwDistance = dtwParam.getDistance().toUpperCase();
				SpatialDistanceFunction spatialDistance = null;
				switch(dtwDistance) {
				case "LATLON":
					spatialDistance = new LatLongDistanceFunction();
					break;
				case "EUCLIDEAN":
					spatialDistance = new EuclideanDistanceFunction();
					break;
				}
				if(paramDistance.equals("UMS")) {
					distance = new EllipsesDistance(spatialDistance);
				} else {
					distance = new DTWDistance(spatialDistance);
				}
				break;
			default:
				distance = new EqualsDistanceFunction<>();
		}
		return distance;
	}

}
