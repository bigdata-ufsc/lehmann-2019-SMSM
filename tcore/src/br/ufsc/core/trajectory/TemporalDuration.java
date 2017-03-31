package br.ufsc.core.trajectory;

import java.time.Instant;

public class TemporalDuration implements Comparable<TemporalDuration> {

	private Instant start;
	private Instant end;
	public Instant getStart() {
		return start;
	}
	public void setStart(Instant start) {
		this.start = start;
	}
	public Instant getEnd() {
		return end;
	}
	public void setEnd(Instant end) {
		this.end = end;
	}
	public TemporalDuration(Instant start, Instant end) {
		super();
		this.start = start;
		this.end = end;
	}
	@Override
	public int compareTo(TemporalDuration o) {
		if(o == null) {
			return 1;
		}
		if(equals(o)) {
			return 0;
		}
		int startComp = start.compareTo(o.start);
		if(startComp != 0) {
			return startComp;
		}
		return end.compareTo(o.end);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemporalDuration other = (TemporalDuration) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
	
}
