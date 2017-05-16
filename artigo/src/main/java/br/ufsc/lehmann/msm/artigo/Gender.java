package br.ufsc.lehmann.msm.artigo;

public enum Gender {
	NO_INF(0), MASC(1), FEM(2);
	
	private int id;

	Gender(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public static Gender fromId(String id) {
		if(id == null) {
			return null;
		}
		switch(id) {
		case "0":
			return NO_INF;
		case "1":
			return MASC;
		case "2":
			return FEM;
		}
		return null;
	}
}