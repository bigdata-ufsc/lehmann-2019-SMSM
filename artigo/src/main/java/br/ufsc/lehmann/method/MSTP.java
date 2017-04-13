package br.ufsc.lehmann.method;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	/**
	 * This class represents an itemset from a sequence from a sequence database.
	 *   - items are integers
	 *   - the itemset is a list of strings ordered by lexical order and does not 
	 *     contain an item twice.
	 *   - this class does not store the support of the itemset
	 * 
	 * Copyright (c) 2008-2013 Philippe Fournier-Viger
	 * 
	 * This file is part of the SPMF DATA MINING SOFTWARE
	 * (http://www.philippe-fournier-viger.com/spmf).
	 *
	 * SPMF is free software: you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License as published by
	 * the Free Software Foundation, either version 3 of the License, or
	 * (at your option) any later version.
	 *
	 * SPMF is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 * GNU General Public License for more details.
	 *
	 * You should have received a copy of the GNU General Public License
	 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
	 */
	public static class Itemset<T> implements Serializable{
		private static final long serialVersionUID = -1877971586744705191L;
		// The list of items in this itemset.
		// The items are lexically ordered and an item can only
		// appear once in an itemset.
		private final List<T> items = new ArrayList<>();
		Comparator<T> comparator; 
		
		/**
		 * Constructor to create an itemset with an item
		 * @param item the item
		 */
		public Itemset(Comparator<T> typeComparator, T item){
			comparator = typeComparator;
			addItem(item);
		}
		
		/**
		 * Constructor to create an empty itemset.
		 */
		public Itemset(Comparator<T> typeComparator){
			this.comparator = typeComparator;
		}

		/**
		 * Add an item to this itemset
		 * @param value the item
		 */
		public void addItem(T value){
				items.add(value);
		}
		
		/**
		 * Get the list of items
		 * @return list of items
		 */
		public List<T> getItems(){
			return items;
		}
		
		/**
		 * Get an item at a given position in this itemset
		 * @param index the position
		 * @return the item
		 */
		public T get(int index){
			return items.get(index);
		}

		/**
		 * Get this itemset as a string
		 * @return this itemset as a string
		 */
		public String toString(){
			StringBuffer r = new StringBuffer ();
			for(T item : items){
				r.append(item.toString());
				r.append(' ');
			}
			return r.toString();
		}
		
		/**
		 * Get the size of this itemset (the number of items)
		 * @return the size
		 */
		public int size(){
			return items.size();
		}

		/**
		 * This methods makes a copy of this itemset but without
		 * items having a support lower than minsup
		 * @param mapSequenceID a map indicating the support of each item. key: item  value: support
		 * @param relativeMinsup the support expressed as a percentage
		 * @return the new itemset
		 */
		public Itemset<T> cloneItemSetMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double relativeMinsup) {
			Itemset<T> itemset = new Itemset<T>(this.comparator);
			for(T item : items){
				if(mapSequenceID.get(item).size() >= relativeMinsup){
					itemset.addItem(item);
				}
			}
			return itemset;
		}
		
		/**
		 * This method makes a copy of an itemset
		 * @return the copy.
		 */
		public Itemset<T> cloneItemSet(){
			Itemset<T> itemset = new Itemset<T>(this.comparator);
			itemset.getItems().addAll(items);
			return itemset;
		}
		
		/**
		 * This methods checks if another itemset is contained in this one.
		 * @param itemset2 the other itemset
		 * @return true if it is contained
		 */
		public boolean containsAll(Itemset<T> itemset2){
			// we will use this variable to remember where we are in this itemset
			int i = 0;
			
			// for each item in itemset2, we will try to find it in this itemset
			for(T item : itemset2.getItems()){
				boolean found = false; // flag to remember if we have find the item
				
				// we search in this itemset starting from the current position i
				while(found == false && i < size()){
					// if we found the current item from itemset2, we stop searching
					if(get(i).equals(item)){
						found = true;
					}// if the current item in this itemset is larger than 
					// the current item from itemset2, we return false
					// because the itemsets are assumed to be lexically ordered.
					else if(comparator.compare(get(i), item) > 0){
						return false;
					}
					
					i++; // continue searching from position  i++
				}
				// if the item was not found in the previous loop, return false
				if(!found){
					return false;
				}
			}
			return true; // if all items were found, return true
		}
		
		
		/**
		 * Check if two itemsets contain the same content
		 * @param itemset2
		 * @return
		 */
		public boolean equals(Itemset<T> itemset2){
			if ( this.containsAll(itemset2) && itemset2.containsAll(this) )
				return true;
			return false;
		}
	}
}
