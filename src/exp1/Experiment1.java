package exp1;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import util.Shuffle;

public class Experiment1 {
	public static void main(String[] args) {
		System.out.println("n, q, min, max, average");
		int[] vals = {1000,2000,3000,4000,5000};
		double[] greylistProportions = {0.33};
		int numIterations = 1000000;
		long start = System.currentTimeMillis();
		for(int b = 0; b < greylistProportions.length; b++) {
			double greylistProportion = greylistProportions[b];
			
			System.out.println(greylistProportion);
			for(int a = 0; a < vals.length; a++) {
				Queue<Point> greylist = new LinkedList<Point>();
				int n = vals[a];
				boolean greylisted[] = new boolean[n];
				
				int minVal = 0;
				int[] leftGreyList = new int[n];
				LinkedList<Integer> minIndexes = new LinkedList<Integer>();
				
				for(int i = 0; i < n; i++){
					minIndexes.add(i);
				}
				int q = ((int) Math.ceil(Math.log(n)/Math.log(2))) + 1;
				Queue<BigInteger> headers = new LinkedList<BigInteger>();
				
				Random rnd = new SecureRandom();
				headers.add(new BigInteger(256, rnd));
				headers.add(new BigInteger(256, rnd));
				headers.add(new BigInteger(256, rnd));
				
				int[] counters = new int[n];

				int whiteListThreshold = 3*(n/q);
				for(int i = 0; i < numIterations; i++) {
//					if(minVal <= i - whiteListThreshold) System.out.println("WhiteListUsed " + i);
					/*ERROR CHECKING*/
//				Object[] array = greylist.toArray();
//				
//
//				for(int j = 1; j < array.length; j++){
//					for(int k = 0; k < j; k++) {
//						if(((Point) array[j]).y == ((Point) array[k]).y) System.out.println("PANIC!!  REPEAT NODES IN GREYLIST");
//					}
//				}
					
					if(greylist.size() > greylistProportion * n)
					{
						
						Point p = greylist.poll();
						greylisted[p.y-1] = false;
						leftGreyList[p.y-1] = i;
						while(greylist.peek() != null && greylist.peek().x == p.x) {
							p = greylist.poll();
							greylisted[p.y-1] = false;
							leftGreyList[p.y-1] = i;
						}
					}
					int[] miningNodes = new int[n - greylist.size()];
					boolean[] alreadyIn = new boolean[n];
					int priorityCount = 0;
					if(minVal < i - whiteListThreshold) {
						for(int j = 0; j < n; j++) {
							if(leftGreyList[j] < i - whiteListThreshold) {
								miningNodes[priorityCount] = j+1;
//								System.out.println(priorityCount);
								priorityCount++;
								alreadyIn[j] = true;
							}
						}
					}
					int count = 0;
					for(int j = priorityCount; j < miningNodes.length; j++) {
						while(greylisted[count] || alreadyIn[count]) {
							count++;
						}
						
						miningNodes[j] = count+1;
						count++;
					}
					/*ERROR CHECK*/
//				for(int j = 1; j < miningNodes.length; j++){
//					for(int k = 0; k < j; k++) {
//						if(miningNodes[j] == miningNodes[k]) System.out.println("PANIC!!  REPEAT NODES IN MINING LIST");
//					}
//				}
					
					byte[] seed = headers.poll().toByteArray();
					rnd = new SecureRandom(seed);
					int[] shuffledNodes = Shuffle.shuffleWithPriority(miningNodes, priorityCount, 0, q, rnd);
					
					for(int j = 0; j < q; j++) {
						counters[shuffledNodes[j]-1]++;
						greylist.add(new Point(i, shuffledNodes[j]));
						greylisted[shuffledNodes[j]-1] = true;
						
						if(leftGreyList[shuffledNodes[j]-1] == minVal) {
							leftGreyList[shuffledNodes[j]-1] = Integer.MAX_VALUE;
							if(!minIndexes.remove(new Integer(shuffledNodes[j]-1))) {
								System.out.println("failed to remove");
							}
//							System.out.println(minIndexes.size());
							if(minIndexes.isEmpty()) {
								minVal = Integer.MAX_VALUE;
								for(int k = 0; k < n; k++){
									if(minVal > leftGreyList[k]) {
										minVal = leftGreyList[k];
										minIndexes.clear();
										minIndexes.add(k);
									}
									else if(minVal == leftGreyList[k]) {
										minIndexes.add(k);
									}
								}
							}
						}
						else {
							leftGreyList[shuffledNodes[j]-1] = Integer.MAX_VALUE;
						}
						int debugCount = 0;
						for(int k = 0; k < n; k++) {
							if(leftGreyList[k] == minVal) debugCount++;
						}
						if(debugCount != minIndexes.size()) System.out.println("BUG");
					}
					
					headers.add(new BigInteger(256, rnd));
				}
				
				int minI = 0;
				int maxI = 0;
				int sum = 0;
				for(int i = 0; i < n; i++) {
//				System.out.printf("%d, ", counters[i]);
					if(counters[minI] > counters[i]) minI = i;
					if(counters[maxI] < counters[i]) maxI = i;
					sum += counters[i];
				}
				double average = ((double) sum) / n;
				System.out.printf("%d, %d, %f, ", n, q, average);
				for(int i = 0; i < n; i++) {
					System.out.printf("%d, ", counters[i]);
//				if(counters[minI] > counters[i]) minI = i;
//				if(counters[maxI] < counters[i]) maxI = i;
//				sum += counters[i];
				}
				
//			System.out.printf("%d, %d, %d, %d, %f\n",n,q,counters[minI],counters[maxI],average);
				System.out.println();
			}
		}

		long end = System.currentTimeMillis();
		System.out.println((end-start)/1000.0);
	}
}
