package linda.prime;

import java.util.Arrays;

/** Algorithm which follows Erasthothene rules */
public class PrimeSequenceNaive {

	public static void main(String... args) {
		int max = 5000000;		//Arbitrary
		try {
			max = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Starting with default upper limit 5000000...");
		}
		boolean[] primes = new boolean[max + 1];		//Array with flags to indicate if it's a prime
		
		int n = 100;									//Number of iteration 
		try {
			n = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Starting with default iteration number 100...");
		}
		long[] times = new long[n];
		
		
		
		for (int niter = 0; niter < n; niter++) {
			Arrays.fill(primes,  true);
			primes[0] = false;
			primes[1] = false;
			
			long tstart = System.nanoTime();		//Mesure of time
			
			for (int i = 2; i <= max; i++) {
				if (primes[i]) {
					for (int k = 2; i*k <= max; k++) {
						primes[i*k] = false;
					}
				}
			}
			long tend = System.nanoTime();
			times[niter] = tend - tstart;
		}
		
		
		
		System.out.print("Result: [ ");
		for (int i = 0; i <= max; i++) {
			if (primes[i]) {
				System.out.print(i + " ");
			};
		}
		System.out.println("]");
		statistics(times);
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

