package br.ufsc.lehmann.classifier;

public class Binarizer {
	
	private Object truthValue;

	public Binarizer(Object truthValue) {
		this.truthValue = truthValue;
	}
	
	public Boolean isTrue(Object value) {
		return truthValue.equals(value);
	}

}
