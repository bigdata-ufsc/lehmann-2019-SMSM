package br.ufsc.lehmann.stopandmove;

import br.ufsc.core.trajectory.TPoint;

public interface GeographicDistanceFunction {

	double distanceInMeters(TPoint p, TPoint d);
}
