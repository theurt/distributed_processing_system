package linda.autre.application;

import java.util.Arrays;
import java.util.Scanner;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.autre.DeploymentApp;
import linda.search.basic.Code;
import linda.search.basic.Manager;
import linda.search.basic.Searcher;
import linda.shm.CentralizedLinda;

public class WordCountApplication extends DeploymentApp {
	
	/** Number of threads searching */
	private static final int nSearchers = 10;
	
	/** Number of threads per query */
	private static final int nSearchersPerQuery = 2;
	
	/** Boolean for the main loop (USELESS) */
	private boolean stop = false;
	
	/** To read what's written by user */
	Scanner scan = new Scanner(System.in);
	
	/** Constructor */
	public WordCountApplication(Linda lin) {

		super(lin);
		//Create new threads attached to the research
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Done), new CBRenewSearcher());
	}
	
	
	/** Called by a main method */
	@Override
	public void start(String[] args) {
		process(args);
		
		//Different threads to execute the same research
        for (int i = 0; i < nSearchers; i++) {
        	(new Thread(new Searcher(linda))).start();
        }
        // Main loop of the application
        while (!stop) {
        	awaitQuery();
        }
	}
	
	/** Launch executions of queries */
	private void process(String[] args) {
		int n = args.length - 1;
		if (n > 0) {
			String dictionary = args[n];
	        String[] searches = Arrays.copyOfRange(args, 0, n);
	        
	        // Concurrent dealing of the different queries
			for (String s : searches) {
				(new Thread(new Manager(linda, dictionary, s, nSearchersPerQuery))).start();
			}
		} else {
			System.out.println("Not enough input arguments");
			System.out.println("Usage: Main <query 1> ... <query n> <dictionary>");
		}
	}
	
	/** To get arguments input by the user */
	private void awaitQuery() {
		String args = scan.nextLine();
		String[] argsSplit = args.split(" ");
		process(argsSplit);
	}
	
	private class CBRenewSearcher implements Callback {
		public void call(Tuple t) {
			(new Thread(new Searcher(linda))).start();
			//Appel r�cursif infini ????
			linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Done), this);
		}
	}
}
