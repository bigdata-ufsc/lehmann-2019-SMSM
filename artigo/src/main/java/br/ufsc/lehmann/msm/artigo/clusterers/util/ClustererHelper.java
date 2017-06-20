
package br.ufsc.lehmann.msm.artigo.clusterers.util;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Trajectories;

/**
 * A base foundation that provides an implementation of {@link #cluster(jsat.DataSet) } and
 * {@link #cluster(jsat.DataSet, java.util.concurrent.ExecutorService) } using their int array counterparts. <br>
 * <br>
 * By default it is assumed that a cluster does not support weighted data. If this is incorrect, you need to overwrite the
 * {@link #supportsWeightedData() } method.
 *
 * @author Edward Raff
 */
public abstract class ClustererHelper {

	/**
	 * Convenient helper method. A list of lists to represent a cluster may be desirable. In such a case, this method will take in an array of cluster
	 * assignments, and return a list of lists.
	 * 
	 * @param assignments
	 *            the array containing cluster assignments
	 * @param dataSet
	 *            the original data set, with data in the same order as was used to create the assignments array
	 * @return a List of lists where each list contains the data points for one cluster, and the lists are in order by cluster id.
	 */
	public static <T> List<List<SemanticTrajectory>> createClusterListFromAssignmentArray(int[] assignments, Trajectories<Number, T> dataSet) {
		List<List<SemanticTrajectory>> clusterings = new ArrayList<List<SemanticTrajectory>>();

		for (int i = 0; i < dataSet.sampleSize(); i++) {
			while (clusterings.size() <= assignments[i])
				clusterings.add(new ArrayList<SemanticTrajectory>());
			if (assignments[i] >= 0)
				clusterings.get(assignments[i]).add(dataSet.get(i));
		}

		return clusterings;
	}

	/**
	 * Gets a list of the datapoints in a data set that belong to the indicated cluster
	 * 
	 * @param c
	 *            the cluster ID to get the datapoints for
	 * @param assignments
	 *            the array containing cluster assignments
	 * @param dataSet
	 *            the data set to get the points from
	 * @param indexFrom
	 *            stores the index from the original dataset that the datapoint is from, such that the item at index {@code i} in the returned list
	 *            can be found in the original dataset at index {@code indexFrom[i]}. May be {@code null}
	 * @return a list of datapoints that were assignment to the designated cluster
	 */
	public static List<SemanticTrajectory> getDatapointsFromCluster(int c, int[] assignments, List<SemanticTrajectory> dataSet, int[] indexFrom) {
		List<SemanticTrajectory> list = new ArrayList<>();
		int pos = 0;
		for (int i = 0; i < dataSet.size(); i++)
			if (assignments[i] == c) {
				list.add(dataSet.get(i));
				if (indexFrom != null)
					indexFrom[pos++] = i;
			}
		return list;
	}
}
