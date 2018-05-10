package br.ufsc.lehmann.method;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.SMSM;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface SMSMDTWTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		MoveSemantic moveSemantic = null;
		Semantic<TPoint, Number> geoSemantic = Semantic.SPATIAL_LATLON;
		MutableInt geoThreshold = Thresholds.STOP_CENTROID_LATLON;
		if(problem instanceof SanFranciscoCabProblem) {
			stopSemantic = ((SanFranciscoCabProblem) problem).stopSemantic();
			moveSemantic = SanFranciscoCabDataReader.MOVE_POINTS_SEMANTIC;
		} else if(problem instanceof GeolifeUniversitySubProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeUniversitySubProblem) problem).stopSemantic();
			moveSemantic = GeolifeUniversityDataReader.MOVE_POINTS_SEMANTIC;
		}
		return new SMSMClassifier(//
				new SMSM.H_MSM_MoveSemanticParameters(moveSemantic, new SMSM.H_MSM_DimensionParameters[] {
						new SMSM.H_MSM_DimensionParameters<>(moveSemantic, AttributeType.MOVE, Thresholds.MOVE_INNERPOINTS_DTW_DISTANCE, 1)
					}, 5.0/10.0),
				new SMSM.H_MSM_StopSemanticParameters(stopSemantic, new SMSM.H_MSM_DimensionParameters[] {
						new SMSM.H_MSM_DimensionParameters<>(geoSemantic, AttributeType.STOP_SPATIAL, geoThreshold.intValue(), 5.0/10.0, true),
//						new SMSM.H_MSM_DimensionParameters<>(SlackTemporalSemantic.SLACK_TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
						new SMSM.H_MSM_DimensionParameters<>(stopSemantic, AttributeType.STOP, Thresholds.calculateThreshold(stopSemantic), 5.0/10.0)
					}, 5.0/10.0)
				);
	}
}
