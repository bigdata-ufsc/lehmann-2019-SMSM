package br.ufsc.lehmann.smsm;

import com.google.common.math.BigIntegerMath;

public class MathTest {

	public static void main(String[] args) {
		for (int i = 1; i < 36; i++) {
//			System.out.println(BigIntegerMath.factorial(i).doubleValue());
			System.out.println(Math.sqrt(BigIntegerMath.factorial(i + 1).doubleValue()));
		}
	}
}
