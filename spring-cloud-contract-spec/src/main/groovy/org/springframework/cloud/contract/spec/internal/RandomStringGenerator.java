package org.springframework.cloud.contract.spec.internal;

import java.util.Random;

class RandomStringGenerator {
	static String randomString(int length) {
		char[] characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		Random random = new Random();
		char[] result = new char[length];
		for (int i = 0; i < result.length; i++) {
			// picks a random index out of character set > random character
			int randomCharIndex = random.nextInt(characterSet.length);
			result[i] = characterSet[randomCharIndex];
		}
		return new String(result);
	}
}
