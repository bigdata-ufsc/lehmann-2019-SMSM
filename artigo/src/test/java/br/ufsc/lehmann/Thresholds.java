package br.ufsc.lehmann;

import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeType;

public interface Thresholds {

	public static final int GEOGRAPHIC_LATLON = 50;
	public static final int GEOGRAPHIC_EUCLIDEAN = 500;
	public static final int GEOGRAPHIC_PROTOTYPE = 1;
	public static final double TEMPORAL = 0.7;
	public static final int STOP_CENTROID_LATLON = 500;
	public static final int STOP_CENTROID_EUCLIDEAN = 5000;
	public static final int STOP_STREET_NAME = 0;
	public static final int MOVE_ANGLE = 10 / 180;
	public static final int MOVE_DISTANCE = 150;
	public static final int MOVE_INNERPOINTS_DISTANCE = 100;
	public static final int MOVE_DURATION = 2 * 60 * 1000;//2 minutes
	public static final double MOVE_INNER_POINTS_PERC = .75;
	public static final double STOP_MOVE = .5;
	

	public static double calculateThreshold(StopSemantic semantic) {
		if(semantic.name().equals(AttributeType.STOP_CENTROID.name())) {
			return Thresholds.STOP_CENTROID_LATLON;
		}
		if(semantic.name().equals(AttributeType.STOP_STREET_NAME.name())) {
			return Thresholds.STOP_STREET_NAME;
		}
		if(semantic.name().equals(AttributeType.STOP_NAME.name())) {
			return Thresholds.STOP_STREET_NAME;
		}
		return 0.0;
	}
}
