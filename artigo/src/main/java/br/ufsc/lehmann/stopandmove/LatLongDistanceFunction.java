package br.ufsc.lehmann.stopandmove;

import br.ufsc.core.trajectory.GeographicDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.utils.Distance;

public class LatLongDistanceFunction implements GeographicDistanceFunction {

	@Override
	public double distance(TPoint p, TPoint d) {
		return Distance.distFrom(p, d) / Distance.EARTH_RADIUS;
	}
	
	@Override
	public double length(SemanticTrajectory trajectory) {
		double ret = 0;
		for (int i = 0; i < trajectory.length() - 2; i++) {
			ret += distance(Semantic.GEOGRAPHIC_LATLON.getData(trajectory, i), Semantic.GEOGRAPHIC_LATLON.getData(trajectory, i + 1));
		}
		return ret;
	}
	
	@Override
	public double convert(double units) {
		return units / Distance.EARTH_RADIUS;
	}

}
