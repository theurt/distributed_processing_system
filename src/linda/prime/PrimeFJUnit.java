package linda.prime;

import java.util.concurrent.RecursiveTask;

import linda.Linda;
import linda.Tuple;

/** Task run by a process */
public class PrimeFJUnit extends RecursiveTask<Void>{
	
	/** Id for serialization */
	private static final long serialVersionUID = 1L;
	
	/** Index of the first element to check */
	private int start;
	
	/** Index o the last element to check */
    private int end;
    
    /** Upper limit for determination of the primes */
    private int max;
    
    /** Linda platform */
    private Linda linda;
    
    /** Treshold determining an acceptable size for sequential computation of Erathosthene rule */
    static int threshold;

    /** Constructor */
    PrimeFJUnit(int s, int e, int m, Linda l) {
    	start = s;
    	end = e;
    	max = m;
    	linda = l;
    }

    protected Void compute() {
    	
    	//Size of the array to check
        int size = end-start;
        Tuple t;
        
        //Base case, we apply sequential Erasthothene rule on a small "array"
        if (size <= threshold) {
        	for (int i = Math.max(2, start); i<end; i++) {
        		t = linda.tryRead(new Tuple(i, Boolean.class));
	        	if (t == null || (Boolean) t.get(1)) {
					for (int k = 2; i*k <= max; k++) {
						linda.write(new Tuple(i*k, false));
					}
				}
            }
        	
        //Divide to conquer, recursive case, let's split the "array" in two
        } else {
        	int mid = (int) (end+start)/2;
        	//First part of the array in a first process
        	PrimeFJUnit f1 = new PrimeFJUnit(start, mid, max, linda);
        	
        	//First part of the array in a second process
        	PrimeFJUnit f2 = new PrimeFJUnit(mid, end, max, linda);
        	f1.fork();
        	f2.fork();
        	f1.join();
        	f2.join();
        }
        return null;
    }
}
