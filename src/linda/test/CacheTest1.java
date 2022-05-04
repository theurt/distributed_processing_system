package linda.test;

import linda.Linda;
import linda.Tuple;

public class CacheTest1 {
	
	public static void main(String[] args) {
	    final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
	        
	    new Thread() {
	        public void run() {
	        	linda.write(new Tuple(1, "a"));
	        	Tuple motif = new Tuple(Integer.class, String.class);
	        	linda.read(motif);
	        	//le motif devrait etre en cache
	        	linda.read(motif);
	        	linda.take(motif);
	        	linda.write(new Tuple(1, "a"));
	        	linda.tryRead(motif);
	        	linda.tryRead(motif);
	            linda.debug("(1)");
	        }
	    }.start();
	}
}
