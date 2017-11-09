package br.ufsc.core.trajectory.semantic;

import br.ufsc.core.trajectory.IDistanceFunction;

public class AttributeDescriptor<T, Y> {
	
	private AttributeType type;
	private IDistanceFunction<Y> function;

	public AttributeDescriptor(AttributeType type, IDistanceFunction<Y> function) {
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

	public double maxDistance() {
		return function.maxDistance();
	}

}
