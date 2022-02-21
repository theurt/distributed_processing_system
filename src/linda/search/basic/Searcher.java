package linda.search.basic;

import linda.*;

import java.util.Arrays;
import java.util.UUID;

public class Searcher implements Runnable {

	/** Linda associated */
    private Linda linda;
    
    /** Indicate if the research has timed out */
    private boolean flag = true;	

    public Searcher(Linda linda) {
        this.linda = linda;
    }

    public void run() {
        System.out.println("Ready to do a search");
        
        //Take a request in linda (dropped by a Manager)
        Tuple treq = linda.take(new Tuple(Code.Request, UUID.class, String.class));
        UUID reqUUID = (UUID)treq.get(1);
        String req = (String) treq.get(2);
        Tuple tv;
        //System.out.println("Looking for: " + req);
        
        //Fire CBInterrupt if an Interrupt is read
        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Interrupt, reqUUID), new CBInterrupt());
        
        //Browse all Linda by searching some String value
        while ((tv = linda.tryTake(new Tuple(Code.Value, String.class))) != null && flag) {	//The flag marked if a code interrupt has been found
            String val = (String) tv.get(1);
            int dist = getLevenshteinDistance(req, val);		//Distance between two words
            if (dist < 10) { // arbitrary
                linda.write(new Tuple(Code.Result, reqUUID, val, dist));
            }
        }
        if (flag) {	//Diff
        	System.out.println("Searcher done after completing query");
        } else {
        	System.out.println("Searcher interrupted");
        }
        linda.write(new Tuple(Code.Searcher, "done", reqUUID));
        linda.write(new Tuple(Code.Done));
    }
    
    /*****************************************************************/

    /* Levenshtein distance is rather slow */
    /* Copied from https://www.baeldung.com/java-levenshtein-distance */
    static int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                                   + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                                   dp[i - 1][j] + 1, 
                                   dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }
    
	/** Make flag equals to false */
    private class CBInterrupt implements linda.Callback {			
        public void call(Tuple t) {
        	//System.out.println("Interrupting searcher...");
            flag = false;
        }
    }

}

