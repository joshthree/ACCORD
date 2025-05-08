package util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import exp1.Point;

public class Shuffle {
	public static int[] shuffle(int[] source, int indexStart, int numVals, Random rnd) {
		int[] toReturn = source.clone();
		int swap;
		for(int i = indexStart; i < indexStart + numVals; i++) {
			int index = rnd.nextInt(source.length-i)+i;
			if(index == i) continue;
			swap = toReturn[index];
			toReturn[index] = toReturn[i];
			toReturn[i] = swap;
		}
		return toReturn;
	}
	
	public static int[] shuffleWithPriority(int[] source, int numPriority, int indexStart, int numVals, Random rnd) {
		int[] toReturn = source.clone();
//		if(numPriority > numVals) System.out.println("How???");
		int swap;
		for(int i = Math.max(indexStart, numPriority); i < indexStart + numVals; i++) {
			int index = rnd.nextInt(source.length-i)+i;
			if(index == i) continue;
			swap = toReturn[index];
			toReturn[index] = toReturn[i];
			toReturn[i] = swap;
		}
		return toReturn;
	}


}
