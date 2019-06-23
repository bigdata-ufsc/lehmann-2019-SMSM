package br.ufsc.lehmann;

import java.util.Arrays;

public class TesteCelo_1stTask {
	
	public static void main(String[] args) {
		
		/*
		 * node: 0
trustGraph: [[0,2], 
 [2,0]]
pretrustedPeers: [1]
trustThreshold: 1
		 */
		boolean isTrusted = new TesteCelo_1stTask().IsTrusted(0, new int[][] {new int[] {0,2}, new int[] {2,0}}, new int[] {1}, 5);
		System.out.println(isTrusted);
	}
	boolean IsTrusted(int node, int[][] trustGraph, int[] pretrustedPeers, int trustThreshold) {
	    int[] realDistances = new int[trustGraph.length];
	    Boolean[] computed = new Boolean[trustGraph.length]; 

	    for (int i = 0; i < trustGraph.length; i++) {
	    	realDistances[i] = Integer.MAX_VALUE; 
	    	computed[i] = false; 
	    }

	    realDistances[node] = 0; 
	    for (int count = 0; count < trustGraph.length - 1; count++) { 
	        int u = minDistance(realDistances, computed); 
	        
	        computed[u] = true; 

	        for (int v = 0; v < trustGraph.length; v++) 
	            if (!computed[v] && trustGraph[u][v] != 0 &&  
	               realDistances[u] != Integer.MAX_VALUE && realDistances[u] + trustGraph[u][v] < realDistances[v]) 
	                realDistances[v] = realDistances[u] + trustGraph[u][v]; 
	    }
	    for(int h = 0; h < realDistances.length; h++) {
	        if(contains(pretrustedPeers, h) && realDistances[h] <= trustThreshold) {
	            return true;
	        }
	    }
	    return false;
	}
	boolean contains(int[] pretrustedPeers, int h) {
		for (int i = 0; i < pretrustedPeers.length; i++) {
			if(pretrustedPeers[i] == h) {
				return true;
			}
		}
		return false;
	}
	int minDistance(int realDistance[], Boolean computed[]) { 
	    int min = Integer.MAX_VALUE, min_index = -1; 

	    for (int v = 0; v < realDistance.length; v++) 
	        if (computed[v] == false && realDistance[v] <= min) { 
	            min = realDistance[v]; 
	            min_index = v; 
	        } 

	    return min_index; 
	}
}
