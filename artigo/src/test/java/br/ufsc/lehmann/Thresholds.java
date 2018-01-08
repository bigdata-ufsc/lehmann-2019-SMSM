package br.ufsc.lehmann;

import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeType;

public interface Thresholds {

	public static final int GEOGRAPHIC_LATLON = 50;
	public static final int GEOGRAPHIC_EUCLIDEAN = 500;
	public static final int GEOGRAPHIC_PROTOTYPE = 1;
	public static final double PROPORTION_TEMPORAL = 0.99;
	public static final long SLACK_TEMPORAL = 30 * 60 * 1000;
	public static final double TEMPORAL = PROPORTION_TEMPORAL;
	public static final double STOP_CENTROID_LATLON = 500;
	public static final int STOP_CENTROID_EUCLIDEAN = 5000;
	public static final Double STOP_STREET_NAME = null;
	public static final int MOVE_ANGLE = 10 / 180;
	public static final int MOVE_DISTANCE = 150;
	public static final int MOVE_INNERPOINTS_DISTANCE = 100;
	public static final int MOVE_DURATION = 2 * 60 * 1000;//2 minutes
	public static final double MOVE_INNER_POINTS_PERC = .9;
	public static final double STOP_MOVE = .5;

	public static Double calculateThreshold(StopSemantic semantic) {
		if(semantic.name().equals(AttributeType.STOP_CENTROID.name())) {
			return Thresholds.STOP_CENTROID_LATLON;
		}
		if(semantic.name().equals(AttributeType.STOP_STREET_NAME.name())) {
			return Thresholds.STOP_STREET_NAME;
		}
		if(semantic.name().equals(AttributeType.STOP_NAME.name())) {
			return Thresholds.STOP_STREET_NAME;
		}
		return null;
	}
}
