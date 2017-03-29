package br.ufsc.core.trajectory.semantic;
/**
 * 
 * @author André Salvaro Furtado
 *
 */
public class Move {
	
	private int tid;
	private int moveId;
	private int startStopId;
	private String startStopName;
	private int endStopId;
	private String endStopName;
	private double startTime;
	private double endTime;
	// points interval - indexes
	private int begin, end;


	public Move(int tid, int moveId, int startStopId, String startStopName,
			int endStopId, String endStopName, double startTime,
			double endTime, int begin, int end) {
		this.tid = tid;
		this.moveId = moveId;
		this.startStopId = startStopId;
		this.startStopName = startStopName;
		this.endStopId = endStopId;
		this.endStopName = endStopName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.begin = begin;
		this.end = end;
	}

	public int getTid() {
		return tid;
	}

	public int getMoveId() {
		return moveId;
	}

	public int getStartStopId() {
		return startStopId;
	}

	public String getStartStopName() {
		return startStopName;
	}

	public int getEndStopId() {
		return endStopId;
	}

	public String getEndStopName() {
		return endStopName;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public int getBegin() {
		return this.begin;
	}

	public int getEnd() {
		return this.end;
	}
	
}
