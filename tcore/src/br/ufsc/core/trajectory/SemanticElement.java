package br.ufsc.core.trajectory;

import java.util.Arrays;

public class SemanticElement {

	private Object[] dimensions;
	
	public SemanticElement(int semantics) {
		dimensions = new Object[semantics];
	}

	public Object getData(int index) {
		return dimensions[index];
	}
	
	public Object getData(Semantic<?, ?> semantic) {
		return dimensions[semantic.index];
	}

	public void setData(int index, Object p) {
		dimensions[index] = p;
	}

	public void addData(Semantic<?, ?> semantic, Object data) {
		dimensions[semantic.index] = data;
	}

	@Override
	public String toString() {
		return "SemanticElement [dimensions=" + Arrays.toString(dimensions) + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(dimensions);
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
		SemanticElement other = (SemanticElement) obj;
		if (!Arrays.equals(dimensions, other.dimensions))
			return false;
		return true;
	}

}
