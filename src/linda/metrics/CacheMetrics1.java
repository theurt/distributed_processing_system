package linda.metrics;

import java.util.Random;

import linda.Linda;
import linda.Tuple;

public class CacheMetrics1 {
	
	public static void main(String[] args) {
		boolean caching = true;
		int nThreads = 2;
		int nRequests = 1000;
		Tuple[] tuples = new Tuple[] {new Tuple(1), new Tuple("a"), new Tuple(true), new Tuple(1,"a"), new Tuple("a",1), new Tuple(1,true), new Tuple(true,1), new Tuple("a",true), new Tuple(true,"a"), new Tuple(1,"a", true), new Tuple(1,true,"a"), new Tuple("a",1,true), new Tuple("a",true,1), new Tuple(true,1,"a"), new Tuple(true,"a",1)};
		
		if (args.length > 0) {
			try {
				nThreads = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching]");
			}
			
			if (args.length > 1) {
				try {
					nRequests = Integer.parseInt(args[1]);
				} catch (Exception e) {
					System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching]");
				}
				
				if (args.length > 2) {
					switch (args[2]) {
					case "false":
						caching = false;
						break;
					case "true":
						break;
					default:
						System.out.println("Usage: java linda.metrics.CacheMetrics1 [threads] [requests per thread] [use caching]");
					}
				}
			}
		}
		final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa", caching);
		long time[] = new long[nThreads];
		
		final int reqPerThread = nRequests;
		
		for (Tuple t : tuples) {
			linda.write(t);
		}
		
		for (int i=0; i<nThreads; i++) {
			final int iThread = i;
			new Thread() {
		        public void run() {
	        		Random rd = new Random();
	        		Tuple t;
	        		int n = tuples.length;
	        		long start = System.nanoTime();
		        	for (int j=0; j<reqPerThread; j++) {
		        		t = tuples[rd.nextInt(n)];
		        		float r = rd.nextFloat();
		        		if (r < .2) {
		        			linda.write(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " wrote " + t);
		        		} else if (r < .4) {
		        			t = linda.tryTake(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " took " + t);
		        		} else {
		        			t = linda.read(t);
		        			if (reqPerThread <= 10)
		        				System.out.println("Thread " + iThread + " read " + t);
		        		}
		        		//System.out.println(iThread);
		        		if (reqPerThread >= 100 && (j % Math.round(reqPerThread/10)) == 0 && j > 0) {
		        			System.out.println("Thread " + iThread + " " + Math.round(reqPerThread/j) + "% done");
		        		}
		        	}
		        	long end = System.nanoTime();
		        	long diff = Math.round((end-start)/1000)/1000;
		        	time[iThread] = diff;
		        	System.out.println("Time taken by thread " + iThread + ": " + diff + "µs");
		        }
		    }.start();
		}
	}
}
