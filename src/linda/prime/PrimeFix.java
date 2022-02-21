package linda.prime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import linda.Tuple;
import linda.shm.CentralizedLinda;

/** V2 of Parallel primes ?? */
public class PrimeFix {
	public static void main(String[] args) {
		
		/** Number of threads ???? */
		int poolSize = 16;
		
		/** Number of tasks */
		int nTasks = 5;
		
		/** Upper limit for primes */
		int max = 1000;
		
		/** Number of iteration */
		int n = 100;
		try {
			poolSize = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Starting with default pool size 16...");
		}
		try {
			nTasks = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Starting with default task amount 5...");
		}
		try {
			max = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.out.println("Starting with default upper limit 1000...");
		}
		try {
			n = Integer.parseInt(args[3]);
		} catch (Exception e) {
			System.out.println("Starting with default iteration number 100...");
		}
	
		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
		long[] times = new long[n];
		boolean primes[] = new boolean[max+1];
		CentralizedLinda linda = new CentralizedLinda();
		linda.write(new Tuple(0, false));
		linda.write(new Tuple(1, false));
		
		for (int niter = 0; niter < n; niter++) {
			linda = resetLinda(linda);
			long tstart = System.nanoTime();
			try {
				linda = primeCalculation(pool, max, nTasks, linda);
			} catch (Exception e) {
				e.printStackTrace();
			}
			long tend = System.nanoTime();
			times[niter] = tend - tstart;
		}
		primes = getResult(linda, max);
		
		System.out.print("Result: [ ");
		for (int i = 0; i <= max; i++) {
			if (primes[i]) {
				System.out.print(i + " ");
			};
		}
		System.out.println("]");
		statistics(times);
		
		pool.shutdown();
		linda.shutdown();
	}
	
	private static CentralizedLinda resetLinda(CentralizedLinda linda) {
		linda.reset();
		linda.write(new Tuple(0, false));
		linda.write(new Tuple(1, false));;
		return linda;
	}
	
	private static boolean[] getResult(CentralizedLinda linda, int max) {
		boolean[] res = new boolean[max+1];
		Tuple t;
		
		for (int i = 0; i <= max; i++) {
			t = linda.tryRead(new Tuple (i, Boolean.class));
			if (t == null) {
				res[i] = true;
			}
		}
		
		return res;
	}
	
	private static CentralizedLinda primeCalculation(ExecutorService xs, int max, int nTasks, CentralizedLinda linda) throws InterruptedException, ExecutionException {
        int step = Math.max(1,max/nTasks);

        ArrayList<PrimeFixUnit> executees = new ArrayList<PrimeFixUnit>();
        for (int i=0; i<nTasks; i++) {
        	executees.add(new PrimeFixUnit(Math.max(i*step, 2), (i+1)*step, max, linda));
        }

        xs.invokeAll(executees);

		return linda;
	}
	
	private static void statistics(long[] times) {
		int n = times.length;
		Arrays.parallelSort(times);
		long[] nt = Arrays.copyOfRange(times, n/4, 3*n/4);
		System.out.println("Time Q1: " + Math.round(nt[0]/1000000) + "ms");
		System.out.println("Average: " + Math.round(Arrays.stream(nt).sum()/nt.length/1000000) + "ms");
		System.out.println("Time Q3: " + Math.round(nt[nt.length-1]/1000000) + "ms");
	}
}