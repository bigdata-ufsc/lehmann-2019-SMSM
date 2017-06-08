package br.ufsc.lehmann.stopandmove;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.utils.Distance;

public class LatLongDistanceFunction implements GeographicDistanceFunction {

	@Override
	public double distanceInMeters(TPoint p, TPoint d) {
		return Distance.distFrom(p, d);
	}

}
