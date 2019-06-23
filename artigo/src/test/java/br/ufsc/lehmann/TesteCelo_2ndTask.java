package br.ufsc.lehmann;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TesteCelo_2ndTask {
	
	public static void main(String[] args) {
		/*
		 * startBalances: [5, 0, 0]
pendingTransactions: [[0,1,5], 
 [1,2,5]]
blockSize: 2
		 */
		String latestBlock = new TesteCelo_2ndTask().getLatestBlock(new int[] {5,0,0}, new int[][] {new int[] {0,1,5}, new int[] {1,2,5}}, 2);
		System.out.println(latestBlock);
	}

	String getLatestBlock(int[] startBalances, int[][] pendingTransactions, int blockSize) {
		java.security.MessageDigest crypt = null;
		try {
			crypt = java.security.MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		int[] currentBalances = startBalances.clone();
		String previousBlockHash = "0000000000000000000000000000000000000000";
		String previousBlock = null;
		int[][] blockTransactions = new int[blockSize][3];
		int currentBlock = 0;
		for (int i = 0; i < pendingTransactions.length; i++) {
			int[] transaction = pendingTransactions[i];
			boolean isValid = currentBalances[transaction[0]] - transaction[2] >= 0;
			if(isValid) {
				blockTransactions[currentBlock++] = transaction;
				currentBalances[transaction[0]] = currentBalances[transaction[0]] - transaction[2];
				currentBalances[transaction[1]] = currentBalances[transaction[1]] + transaction[2];
			}
			if(i % blockSize == blockSize - 1) {
				//finish a block
				//computes previous block hash
				int nonce = 0;
				String blockTransactionsString = Arrays.deepToString(blockTransactions);
				String head = previousBlockHash + ", ";
				String tail = ", " + blockTransactionsString;
				String blockString = head + nonce + tail;
				String hash = sha1(crypt, blockString);
				while(!isValidHash(hash)) {
					nonce++;
					blockString = head + nonce + tail;
					hash = sha1(crypt, blockString);
				}
				previousBlockHash = hash;
				previousBlock = hash + ", " + blockString;
				currentBlock = 0;
			}
		}
		if(currentBlock > 0) {
			//finish block with the remaining transactions
			int[][] remainingTransactions = new int[currentBlock][3];
			System.arraycopy(blockTransactions, 0, remainingTransactions, 0, currentBlock);
			int nonce = 0;
			String blockTransactionsString = Arrays.deepToString(remainingTransactions);
			String head = previousBlockHash + ", ";
			String tail = ", " + blockTransactionsString;
			String blockString = head + nonce + tail;
			String hash = sha1(crypt, blockString);
			while(!isValidHash(hash)) {
				nonce++;
				blockString = head + nonce + tail;
				hash = sha1(crypt, blockString);
			}
			previousBlockHash = hash;
			previousBlock = hash + ", " + blockString;
		}
		return previousBlock;
	}

	boolean isValidHash(String hash) {
		return hash.substring(0, 4) .equals("0000");
	}

	String sha1(java.security.MessageDigest crypt, String text) {
		String sha1 = "";
		try {
			crypt.update(text.getBytes("UTF-8"));
			Formatter formatter = new Formatter(new StringBuilder(text.length() * 2));
			for (byte b : crypt.digest()) {
				formatter.format("%02x", b);
			}
			sha1 = formatter.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sha1;
	}
}
