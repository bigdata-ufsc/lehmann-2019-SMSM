package br.ufsc.lehmann.msm.artigo;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.GenderSemantic.Gender;

public class GenderSemantic extends Semantic<String, Number>{

	public GenderSemantic(int index) {
		super(index);
	}

	@Override
	public boolean match(SemanticTrajectory a, int i, SemanticTrajectory b, int j, Number threshlod) {
		return a.getDimensionData(index, i).equals(b.getDimensionData(index, j));
	}

	@Override
	public Number distance(SemanticTrajectory a, int i, SemanticTrajectory b, int j) {
		return match(a, i, b, j, null) ? 0.0 : 1.0;
	}

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
}
