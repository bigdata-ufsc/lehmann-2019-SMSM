package br.ufsc.ftsm;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.Trajectory;

public class Score implements Comparable<Score> {

	private double score;
	private SemanticTrajectory t;

	public Score(double score, SemanticTrajectory t) {
		this.score = score;
		this.setTrajectory(t);
	}

	public double getScore() {
		return this.score;
	}

	@Override
	public int compareTo(Score score2) {
		if (this.score < score2.getScore()) {
			return -1;
		}
		if (this.score > score2.getScore()) {
			return 1;
		}
		return 0;
	}

	public SemanticTrajectory getTrajectory() {
		return t;
	}

	public void setTrajectory(SemanticTrajectory t) {
		this.t = t;
	}

}
