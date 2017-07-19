package br.ufsc.lehmann.msm.artigo.clusterers.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceMatrix<T> {

	private Map<Tuple<T, T>, Double> distances = new HashMap<>();
	
	public DistanceMatrix() {
	}
	
	public DistanceMatrix(List<T> data, double[][] distances) {
		if(distances.length == 0) {
			throw new IllegalArgumentException();
		}
		if(distances.length != distances[0].length) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				put(data.get(i), data.get(j), distances[i][j]);
			}
		}
	}
	
	public void put(T first, T last, Double distance) {
		distances.put(new Tuple<>(first, last), distance);
	}
	
	public Double retrieve(T first, T last) {
		return distances.get(new Tuple<>(first, last));
	}
	
	public static class Tuple<F, L> {
		private F first;
		private L last;
		public Tuple(F first, L last) {
			this.first = first;
			this.last = last;
		}
		public F getFirst() {
			return first;
		}
		public void setFirst(F first) {
			this.first = first;
		}
		public L getLast() {
			return last;
		}
		public void setLast(L last) {
			this.last = last;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((first == null) ? 0 : first.hashCode());
			result = prime * result + ((last == null) ? 0 : last.hashCode());
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
			Tuple other = (Tuple) obj;
			if (first == null) {
				if (other.first != null)
					return false;
			} else if (!first.equals(other.first))
				return false;
			if (last == null) {
				if (other.last != null)
					return false;
			} else if (!last.equals(other.last))
				return false;
			return true;
		}
	}
}
