package linda.search.basic;

import java.util.UUID;

import linda.Linda;
import linda.Tuple;

/** Useful to be sure that a request finished everytime */
public class ManagerTimer implements Runnable{
	private UUID reqUUID;
	private Linda linda;
	public final static int interval = 5000;
	
	public ManagerTimer(UUID id, Linda l) {
		this.reqUUID = id;
		this.linda = l;
	}
	
	public void run() {
		try {
			Thread.sleep(interval);
			linda.write(new Tuple(Code.Searcher, "done", this.reqUUID));		//Request expected by the take in the Manager
			System.out.println("Interrupted query " + reqUUID + " (timed out)");
		} catch (InterruptedException e) {
			
		}
	}
}
