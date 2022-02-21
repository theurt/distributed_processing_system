package linda.search.basic;

import java.util.UUID;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import linda.*;

/** The Manager drops the data to explore and the query */
public class Manager implements Runnable {

	/** Linda associated */
    private Linda linda;

    /**Id of the request. */
    private UUID reqUUID;
    
    /** Name of the dictionnary */
    private String pathname;
    
    /** Word to search */
    private String search;
    
    /** Distance between the best macth and the word requested */
    private int bestvalue = Integer.MAX_VALUE; // lower is better
    
    /** Best match with the word requested */
    private String bestresult;
    
    /** Number of threads searching for this request */
    private int nSearchers;
    
    /** ??? */
    public final static int streamLimit = 100000;

    /** Constructor */
    public Manager(Linda linda, String pathname, String search, int n) {
        this.linda = linda;
        this.pathname = pathname;
        this.search = search;
        this.nSearchers = n;		
    }

    /** Drop the query in linda */
    private void addSearch(String search) {
        
    	//Store data about the query
    	this.search = search;
        this.reqUUID = UUID.randomUUID();
        System.out.println("Query " + this.reqUUID + ": looking for \"" + this.search +"\"");
        
        //When the query is taken, execute the callback CbGetResult to get the best result
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, this.reqUUID, String.class, Integer.class), new CbGetResult());
        
        //Drop the Query nSearchers times (it will be executed faster ;) )
        for (int i = 0; i < nSearchers; i++) {
        	linda.write(new Tuple(Code.Request, this.reqUUID, this.search));
        }
    }

    /** Extract data from a dictionnary into linda */
    private void loadData(String pathname) {
        try (Stream<String> stream = Files.lines(Paths.get(pathname))) {
            stream.limit(streamLimit).forEach(s -> linda.write(new Tuple(Code.Value, s.trim())));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /** Get the result produced by Searcher (if it doesn't succeed after a certain timer, we interrupt the take call
     * and we give up the request */
    private void waitForEndSearch() {
    	
    	//Create a timer which can bypass the blocking take
    	Runnable timer = (new Thread(new ManagerTimer(this.reqUUID, this.linda)));
    	((Thread) timer).start();
        linda.take(new Tuple(Code.Searcher, "done", this.reqUUID));			    	//Non blocking call thanks to ManagerTimer (it writes the needed tuple
        																			//After a delay so the linda.take return )
        //linda.take(new Tuple(Code.Request, this.reqUUID, String.class)); // remove query
        //If the timer isn't finished we interrupt
        ((Thread) timer).interrupt();												
        linda.write(new Tuple(Code.Interrupt, this.reqUUID));
        System.out.println("Query " + this.reqUUID + " done");
        
        //Let's clean the requests/artifacts dropped in linda
        (new Thread(new GarbageCollector())).start();    
    }

    /** Help to make recursive request to find the best match possible */
    private class CbGetResult implements linda.Callback {
        public void call(Tuple t) {  // [ Result, ?UUID, ?String, ?Integer ]
        	
        	//Disassemble parameters from query-tuple
            String s = (String) t.get(2);				//Best match for the word
            Integer v = (Integer) t.get(3);				//Distance with this word
            
            //We want the smallest distance between the word requested and the one in linda
            if (v < bestvalue) {
                bestvalue = v;
                bestresult = s;
                System.out.println("New best (" + bestvalue + "): \"" + bestresult + "\"");
            }
            // Not the best match for the word requested, let's try again later (via callback)
            if (bestvalue > 0) {
            	linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, reqUUID, String.class, Integer.class), this);
            } else {
            	System.out.println(">>> Word found: " + search);
            	linda.write(new Tuple(Code.Interrupt, reqUUID)); 
            }
        }
    }

    public void run() {
    	//extract data into linda
        this.loadData(pathname);
        //Add the query into linda
        this.addSearch(search);
        this.waitForEndSearch();
    }
    
    
    /** Erase all the queries/artifacts written by the searches in linda */
    private class GarbageCollector implements Runnable {
    	private final static int delay = 1000;
    	public void run() {
    		try {
    			Thread.sleep(delay);
    			linda.takeAll(new Tuple(Code.Request, reqUUID, search));
    			linda.takeAll(new Tuple(Code.Searcher, "done", reqUUID));
    			linda.tryTake(new Tuple(Code.Interrupt, reqUUID));
    			try (Stream<String> stream = Files.lines(Paths.get(pathname))) {
    	            stream.limit(streamLimit).forEach(s -> linda.tryTake(new Tuple(Code.Value, s.trim())));
    	        } catch (java.io.IOException e) {
    	            e.printStackTrace();
    	        }
    		} catch (InterruptedException e) {
    			System.out.println("Garbage collector interrupted. Linda may contain obsolete data.");
    		}
    	}
    }
}
