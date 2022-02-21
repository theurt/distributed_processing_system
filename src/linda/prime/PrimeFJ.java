package linda.prime;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import linda.Tuple;
import linda.shm.CentralizedLinda;

/** Primes, parallel version with recursive tasks and Fork/Join */
public class PrimeFJ {
	public static void main(String[] args) {
		int threshold = 5;		//Treshold determining an acceptable size for sequential computation of Erathosthene rule
		int max = 1000;			//Upper limit for primes
		int n = 100;			//Number of iteration executed
		try {
			threshold = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Starting with default threshold 5...");
		}
		try {
			max = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Starting with default upper limit 1000...");
		}
		try {
			n = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.out.println("Starting with default iteration number 100...");
		}
		
		ForkJoinPool fjp = new ForkJoinPool();		//Parallelism of tasks with possibility of stealing a task
        PrimeFJUnit.threshold = threshold;
        CentralizedLinda linda = new CentralizedLinda();
        boolean[] primes = new boolean[max+1];
        long[] times = new long[n];
        
        //See Prime Sequence to better understand the shape of the following loop
        for (int niter = 0; niter < n; niter++) {
			linda = resetLinda(linda);
			long tstart = System.nanoTime();
			try {
				primeCalculation(fjp, max, linda);
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
		
		fjp.shutdown();
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
	
	private static void primeCalculation(ForkJoinPool fjp, int max, CentralizedLinda linda) throws InterruptedException, ExecutionException {
		PrimeFJUnit tot = new PrimeFJUnit(2, max, max, linda);
		fjp.invoke(tot);
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
