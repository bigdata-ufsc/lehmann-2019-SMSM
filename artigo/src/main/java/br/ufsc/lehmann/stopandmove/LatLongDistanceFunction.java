package br.ufsc.lehmann.stopandmove;

import br.ufsc.core.trajectory.GeographicDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.utils.Distance;

public class LatLongDistanceFunction implements GeographicDistanceFunction {

	@Override
	public double distanceInMeters(TPoint p, TPoint d) {
		return Distance.distFrom(p, d);
	}
	
	@Override
	public double length(SemanticTrajectory trajectory) {
		double ret = 0;
		for (int i = 0; i < trajectory.length() - 2; i++) {
			ret += distanceInMeters(Semantic.GEOGRAPHIC.getData(trajectory, i), Semantic.GEOGRAPHIC.getData(trajectory, i + 1));
		}
		return ret;
	}

}
