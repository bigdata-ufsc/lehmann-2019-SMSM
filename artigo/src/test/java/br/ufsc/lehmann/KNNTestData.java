package br.ufsc.lehmann;

public class KNNTestData<T, O> {

	private T data;
	private O klazz;
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public O getKlazz() {
		return klazz;
	}
	public void setKlazz(O klazz) {
		this.klazz = klazz;
	}
	public KNNTestData(T data, O klazz) {
		super();
		this.data = data;
		this.klazz = klazz;
	}
}
