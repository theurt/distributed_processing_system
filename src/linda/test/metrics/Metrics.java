package linda.test.metrics;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import linda.shm.CentralizedLinda;

public class Metrics {
	
	
	public static void test(boolean caching, int nThreads, int nRequests, double takeF, double readAllF, Linda linda) {
	
		Tuple[] tuples = new Tuple[] {new Tuple(1), new Tuple("a"), new Tuple(true), new Tuple(1,"a"), new Tuple("a",1), new Tuple(1,true), new Tuple(true,1), new Tuple("a",true), new Tuple(true,"a"), new Tuple(1,"a", true), new Tuple(1,true,"a"), new Tuple("a",1,true), new Tuple("a",true,1), new Tuple(true,1,"a"), new Tuple(true,"a",1)};
		
		//Nettoyer linda
		if(linda instanceof LindaClient) {
			((linda.server.LindaClient) linda).wipe();
			try {
				((linda.server.LindaClient) linda).shutdown();
				((linda.server.LindaClient) linda).restart();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(linda instanceof CentralizedLinda) {
			((CentralizedLinda) linda).shutdown();
			((CentralizedLinda) linda).restart();
		}
		
		
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
