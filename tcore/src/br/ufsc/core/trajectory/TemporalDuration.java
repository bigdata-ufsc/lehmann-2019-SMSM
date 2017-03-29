package br.ufsc.core.trajectory;

import java.time.Instant;

public class TemporalDuration {

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
	
}
