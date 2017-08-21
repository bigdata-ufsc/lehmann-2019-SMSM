package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Stop;

public class ComparableStop extends Stop implements Comparable<ComparableStop> {
	
	private StopComparator comparator;

	public ComparableStop(Stop stop, StopComparator comparator) {
		super(stop.getStopId(), stop.getStopName(), stop.getStartTime(), stop.getEndTime(), stop.getStartPoint(), stop.getBegin(), stop.getEndPoint(), stop.getLength(), stop.getCentroid(), (String) stop.getAttribute(AttributeType.STOP_STREET_NAME));
		this.comparator = comparator;
	}

	@Override
	public int compareTo(ComparableStop o) {
		return comparator.compare(this, o);
	}

}
