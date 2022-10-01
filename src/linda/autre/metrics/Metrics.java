package linda.autre.metrics;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import linda.Linda;
import linda.Tuple;

/** Calcul of metrics  (see cacheMetrics for more comments )*/
public class Metrics {
	
	public static void main(String[] args) {
		boolean caching = true;						//cache version ?
		int nThreads = 10;							//number of threads
		int nRequests = 10000;						//number of requests per thread
		double takeF = .05;							//frequency of take
		double readAllF = 0;						//frequency of readAll
		Tuple[] tuples = new Tuple[] {new Tuple(1), new Tuple("a"), new Tuple(true), new Tuple(1,"a"), new Tuple("a",1), new Tuple(1,true), new Tuple(true,1), new Tuple("a",true), new Tuple(true,"a"), new Tuple(1,"a", true), new Tuple(1,true,"a"), new Tuple("a",1,true), new Tuple("a",true,1), new Tuple(true,1,"a"), new Tuple(true,"a",1)};
		
		//parse parameters
		if (args.length > 0) {
			try {
				nThreads = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching] [take frequency] [readAll frequency]");
			}
			
			if (args.length > 1) {
				try {
					nRequests = Integer.parseInt(args[1]);
				} catch (Exception e) {
					System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching] [take frequency] [readAll frequency]");
				}
				
				if (args.length > 2) {
					switch (args[2]) {
					case "false":
						caching = false;
						break;
					case "true":
						break;
					default:
						System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching] [take frequency] [readAll frequency]");
					}
					
					if (args.length > 3) {
						try {
							takeF = Double.parseDouble(args[3]);
							if (takeF >= .5)
								System.out.println("Warning: high take frequencies can hinder result reliability");
						} catch (Exception e) {
							System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching] [take frequency] [readAll frequency]");
						}
						
						if (args.length > 4) {
							try {
								readAllF = Double.parseDouble(args[4]);
								if (readAllF + 2 * takeF >= 1)
									System.out.println("Warning: refrain from using both high take and readAll frequencies for better results");
							} catch (Exception e) {
								System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching] [take frequency] [readAll frequency]");
							}
						}
					}
				}
			}
		}
		final Linda linda = new linda.server.LindaClient("//localhost:4000/ServerLinda", caching);
		((linda.server.LindaClient) linda).wipe();
		long time[] = new long[nThreads];
		CountDownLatch latch = new CountDownLatch(nThreads);
		
		final int reqPerThread = nRequests;
		
		for (Tuple t : tuples) {
			linda.write(t);
		}
		
		for (int i=0; i<nThreads; i++) {
			final int iThread = i;
			final double takeFreq = takeF;
			final double readAllFreq = readAllF;
			new Thread() {
		        public void run() {
	        		Random rd = new Random();
	        		Tuple t;
	        		int n = tuples.length;
	        		long start = System.nanoTime();
		        	for (int j=0; j<reqPerThread; j++) {
		        		t = tuples[rd.nextInt(n)];
		        		float r = rd.nextFloat();
		        		if (r < takeFreq) {
		        			t = linda.tryTake(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " took " + t);
		        		} else if (r < 2*takeFreq) {
		        			linda.write(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " wrote " + t);
		        		} else if (r < 2*takeFreq + readAllFreq) {
		        			linda.readAll(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " read all from " + t);
		        		} else {
		        			t = linda.tryRead(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " read " + t);
		        		}
		        		//System.out.println(iThread);
		        		if (reqPerThread >= 100 && (j % Math.round(reqPerThread/10)) == 0 && j > 0) {
		        			System.out.println("Thread " + iThread + " " + Math.round(100*j/reqPerThread) + "% done");
		        		}
		        	}
		        	long end = System.nanoTime();
		        	long diff = Math.round((end-start)/1000)/1000;
		        	time[iThread] = diff;
		        	System.out.println("Time taken by thread " + iThread + ": " + diff + "ms");
		        	latch.countDown();
		        }
		    }.start();
		}
		
		try {
			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long mean = 0;
		for (long e : time) {
			mean += e;
		}
		mean /= time.length;
		long meanR = Math.round(1e3 * reqPerThread * nThreads / mean);
		
		System.out.println();
		System.out.println("Average time per thread: " + mean + "ms");
		System.out.println("Average requests per second: " + meanR);
	}
}
