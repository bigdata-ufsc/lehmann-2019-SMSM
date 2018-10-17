package br.ufsc.core;

import org.apache.commons.lang3.mutable.MutableDouble;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;

public abstract class ComputableThreshold<T, E> extends MutableDouble {
	
	private String description;

	public ComputableThreshold(String description) {
		this.description = description;
	}

	public abstract T compute(E a, E b, SemanticTrajectory trajA, SemanticTrajectory trajB, Semantic<E, T> semantic);

	public String description() {
		return description;
	}

}
