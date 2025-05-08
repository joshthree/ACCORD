package exp1;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import util.Shuffle;

public class Experiment3_1 {
	public static void main(String[] args) {
//		System.out.println("n, q, min, max, average");
		int[] vals = {5000};            
		double[] greylistProportions = {0.33};
		long start = System.currentTimeMillis();
		int numIterations = 10;
		int numFolds = 10000;
//		double qPercentage = 0.40;
		double[] badThresh = {0.01, 0.02, 0.03, 0.04, 0.05 , 0.06, 0.07, 0.08, 0.09, .1, 0.2,0.3,0.4,0.5};
		int counterSize = 256;

		double greylistProportion = greylistProportions[0];
		int n = vals[0];
		
		int q = ((int) Math.ceil(Math.log(n)/Math.log(2))) + 1;
		for(int l = 0; l < badThresh.length; l++) {
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
					int timeSpent = 0;
					int maxTime = 0;

					int[] fullMiningNodesList = new int[n];
					for(int i = 0; i < n; i++) {
						fullMiningNodesList[i] = i+1;
					}
					
					
					Queue<int[]> deferred = new LinkedList<int[]>();
					deferred.add(null);
					deferred.add(null);
					deferred.add(null);
					for(int i = 0; i < numIterations*numFolds; i++) {
						/*ERROR CHECKING*/
	//				Object[] array = greylist.toArray();
	//				
	//
	//				for(int j = 1; j < array.length; j++){
	//					for(int k = 0; k < j; k++) {
	//						if(((Point) array[j]).y == ((Point) array[k]).y) System.out.println("PANIC!!  REPEAT NODES IN GREYLIST");
	//					}
	//				}
						if(i % numIterations == 0) {
							deferred = new LinkedList<int[]>();
							deferred.add(null);
							deferred.add(null);
							deferred.add(null);
							
							greylist = new LinkedList<Point>();
							for(int j = 0; j < greylisted.length; j++) {
								greylisted[j] = false;
							}
							
							int[] shuffledNodes = Shuffle.shuffle(fullMiningNodesList, 0, (((int) (n*greylistProportion))/q)*q, rnd);
							for(int j = 0; j < (((int) (n*greylistProportion))/q)*q; j++) {
								greylist.add(new Point(j/14, shuffledNodes[j]));
								greylisted[shuffledNodes[j]-1] = true;
							}
						}
						
						
						
						
						if(greylist.size() > greylistProportion * n)
						{
							
							Point p = greylist.poll();
							greylisted[p.y-1] = false;
							while(greylist.peek() != null && greylist.peek().x == p.x) {
								p = greylist.poll();
								greylisted[p.y-1] = false;
							}
							
						}
						int[] miningNodes = new int[n - greylist.size()];
						int count = 0;
						for(int j = 0; j < miningNodes.length; j++) {
							while(greylisted[count]) {
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
						int k = 0;
						int count1 = 0;
						boolean goodQuorum = false;
						boolean isPrimary = true;
						int[] primaryQuorum = null;
						int[] shuffledNodes = miningNodes;
						do {
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
							shuffledNodes = Shuffle.shuffle(shuffledNodes, k, q, rnd);
							if(isPrimary) {
								primaryQuorum = shuffledNodes;
							}
							int badCount = 0;
							for(int j = k; j < k+q; j++) {
								if(shuffledNodes[j] <= nThresh) badCount++;
							}
							if((badCount) > qThresh) {
								isPrimary = false;
								k += q;
								count1++;
								if(count1 % 3 == 0) {
									qThresh += 2;
									if(qThresh >= (q-1)/2) qThresh = (q-1)/2;								
								}
							}
							else goodQuorum = true;
							
							for(int j = 0; j < q; j++) {
								if(greylisted[shuffledNodes[j]-1]) System.out.println("ERROR");
							}
							
						} while(!goodQuorum);
	
						qThresh = b;
						for(int j = 0; j < q; j++) {
							greylist.add(new Point(i, primaryQuorum[j]));
							greylisted[primaryQuorum[j]-1] = true;
						}
						int[] miningQuorum = null;
						if(!isPrimary) {
							miningQuorum = new int[q];
							for(int j = 0; j < q; j++)
							{
								miningQuorum[j] = shuffledNodes[j+k];
							}
						}
						
						deferred.add(miningQuorum);
						
						int[] deferredQuorum = deferred.poll();
						if(deferredQuorum != null) {
							for(int j = 0; j < q; j++) {
								greylist.add(new Point(i, deferredQuorum[j]));
								greylisted[deferredQuorum[j]-1] = true;
							}
						}
						
						headers.add(new BigInteger(256, rnd));
						int numFailed = count1;
						if (numFailed >= counter.length) {
	//						System.out.println("Make array Bigger: "+ count1);
							numFailed = counter.length-1;
						}
						counter[numFailed]++;
						int iterationTime = (count1 + 1) * 20;
						if(count1 == 0) iterationTime = 10;
						timeSpent += iterationTime;
						if(iterationTime > maxTime)
							maxTime = iterationTime;
					}
	
					//System.out.printf("%d, ", n);
	//				if(counters[minI] > counters[i]) minI = i;
	//				if(counters[maxI] < counters[i]) maxI = i;
	//				sum += counters[i]}
					
					
	//			System.out.printf("%d, %d, %d, %d, %f\n",n,q,counters[minI],counters[maxI],average);
	
					System.out.printf("%f, ", ((double) timeSpent)/(numIterations*numFolds));
					
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

