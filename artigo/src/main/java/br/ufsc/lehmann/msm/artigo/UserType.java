package br.ufsc.lehmann.msm.artigo;

public enum UserType {
	Subscriber(0), Customer(1);
	
	private int id;

	UserType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}