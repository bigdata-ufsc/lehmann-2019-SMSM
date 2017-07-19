package br.ufsc.lehmann.msm.artigo.validation;

import smile.math.DoubleArrayList;
import smile.math.IntArrayList;
import smile.math.Math;

/**
 *
 * @author lucasyao, djh.shih
 *
 *         This is the data structure for result of silhouette. members: 1. total number of samples 2. silhouette values of each sample 3. the
 *         neighboring class of each sample (closest cluster)
 */
public class Silhouettes {

	DoubleArrayList silhouetteValues;
	IntArrayList neighborLabels;

	/**
	 * Constructor.
	 */
	public Silhouettes() {
		silhouetteValues = new DoubleArrayList();
		neighborLabels = new IntArrayList();
	}

	/**
	 * add a value in the silhouette list
	 * 
	 * @param value
	 *            is the silhouette value
	 * @param the
	 *            nearest cluster of the sample
	 */
	public void addSilhouette(double value, Integer label) {
		silhouetteValues.add(value);
		neighborLabels.add(label);
	}

	/**
	 * delete a silhouette at a given index
	 * 
	 * @param index
	 *            the position of the sample you want to delete (0~ size-1)
	 */
	public void deleteSilhouette(int index) {
		silhouetteValues.remove(index);
		neighborLabels.remove(index);
	}

	/**
	 * get the value for the silhouette at a given index
	 * 
	 * @param index
	 *            the position of the sample you want to delete (0~ size-1)
	 * @return
	 */
	public double getSilhouette(int index) {
		return silhouetteValues.get(index);
	}

	/**
	 * This function is to get the neighbor cluster of current sample (the given index)
	 * 
	 * @param index
	 *            the position of the sample you want to delete (0~ size-1)
	 * @return the neighbor cluster of current sample
	 */
	public Integer getNeighbor(int index) {
		return neighborLabels.get(index);
	}

	public int size() {
		return silhouetteValues.size();
	}

	/**
	 * Calculate the average silhouette.
	 * 
	 * @return the average silhouette
	 */
	public double getMean() {
		return Math.mean(silhouetteValues.toArray());
	}

	/**
	 * Calculate the median silhouette.
	 * 
	 * @return the median silhouette
	 */
	public double getMedian() {
		return Math.median(silhouetteValues.toArray());
	}
}