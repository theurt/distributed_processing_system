package linda.prime;

import java.util.Arrays;

import linda.Tuple;
import linda.shm.CentralizedLinda;

/** Version of primes with Linda */
public class PrimeSequence {

	public static void main(String... args) {
		int max = 1000;
		try {
			max = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Starting with default upper limit 1000...");
		}
		boolean[] primes = new boolean[max + 1];
		
		int n = 100;
		try {
			n = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Starting with default iteration number 100...");
		}
		long[] times = new long[n];
		CentralizedLinda linda = new CentralizedLinda();
		linda.write(new Tuple(0, false));
		linda.write(new Tuple(1, false));
		Tuple t;
		
		for (int niter = 0; niter < n; niter++) {
			linda = resetLinda(linda);		// Initially only 0 and 1 exist
			
			long tstart = System.nanoTime();
			
			for (int i = 2; i <= max; i++) {
				t = linda.tryRead(new Tuple(i, Boolean.class));
	        	if (t == null || (Boolean) t.get(1)) {		//If t== null it means that this case is empty, t;get(1) means we haven't visited it yet
					for (int k = 2; i*k <= max; k++) {
						linda.write(new Tuple(i*k, false));
					}
				}
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
		linda.shutdown();
	}
	
	/** Reset properly Linda */
	private static CentralizedLinda resetLinda(CentralizedLinda linda) {
		linda.reset();
		linda.write(new Tuple(0, false));
		linda.write(new Tuple(1, false));;
		return linda;
	}
	
	/** Create an array with the tuples in Linda */
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
	
	/** Display stats about execution */
	private static void statistics(long[] times) {
		int n = times.length;
		Arrays.parallelSort(times);
		long[] nt = Arrays.copyOfRange(times, n/4, 3*n/4);
		System.out.println("Time Q1: " + Math.round(nt[0]/1000000) + "ms");
		System.out.println("Average: " + Math.round(Arrays.stream(nt).sum()/nt.length/1000000) + "ms");
		System.out.println("Time Q3: " + Math.round(nt[nt.length-1]/1000000) + "ms");
	}
}

