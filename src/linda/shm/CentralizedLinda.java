package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Shared memoire implementation of Linda. */
public class CentralizedLinda implements Linda {
	
	/** Shared memory of tuples. */
	private LinkedList<Tuple> memoire;
	
	/** Monitor for shared memory */
	private ReentrantLock monit = new ReentrantLock();
	//private Condition ecriture = monit.newCondition();				
	
	/** List of Events */
	private ArrayList<EventRegisterParam> events = new ArrayList<EventRegisterParam>();
	
	/** Number of cores */
	int nb_cores = Runtime.getRuntime().availableProcessors();
	
	/** TEST */
	private String log;
	
	/** Pool of threads */
	ExecutorService pool = Executors.newFixedThreadPool(nb_cores);
	
	/** Tuple + condition for monitory part */
	private static class Couple{
		Tuple tuple;
		Condition condition;
		public Couple(Tuple t, Condition c) {
			tuple = t;
			condition = c;
		}
	}
	
	/** Permet de g�rer les lectures sur des tuples qui ne sont pas encore pr�sents en m�moire */
	private HashMap<UUID, Couple> ecriture = new HashMap<>();
	
	/** Callbacks d'invalidation de cache */
	private ArrayList<Callback> cacheCallbacks = new ArrayList<Callback>();
	
	/** Taille du cache récupéré par les clients */
	private int cacheSize = 100;
	
	//classe statique contenant les paramètres d'un eventRegister
	private static class EventRegisterParam {
		private linda.Linda.eventMode mode;
		private Tuple template;
		private Callback callback;
		
		public EventRegisterParam(eventMode m, Tuple t, Callback cb) {
			this.mode = m;
			this.template = t;
			this.callback = cb;
		}
	} 
	
	/**Constructor */
    public CentralizedLinda() {
    	this.memoire = new LinkedList<Tuple>();
    }
   
   public String getLog() {
    	return this.log;
   }
    
    
    /** Adds a tuple t to the tuplespace. */
    public void write(Tuple t) {
    	
    	// Access critical section
    	monit.lock();
	//System.out.printf("WRITE : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

    	
    	try {
	    	//Writing in the memory 
	    	this.memoire.add(t);
	    	for(UUID id : ecriture.keySet()) {				//Ecriture = tuple en attente d'etre ecrit ? => A CONFIRMER
	    		if(t.matches(ecriture.get(id).tuple)) {				    //Tuple existing in memory
	    			
	    			//Si une tentative de read a �t� faite, on r�veille le thread concern� pour faire le read
	    			ecriture.get(id).condition.signal();    			
	    		}
	     	}
    	}
	finally {
    		//Leave critical section
       		monit.unlock();
		//System.out.printf("WRITE : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
       		//signalAll à changer : créer Condition dynamiquement ? 
	}
	//Call necessary callbacks
	searchForCallback(t);
    }

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple take(Tuple template) {
    	
    	//Access critical section
    	monit.lock();
	//System.out.printf("TAKE : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

		Tuple t;
    	try 
    	{
			Condition e1 = monit.newCondition();			//Condition for the monitor, associated with the tuple
			UUID id = UUID.randomUUID();					//New id for the tuple
			
			//Check all the memory
			while((t = getFromMemory(template)) == null) {
				try {
					ecriture.put(id, new Couple(template, e1));				//Permet d'attendre qu'une ecriture correspondant au tuple se fasse 
					//Wait until an event occur and release the monitor
					e1.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//Delete tuple from memory/buffer
			ecriture.remove(id);
			memoire.remove(t);
		}
		
		//Leave critical section
		finally {
			monit.unlock();
			//System.out.printf("TAKE : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

		}
    	this.invalidateCaches();
		return t;
    }

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple read(Tuple template) {
    	
    	monit.lock();
	//System.out.printf("READ : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
		Tuple t;
    	try {
			Condition e1 = monit.newCondition();
			UUID id = UUID.randomUUID();
			while((t = getFromMemory(template)) == null) {
				try {
					ecriture.put(id, new Couple(template, e1));
					e1.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ecriture.remove(id);
    	}
		finally 
		{
		monit.unlock();
		//System.out.printf("READ : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

		}
		
		return t.deepclone();
		
		//return this.read(template).deepclone()
	    //ON peut simplifier en faisant un deepclone sur un take ?

    };

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found. */
    public Tuple tryTake(Tuple template) {
    	monit.lock();
	//System.out.printf("TRYTAKE : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
		Tuple t; 
		try {
			t= getFromMemory(template);
			if(t != null) {
				memoire.remove(t);		//If tuple is null we return
				this.invalidateCaches();
			}
		}
		finally {
			monit.unlock();
			//System.out.printf("TRYTAKE : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
		}
		return t;
    };

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found. */
    public Tuple tryRead(Tuple template) {
    	monit.lock();
	//System.out.printf("TRYREAD : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
		Tuple t;
		try {
		t = getFromMemory(template);	//If tuple is null we return
		}
		finally {
			monit.unlock();
			//System.out.printf("TRYREAD : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

		}
		return t != null ? t.deepclone() : null;
    };

    /** Returns all the tuples matching the template and removes them from the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between takeAll and other methods;
     * for instance two concurrent takeAll with similar templates may split the tuples between the two results.
     */
    public Collection<Tuple> takeAll(Tuple template) {
    	monit.lock();
	//System.out.printf("TAKEALL : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
		Collection<Tuple> res;
		try {
			res = getAllFromMemory(template);
			if(!res.isEmpty()) {
				memoire.removeAll(res);
				this.invalidateCaches();
			}
		}
		finally {
			monit.unlock();
			//System.out.printf("TAKEALL : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

		}
		return res;
    };

    /** Returns all the tuples matching the template and leaves them in the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between readAll and other methods;
     * for instance (write([1]);write([2])) || readAll([?Integer]) may return only [2].
     */
    public Collection<Tuple> readAll(Tuple template) {
    	monit.lock();
	//System.out.printf("READALL : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
    	Collection<Tuple> res;
    	try {
			Collection<Tuple> tmp = getAllFromMemory(template);
			res = new ArrayList<Tuple>();
			for (Tuple tuple : tmp) {
				res.add(tuple.deepclone());
			}
    	}
    	finally 
    	{
    		monit.unlock();
		//System.out.printf("READALL : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

    	}
		return res;
    }

    /** Registers a callback which will be called when a tuple matching the template appears.
     * If the mode is Take, the found tuple is removed from the tuplespace.
     * The callback is fired once. It may re-register itself if necessary.
     * If timing is immediate, the callback may immediately fire if a matching tuple is already present; if timing is future, current tuples are ignored.
     * Beware: a callback should never block as the calling context may be the one of the writer (see also {@link AsynchronousCallback} class).
     * Callbacks are not ordered: if more than one may be fired, the chosen one is arbitrary.
     * Beware of loop with a READ/IMMEDIATE re-registering callback !
     *
     * @param mode read or take mode.
     * @param timing (potentially) immediate or only future firing.
     * @param template the filtering template.
     * @param callback the callback to call if a matching tuple appears.
     */
    
	public void eventRegister(linda.Linda.eventMode mode, linda.Linda.eventTiming timing, Tuple template, Callback callback) {
		if (mode == eventMode.CACHE) {
			this.cacheCallbacks.add(callback);
		} else {
			
			if(timing == eventTiming.IMMEDIATE) {
				//System.out.println("DEBUT LOCK");
				if (monit.isLocked()) {
					//System.out.printf("lock held by another thread, Thread %d can't take the lock\n", Thread.currentThread().getId());
				}
				Tuple t = null;
				monit.lock();
				//System.out.printf("EVENT : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
				try {
					t= getFromMemory(template);
				}
				finally {
					monit.unlock();		
					//System.out.printf("EVENT : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
	
				}
				if(t != null) {
					call(callback,mode,t.deepclone());
					//Interblocage ici, si le callback cherche à être recharger il a besoin du lock qui appartient au thread courant
				} else {
					monit.lock();
					events.add(new EventRegisterParam(mode, template, callback));
					monit.unlock();
				}
				
			} else {
				monit.lock();
				events.add(new EventRegisterParam(mode, template, callback));
				monit.unlock();
			}
		}

	}

    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
    public void debug(String prefix) {
    	monit.lock();
    	//System.out.printf("DEBUG : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

    	try {
	    	System.out.println(prefix + "Memory content:");
	    	log = new String();
	    	log = log + prefix + "Memory content:\n";
	    	for (Tuple t : memoire) {
	    		for (Object o : t) {
	    			System.out.print(o.toString() + " ");
	    			log = log + o.toString() + " ";
	    		}
	    		System.out.println();
	    		log = log +"\n";
	    	}
	    	System.out.println(prefix + "Registered callbacks:");
	    	log = log + prefix + "Registered callbacks:\n";
	    	for (EventRegisterParam e : events) {
	    		System.out.println(e.template + " " + e.mode);
	        	log = log + e.template + " " + e.mode + "\n";
	
	    	}
    	}
    	finally {
	    	monit.unlock();
		//System.out.printf("DEBUG : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());

    	}
    }
    
    /** To close thread Service Executor */
    public void shutdown() {
    	pool.shutdown();
    }
    
    /** Erase memory and LIFO of events */
    public void reset() {
    	memoire = new LinkedList<Tuple>();
    	events = new ArrayList<EventRegisterParam>();
    }
    
    public ArrayList<Tuple> getCache() {
    	ArrayList<Tuple> cache = new ArrayList<Tuple>();
    	monit.lock();
    	//Le cache ne peut pas être plus gros que la mémoire et il a aussi une taille limité
    	for (int i = 0; i < Math.min(this.cacheSize, this.memoire.size()); i++) {
    		cache.add(this.memoire.get(i));
    	}
    	monit.unlock();
    	return cache;
    }
    
    /* Prévenir tous clients que leur cache est invalide */
    private void invalidateCaches() {
    	Tuple t = new Tuple();
		for (Callback e : this.cacheCallbacks) {
			try {
				e.call(t);
			} catch (Exception ex) {
				//e.printStackTrace();
				System.out.println("A client cache may not have been reached and updated");
			}
		}
    }
    
    /**Fire a callback on a specific tuple */
    private void call(Callback callback, eventMode mode, Tuple tuple) {
		
		switch (mode) {
		case READ:
			break;
		case TAKE:
			monit.lock();
			//System.out.printf("CALL : Thread %s  with Id %d hold the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
			try {
				memoire.remove(tuple);
			}
			finally {
				monit.unlock();
				//System.out.printf("CALL : Thread %s  with Id %d free the lock\n",Thread.currentThread().getName(), Thread.currentThread().getId());
			}
			break;
		default:
			break;
		}
		//System.out.println("Appel callback");
		callback.call(tuple.deepclone()); //Pourquoi on le clone ???
		
	}
    
    /**Find a tuple matching the template in memory */
    private Tuple getFromMemory(Tuple template) {
		int memory_size = memoire.size();												//Total Size of the memory
		int chunks_size = Math.max(1,(int) Math.ceil((double) memory_size/nb_cores));	//Size for each thread

		ArrayList<Future<Tuple>> res = new ArrayList<Future<Tuple>>();
		
		//Browse in memory with all available/necessary threads the list of matching tuples
		for(int i = 0 ; i < Math.min(memory_size,nb_cores) ; i++) {			//If there are less tuples in memory than threads we don't need to use all of them
			int first = i*chunks_size;													//First indice of the block 
			int last =  (i+1)*chunks_size;												//Last indice of the block
			res.add(pool.submit(new Searcher(first > memory_size ? memory_size : first,
								last > memory_size ? memory_size : last,
								template)));
		}
		
		//Keep only the tuple which are really in memory
		Tuple found_tuple = null;
		for (Future<Tuple> f : res) {
			try {
				if (f.get() != null) {
					found_tuple = f.get();
					break;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		//Return the last tuple in memory matching the template (or null)
		return found_tuple;
	}
	
    /**Find all the tuples matching the template in memory */
	private Collection<Tuple> getAllFromMemory(Tuple template) {
		int memory_size = memoire.size();
		int chunks_size = Math.max(1,(int) Math.ceil((double) memory_size/nb_cores));

	    ArrayList<Future<Collection<Tuple>>> res = new ArrayList<Future<Collection<Tuple>>>();
	    for(int i = 0 ; i < Math.min(memory_size,nb_cores) ; i++) {
	        int first = i*chunks_size;
	        int last =  (i+1)*chunks_size;
	        res.add(pool.submit(new SearcherAll(first > memory_size ? memory_size : first, last > memory_size ? memory_size : last, template)));
		}
		Collection<Tuple> found_tuples = new ArrayList<Tuple>();
		try {
			for(Future<Collection<Tuple>> r : res) {
				found_tuples.addAll((Collection<Tuple>) r.get());
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return found_tuples;
	}
	
    /**Callback called to find a tuple in a part of a memory */
	class Searcher implements Callable<Tuple>{
		
		private Collection<Tuple> submemory;
		private Tuple template;
		
		Searcher(int first, int last, Tuple template){
			submemory = memoire.subList(first, last);
			this.template = template;
		}
		
		@Override
		public Tuple call() {
			Tuple res =  null; 
			for (Tuple t : submemory) {
				if(t.matches(template)) {
					res = t;
					break;
				}
			}
			return res;
		}
	}
	
    /**Callback called to find all the tuples in a part of a memory */
	class SearcherAll implements Callable<Collection<Tuple>>{
		
		private Collection<Tuple> submemory;
		private Tuple template;
		
		SearcherAll(int first,int last,Tuple template){
			monit.lock();
			try {
				submemory = memoire.subList(first, last);
			}
			finally 
			{
				monit.unlock();
			}
			this.template = template;
		}
		
		@Override
		public Collection<Tuple> call() {
			Collection<Tuple> res =  new ArrayList<Tuple>(); 
			for (Tuple t : submemory) {
				if(t.matches(template)) {
					res.add(t);
				}
			}
			return res;
		}
	}
	
	/** Call callback corresponding to a specific tuple */
	private void searchForCallback(Tuple t) {
		@SuppressWarnings("unchecked")
		
		//Iterate on a clone to avoid modificate the original LIFO
		Iterator<EventRegisterParam> it = ((Collection<EventRegisterParam>) events.clone()).iterator();
		EventRegisterParam e = null;
		while(it.hasNext()) {
			e = it.next();
			//We have found the target tuple
			if(t.matches(e.template)) {
				//Fire the callback
				monit.lock();
				call(e.callback,e.mode,t);
				monit.unlock();
				//Suppress event from LIFO
				//events.remove(e);
			}
		}
	}
}