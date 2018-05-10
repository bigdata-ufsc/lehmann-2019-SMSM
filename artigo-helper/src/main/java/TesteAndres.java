import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.math.Stats;

public class TesteAndres {

	public static void main(String[] args) {
		Random r = new Random(110);

		Integer[] pops = new Integer[] { 10, 500, 10000, 500000 };
		List<Integer> ty = Arrays.asList(pops);
		ty.parallelStream().forEach(pop -> {
			for (int dimens = 2; dimens <= 6; dimens++) {
				int[] s = new int[100];
				for (int k = 0; k < 100; k++) {
					List<Objeto> l = new ArrayList<>();
					for (int i = 0; i < pop; i++) {
						int[] values = new int[dimens];
						for (int j = 0; j < dimens; j++) {
							values[j] = r.nextInt(pop / 2);
						}
						l.add(new Objeto((r.nextBoolean() ? "X" : "O"), values));
					}

					ArrayList<Objeto> finalA = new ArrayList<>(l);
					andresMethod(finalA);
					s[k] = finalA.size();
				}
				Stats stats = Stats.of(s);
				System.out.printf("[Pop: %d, Dimens: %d] - Mean: %.2f, SD: %.2f\n", pop, dimens, stats.mean(),
						stats.populationStandardDeviation());
			}
		});

	}

	private static void andresMethod(List<Objeto> t) {
		for (int i = 0; i < t.size(); i++) {
			Objeto objeto = t.get(i);
			List<Objeto> g = new ArrayList<>(t);
			for (Objeto o : g) {
				if (objeto.hasInSuperiorQuadrant(o)) {
					int j = t.indexOf(o);
					if (j < i) {
						i--;
					}
					t.remove(j);
				}
			}
		}
	}

	private static class Objeto {
		private int id;
		private int[] dimensions;
		private String classe;
		static Integer uuid = 0;

		public Objeto(String classe, int... dimensions) {
			this.id = uuid++;
			this.classe = classe;
			this.dimensions = dimensions;
		}

		public boolean hasInSuperiorQuadrant(Objeto o) {
			for (int i = 0; i < dimensions.length; i++) {
				if (this.dimensions[i] >= o.dimensions[i]) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public String toString() {
			return "Objeto [id=" + id + ", classe=" + classe + ", dimensions=" + Arrays.toString(dimensions) + "]";
		}
	}
}
