package br.ufsc.lehmann.metric;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class NDCG {

	// Prevent instantiation.
	private NDCG() {
	}

	/**
	 * Compute the normalized discounted cumulative gain (NDCG) of a list of ranked
	 * items.
	 * 
	 * See http://recsyswiki.com/wiki/Discounted_Cumulative_Gain
	 * 
	 * @param ranked_items  a list of ranked item IDs, the highest-ranking item
	 *                      first
	 * @param correct_items a collection of positive/correct item IDs
	 * @param ignore_items  a collection of item IDs which should be ignored for the
	 *                      evaluation
	 * @return the NDCG for the given data
	 */
	public static double compute(List<String> ranked_items, Collection<String> correct_items,
			Collection<String> ignore_items) {

		if (ignore_items == null)
			ignore_items = new HashSet<String>();

		double dcg = 0;
		double idcg = computeIDCG(correct_items.size());
		int left_out = 0;

		for (int i = 0; i < ranked_items.size(); i++) {
			String item_id = ranked_items.get(i);
			if (ignore_items.contains(item_id)) {
				left_out++;
				continue;
			}

			if (!correct_items.contains(item_id))
				continue;

			// compute NDCG part
			int rank = i + 1 - left_out;
			dcg += Math.log(2) / Math.log(rank + 1);

		}

		return dcg / idcg;
	}

	/**
	 * Computes the ideal DCG given the number of positive items..
	 * 
	 * See http://recsyswiki.com/wiki/Discounted_Cumulative_Gain
	 * 
	 * @return the ideal DCG <param name='n'>the number of positive items
	 */
	static double computeIDCG(int n) {
		double idcg = 0;
		for (int i = 0; i < n; i++)
			idcg += Math.log(2) / Math.log(i + 2);
		return idcg;
	}
}