package br.ufsc.core.trajectory;

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

}
