package exp1;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import util.Shuffle;

public class Experiment4 {
	public static void main(String[] args) {
//		System.out.println("n, q, min, max, average");
		int[] vals = {10000};            
		double[] greylistProportions = {0.33};
		long start = System.currentTimeMillis();
		int numIterations = 100000;
//		double qPercentage = 0.40;
		
		double[] badThresh = new double[50];
		for(int i = 0; i < badThresh.length; i++) {
			badThresh[i] = ((double)(i+1))/100;
		}
		int counterSize = 256;
		int attemptedCompromisedSections = 1;
		double greylistProportion = greylistProportions[0];
		int n = vals[0];
		int q = ((int) Math.ceil(Math.log(n)/Math.log(2))) + 1;
		for(int l = 40; l < badThresh.length; l++) {
			double badNodePercent = badThresh[l];
//			System.out.printf("%f,", badNodePercent);
//			System.out.printf("averageTime,");
//			System.out.printf("maxTime,");
//			for(int i = 0; i < counterSize; i++) {
//				System.out.printf("%d,", i);
//			}
			for(int b = 0; b <= (q-1)/2; b++) {
				
				int[] counter = new int[counterSize];
	//			System.out.println(greylistProportion);
				for(int a = 0; a < vals.length; a++) {
					Queue<Point> greylist = new LinkedList<Point>();
					boolean greylisted[] = new boolean[n];
					Queue<BigInteger> headers = new LinkedList<BigInteger>();
					
					Random rnd = new SecureRandom();
					headers.add(new BigInteger(256, rnd));
					headers.add(new BigInteger(256, rnd));
					headers.add(new BigInteger(256, rnd));
					
					int qThresh = b;
					int nThresh = (int) (n * badNodePercent);
					if(nThresh*2 == n) nThresh--;
	//				System.out.pri 	ntln(nThresh);
	//				System.out.println(qThresh);
	//				System.out.println(nThresh);
					int maliciousBlocks = 0;
					int[] quorumSelection = null;
					int alpha = 3;
					boolean[] deferredQuorumSelection = new boolean[3];
					{
						int[][] q1 = new int[3][q];
						
						for(int i = 0; i < 3*q; i++) {
							q1[i/q][i%q] = i+1;
						}
						for(int i = 0; i < 3; i++) {
							deferredQuorumSelection[i] = true;				
						}
					}
					Queue<int[]> deferred = new LinkedList<int[]>();
					deferred.add(null);
					deferred.add(null);
					deferred.add(null);

					int minVal = 0;
					int[] leftGreyList = new int[n];
					LinkedList<Integer> minIndexes = new LinkedList<Integer>();
					int whiteListThreshold = 3*(n/q);
					boolean started = false;
					int[] priorityDistribution = new int[q+1];

					for(int i = 0; i < n; i++){
						minIndexes.add(i);
					}
					
					for(int i = 0; i < numIterations; i++) {
						if(!started) numIterations++;
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
							for(int j = 0; j < n - greylist.size(); j++) {
								if(leftGreyList[j] < i - whiteListThreshold) {
									miningNodes[priorityCount] = j+1;
//									System.out.println(priorityCount);
									priorityCount++;
									alreadyIn[j] = true;
								}
							}
						}
//						if(priorityCount != 0) System.out.println("Is this it? " + priorityCount);
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
						int k = 0;
						int count1 = 0;
						boolean goodQuorum = false;
						boolean isPrimary = true;
						int[] primaryQuorum = null;
						int[] shuffledNodes = miningNodes;
						boolean deferredQuorum0 = deferredQuorumSelection[i%3];
						boolean maliciousChoice = false;
						if(deferredQuorum0 == false) {
							if(k+q > shuffledNodes.length) {
						        ByteBuffer byteBuffer = ByteBuffer.allocate(shuffledNodes.length * 4);        
						        IntBuffer intBuffer = byteBuffer.asIntBuffer();
						        intBuffer.put(shuffledNodes);
	
						        byte[] array = byteBuffer.array();
						        
						        byte[] seed2 = new byte[seed.length+array.length];
						        for(int j = 0; j < seed.length; j++) {
						        	seed2[j] = seed[j];
						        }
						        for(int j = 0; j < array.length; j++) {
						        	seed2[j+seed.length] = array[j];
						        }
								rnd = new SecureRandom(seed2);
								k = 0;
							}
							shuffledNodes = Shuffle.shuffleWithPriority(shuffledNodes, priorityCount, k, q, rnd);
						}
						else
						{
							maliciousChoice = true;
//							int[][] validMiners = Shuffle.validMinersInBetaBlocks(i, whiteListThreshold, minVal, minIndexes, leftGreyList, headers, greylist, deferred, deferredQuorumSelection, greylisted, currentMiners, q, n, greylistProportion);
							int[] chosenQuorum = new int[q];
							int badCount2 = 0;
							if(priorityCount > q) priorityCount = q;

							for(int j = 0; j < priorityCount; j++) {
								
								chosenQuorum[j] = shuffledNodes[j];
								if(shuffledNodes[j] <= nThresh) {
									badCount2++;
								}
							}
//							if(priorityCount - badCount2 > qThresh) {
//								System.out.println(i + " fail to maintain :-) " + qThresh);
//							}
//							else {
////								System.out.println(i + " Controlling the quorum " + ((priorityCount - badCount2)));
//							}
							int firstOutsiderIndex = priorityCount;
							while(shuffledNodes[firstOutsiderIndex] <= nThresh) {
								if(shuffledNodes[firstOutsiderIndex] == 0) System.out.println("0 in valid miner list");
								firstOutsiderIndex++;
							}
							int modifiedQThresh = qThresh - badCount2;
							
							if(qThresh - badCount2 < firstOutsiderIndex - priorityCount) {
								int j;
//								System.out.printf("%d - %d\n", qThresh, badCount2);
								for(j = priorityCount; j < qThresh - badCount2; j++) {
									chosenQuorum[j] = shuffledNodes[j + priorityCount];
								}
								for(; j < q; j++){
									chosenQuorum[j] = shuffledNodes[j + firstOutsiderIndex];
								}
							}
							else {
								for(int j = priorityCount; j < q; j++) {
									chosenQuorum[j] = shuffledNodes[j + firstOutsiderIndex];
								}
							}
							shuffledNodes = chosenQuorum;
						}
						boolean errorFound = false;
						for(int j = 0; j < q; j++) {
							if(greylisted[shuffledNodes[j]-1] && !errorFound) {
								errorFound = true;
								System.out.println("ERROR " + i + " " + j + " " + " " + priorityCount + " " + maliciousChoice);
							}
						}
						if(!errorFound && maliciousChoice) {
//							System.out.println("fine " + i + " " + priorityCount + " " + maliciousChoice);
						}
//						if(!errorFound) System.out.println(i);
						if(isPrimary) {
							primaryQuorum = shuffledNodes;
						}
						int badCount = 0;
						for(int j = k; j < k+q; j++) {
							if(shuffledNodes[j] <= nThresh) badCount++;
						}
						if((badCount) >= q-qThresh) {
							if(started) maliciousBlocks++;	
							deferredQuorumSelection[i%3] = true;
						}
						else {
							deferredQuorumSelection[i%3] = false;
						}
							
	
						qThresh = b;
						for(int j = 0; j < q; j++) {
							greylist.add(new Point(i, primaryQuorum[j]));
							greylisted[primaryQuorum[j]-1] = true;
							if(minVal == leftGreyList[primaryQuorum[j]-1]){
								if(!minIndexes.remove(new Integer(primaryQuorum[j]-1))) {
									System.out.println("failed to remove 1 " + (primaryQuorum[j]-1));
									System.out.println(minIndexes.size());
								}
	
								if(minIndexes.isEmpty()) {
									started = true;
									minVal = Integer.MAX_VALUE;
									for(int l1 = 0; l1 < n; l1++){
										if(minVal > leftGreyList[l1]) {
											minVal = leftGreyList[l1];
											minIndexes.clear();
											minIndexes.add(l1);
										}
										else if(minVal == leftGreyList[l1]) {
											minIndexes.add(l1);
										}
									}
								}
							
							}
							leftGreyList[primaryQuorum[j]-1] = Integer.MAX_VALUE;
						}
						int[] miningQuorum = null;
						if(!isPrimary) {
							miningQuorum = new int[q];
							for(int j = 0; j < q; j++) {
								miningQuorum[j] = shuffledNodes[j+k];
							}
						}
						
						deferred.add(miningQuorum);
						
						int[] deferredQuorum = deferred.poll();
						if(deferredQuorum != null) {
							for(int j = 0; j < q; j++) {
								greylist.add(new Point(i, deferredQuorum[j]));
								greylisted[deferredQuorum[j]-1] = true;
								if(leftGreyList[deferredQuorum[j]-1] == minVal) {
									if(!minIndexes.remove(new Integer(deferredQuorum[j]-1))) {
										System.out.println("failed to remove 2 " + (deferredQuorum[j]-1));
									}
	
									if(minIndexes.isEmpty()) {
										started = true;
										minVal = Integer.MAX_VALUE;
										for(int l1 = 0; l1 < n; l1++){
											if(minVal > leftGreyList[l1]) {
												minVal = leftGreyList[l1];
												minIndexes.clear();
												minIndexes.add(l1);
											}
											else if(minVal == leftGreyList[l1]) {
												minIndexes.add(l1);
											}
										}
									}
								}

								leftGreyList[deferredQuorum[j]-1] = Integer.MAX_VALUE;
							}
						}
						headers.add(new BigInteger(256, rnd));
					}
	
					//System.out.printf("%d, ", n);
	//				if(counters[minI] > counters[i]) minI = i;
	//				if(counters[maxI] < counters[i]) maxI = i;
	//				sum += counters[i]}
					
					
	//			System.out.printf("%d, %d, %d, %d, %f\n",n,q,counters[minI],counters[maxI],average);
	
					System.out.printf("%f, ", ((double) maliciousBlocks)/numIterations);
					
//					for(int i = 0; i < counter.length; i++) {
//						System.out.printf("%d, ", counter[i]);
//					}
				}
			}
			System.out.println();
	
		}
		long end = System.currentTimeMillis();
		System.out.println((end-start)/1000.0);
	}
}

