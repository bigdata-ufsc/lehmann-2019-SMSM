package br.ufsc.core.trajectory.semantic;

public class Attribute {
	private AttributeType type;
	private Object value;

	public Attribute(AttributeType type, Object value) {
		this.type = type;
		this.value = value;
	}
	public AttributeType getType() {
		return type;
	}
	public void setType(AttributeType type) {
		this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
