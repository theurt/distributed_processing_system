package linda.prime;
import java.util.concurrent.Callable;

import linda.Linda;
import linda.Tuple;

public class PrimeFixUnit implements Callable<Void>{
	/** Index of the first element to check */
	private int start;
	
	/** Index o the last element to check */
    private int end;
    
    /** Upper limit for determination of the primes */
    private int max;
    
    /** Linda platform */
    private Linda linda;
	
    /** Constructor */
	PrimeFixUnit(int s, int e, int m, Linda l) {
		start = s;
	    end = e;
	    max = m;
	    linda = l;
	    }
	
	@Override
	/** See PrimeSequence for details */
	public Void call() {
		Tuple t;
	    for (int i = start; i < end; i++) {
	    	t = linda.tryRead(new Tuple(i, Boolean.class));
	    	if (t == null || (Boolean) t.get(1)) {
	    		for (int k = 2; i*k <= max; k++) {
	    			linda.write(new Tuple(i*k, false));
				}
	    	}
	    }
    	return null;
	}
}