package br.ufsc.lehmann.method;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;

public class MSTP extends TrajectorySimilarityCalculator<SemanticTrajectory> {
	
	private Semantic<Comparable<? extends Object>, ?>[] semantics;

	public MSTP(Semantic<Comparable<? extends Object>, ?>... semantics) {
		this.semantics = semantics;
	}

	@Override
	public double getDistance(SemanticTrajectory t1, SemanticTrajectory t2) {
		/*
		 * Similarity algo. of MSTP-Similarity by [Ying, Lu el. 2010]
		 */
		RatioPair rp = calPatternSimilarRatios(t1, t2);
		int pattern1_size = t1.length();
		int pattern2_size = t2.length();
		double ratio1 = 1.0 * rp.ratio1 / pattern1_size;
		double ratio2 = 1.0 * rp.ratio2 / pattern2_size;
		// weighted by length
		return (pattern1_size * ratio1 + pattern2_size * ratio2) / (pattern1_size + pattern2_size);
	}

	private RatioPair calPatternSimilarRatios(SemanticTrajectory p1, SemanticTrajectory p2) {
		RatioPair[][] matrics = calRatioPairMatrics(getPatternItemsetList(p1), getPatternItemsetList(p2));
		return matrics[p1.length()][p2.length()];
	}

	private List<Itemset<ComplexSemanticType>> getPatternItemsetList(SemanticTrajectory st) {
		List<Itemset<ComplexSemanticType>> ret = new ArrayList<>(st.length());
		for (int i = 0; i < st.length(); i++) {
			ComplexSemanticType complex = new ComplexSemanticType(semantics.length);
			for (int j = 0; j < semantics.length; j++) {
				Comparable<? extends Object> semanticData = semantics[j].getData(st, i);
				complex.data[j] = semanticData;
			}
			ret.add(new Itemset<>(new ComplexSemanticTypeComparator(), complex));
		}
		return ret;
	}

	private RatioPair[][] calRatioPairMatrics(List<Itemset<ComplexSemanticType>> La, List<Itemset<ComplexSemanticType>> Lb) {
		/*
		 * Based on the dynamic programming version of LCS Different to the
		 * above, itemset is weighted by its location in the sequence For
		 * itemset i in L-sequence, the weighted value is calculated by
		 * exp(L-i+1)/L
		 */
		int aLen = La.size();
		int bLen = Lb.size();

		RatioPair[][] c = new RatioPair[aLen + 1][bLen + 1];
		for (int i = 0; i < aLen + 1; i++) {
			for (int j = 0; j < bLen + 1; j++) {
				c[i][j] = new RatioPair();
			}
		}

		int last_a_index = -1;
		int last_b_index = -1;
		for (int i = 1; i < aLen + 1; i++) {
			for (int j = 1; j < bLen + 1; j++) {
				Itemset<ComplexSemanticType> commonSubset = commonSubset(La.get(i - 1), Lb.get(j - 1));
				int temp_count = c[i - 1][j - 1].count + commonSubset.size();
				if (temp_count > c[i - 1][j].count && temp_count > c[i][j - 1].count) {
					c[i][j].count = temp_count;
					// position weight
					double pw_a = 0, pw_b = 0;
					if (last_a_index != -1)
						pw_a = -1.0 * (i - 1 - last_a_index - 1) / aLen;
					if (last_b_index != -1)
						pw_b = -1.0 * (j - 1 - last_b_index - 1) / bLen;
					c[i][j].ratio1 = c[i - 1][j - 1].ratio1
							+ Math.pow(Math.E, pw_a) * commonSubset.size() / La.get(i - 1).size();
					c[i][j].ratio2 = c[i - 1][j - 1].ratio2
							+ Math.pow(Math.E, pw_b) * commonSubset.size() / Lb.get(j - 1).size();

					last_a_index = i - 1;
					last_b_index = j - 1;
				} else {
					if (c[i - 1][j].count > c[i][j - 1].count)
						c[i][j].set(c[i - 1][j]);
					else
						c[i][j].set(c[i][j - 1]);
				}
			}
		}

		return c;
	}

	private Itemset<ComplexSemanticType> commonSubset(Itemset<ComplexSemanticType> a, Itemset<ComplexSemanticType> b) {
		Itemset<ComplexSemanticType> commonItemset = new Itemset<ComplexSemanticType>(a.comparator);
		List<ComplexSemanticType> aItems = a.getItems();
		List<ComplexSemanticType> bItems = b.getItems();
		for (ComplexSemanticType e : aItems) {
			if (bItems.contains(e)) {
				commonItemset.addItem(e);
			}
		}
		return commonItemset;
	}

	static class RatioPair {
		public double ratio1 = 0;
		public double ratio2 = 0;
		// total common items, used by dynamic programming LCS
		public int count = 0;

		public void set(RatioPair pair) {
			this.ratio1 = pair.ratio1;
			this.ratio2 = pair.ratio2;
			this.count = pair.count;
		}
	}
	
	static class ComplexSemanticType {
		Comparable<?>[] data;

		ComplexSemanticType(int semantics) {
			data = new Comparable<?>[semantics];
		}
	}
}
