package br.ufsc.core.trajectory.semantic;

import br.ufsc.core.trajectory.IDistanceFunction;

public class AttributeDescriptor<T> {
	
	private AttributeType type;
	private IDistanceFunction function;

	public AttributeDescriptor(AttributeType type, IDistanceFunction function) {
		this.type = type;
		this.function = function;
	}

	public double convertThreshold(double doubleValue) {
		return function.convert(doubleValue);
	}

	public double distance(T d1, T d2) {
		return function.distance(type.getValue(d1), type.getValue(d2));
	}
	
	public String attributeName() {
		return type.name();
	}

}
