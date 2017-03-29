package br.ufsc.lehmann.msm.artigo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Andreas Thiele
 *
 *
 *         An implementation of knn. Uses Euclidean distance weighted by
 *         1/distance
 * 
 *         Main method to classify if entry is male or female based on: Height,
 *         weight
 */
public class NearestNeighbour<T> {
	public static void main(String[] args) {
		ArrayList<NearestNeighbour.DataEntry<double[]>> data = new ArrayList<NearestNeighbour.DataEntry<double[]>>();
		data.add(new DataEntry<double[]>(new double[] { 175, 80 }, "Male"));
		data.add(new DataEntry<double[]>(new double[] { 193.5, 110 }, "Male"));
		data.add(new DataEntry<double[]>(new double[] { 183, 92.8 }, "Male"));
		data.add(new DataEntry<double[]>(new double[] { 160, 60 }, "Male"));
		data.add(new DataEntry<double[]>(new double[] { 177, 73.1 }, "Male"));
		data.add(new DataEntry<double[]>(new double[] { 175, 80 }, "Female"));
		data.add(new DataEntry<double[]>(new double[] { 150, 55 }, "Female"));
		data.add(new DataEntry<double[]>(new double[] { 159, 63.2 }, "Female"));
		data.add(new DataEntry<double[]>(new double[] { 180, 70 }, "Female"));
		data.add(new DataEntry<double[]>(new double[] { 163, 110 }, "Female"));
		NearestNeighbour<double[]> nn = new NearestNeighbour<double[]>(data, 3/*neighbours*/, new IMeasureDistance<double[]>() {
			
			@Override
			public double distance(DataEntry<double[]> a, DataEntry<double[]> b) {
				double distance = 0.0;
				int length = a.getX().length;
				for (int i = 0; i < length; i++) {
					double t = a.getX()[i] - b.getX()[i];
					distance = distance + t * t;
				}
				return Math.sqrt(distance);
			}
		});
		System.out.println("Classified as: " + nn.classify(new DataEntry<double[]>(new double[] { 170, 60 }, "Ignore")));
	}

	private int k;
	private ArrayList<Object> classes;
	private ArrayList<DataEntry<T>> dataSet;
	private IMeasureDistance<T> measurer;

	/**
	 * 
	 * @param dataSet
	 *            The set
	 * @param k
	 *            The number of neighbours to use
	 */
	public NearestNeighbour(ArrayList<DataEntry<T>> dataSet, int k, IMeasureDistance<T> measurer) {
		this.measurer = measurer;
		this.classes = new ArrayList<Object>();
		this.k = k;
		this.dataSet = dataSet;

		// Load different classes
		for (DataEntry<T> entry : dataSet) {
			if (!classes.contains(entry.getY()))
				classes.add(entry.getY());
		}
	}

	private DataEntry<T>[] getNearestNeighbourType(DataEntry<T> x) {
		DataEntry<T>[] retur = new DataEntry[this.k];
		double fjernest = Double.MIN_VALUE;
		int index = 0;
		for (DataEntry<T> tse : this.dataSet) {
			double distance = distance(x, tse);
			if (retur[retur.length - 1] == null) { // Hvis ikke fyldt
				int j = 0;
				while (j < retur.length) {
					if (retur[j] == null) {
						retur[j] = tse;
						break;
					}
					j++;
				}
				if (distance > fjernest) {
					index = j;
					fjernest = distance;
				}
			} else {
				if (distance < fjernest) {
					retur[index] = tse;
					double f = 0.0;
					int ind = 0;
					for (int j = 0; j < retur.length; j++) {
						double dt = distance(retur[j], x);
						if (dt > f) {
							f = dt;
							ind = j;
						}
					}
					fjernest = f;
					index = ind;
				}
			}
		}
		return retur;
	}

	private double convertDistance(double d) {
		return 1.0 / d;
	}

	/**
	 * Computes distance
	 * 
	 * @param a
	 *            From
	 * @param b
	 *            To
	 * @return Distance
	 */
	public double distance(DataEntry<T> a, DataEntry<T> b) {
		return measurer.distance(a, b);
	}

	/**
	 * 
	 * @param e
	 *            Entry to be classifies
	 * @return The class of the most probable class
	 */
	public Object classify(DataEntry<T> e) {
		HashMap<Object, Double> classcount = new HashMap<Object, Double>();
		DataEntry<T>[] de = this.getNearestNeighbourType(e);
		for (int i = 0; i < de.length; i++) {
			double distance = convertDistance(distance(de[i], e));
			if (!classcount.containsKey(de[i].getY())) {
				classcount.put(de[i].getY(), distance);
			} else {
				classcount.put(de[i].getY(), classcount.get(de[i].getY()) + distance);
			}
		}
		// Find right choice
		Object o = null;
		double max = 0;
		for (Object ob : classcount.keySet()) {
			if (classcount.get(ob) > max) {
				max = classcount.get(ob);
				o = ob;
			}
		}

		return o;
	}

	public static class DataEntry<T> {
		private T x;
		private Object y;

		public DataEntry(T x, Object y) {
			this.x = x;
			this.y = y;
		}

		public T getX() {
			return this.x;
		}

		public Object getY() {
			return this.y;
		}
	}
}