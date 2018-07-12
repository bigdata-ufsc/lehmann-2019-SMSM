package br.ufsc.lehmann;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;

public interface Thresholds {

	public static final MutableInt SPATIAL_LATLON = new MutableInt(4);
	public static final MutableInt SPATIAL_EUCLIDEAN = new MutableInt(200);
	public static final MutableInt SPATIAL_PROTOTYPE = new MutableInt(1);
	public static final MutableDouble PROPORTION_TEMPORAL = new MutableDouble(.1);
	public static final MutableLong SLACK_TEMPORAL = new MutableLong(0 * 60 * 60 * 1000);
	public static final MutableDouble TEMPORAL = PROPORTION_TEMPORAL;
	public static final MutableInt STOP_CENTROID_LATLON = new MutableInt(500);
	public static final MutableInt STOP_CENTROID_EUCLIDEAN = new MutableInt(200);
	public static final MutableInt MOVE_DISTANCE = new MutableInt(50);
	public static final MutableInt MOVE_INNERPOINTS_DISTANCE = new MutableInt(100);
	public static final ComputableDouble<Move> MOVE_INNERPOINTS_DTW_DISTANCE = new ComputableDouble<Move>() {
		public Number compute(Move a, Move b) {
			return (a.getTravelledDistance() + b.getTravelledDistance());
		}
	};
	public static final MutableInt MOVE_DURATION = new MutableInt(2 * 60 * 1000);//2 minutes
	public static final MutableDouble MOVE_INNER_POINTS_PERC = new MutableDouble(.001);
	public static final MutableDouble STOP_MOVE = new MutableDouble(.5);

	public static Double calculateThreshold(StopSemantic semantic) {
		if(semantic.name().equals(AttributeType.STOP_CENTROID.name())) {
			return Thresholds.STOP_CENTROID_EUCLIDEAN.doubleValue();
		}
		return null;
	}
}
