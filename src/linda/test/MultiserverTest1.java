package linda.test;

import linda.Linda;
import linda.Tuple;

public class MultiserverTest1 {
	public static void main(String[] args) {
		final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa", false);
		((linda.server.LindaClient) linda).wipe();
		
		new Thread() {
            public void run() {
            	linda.write(new Tuple(1, "foo"));
            	linda.write(new Tuple(1, "foo"));
                Tuple motif = new Tuple(Integer.class, String.class);
                System.out.println(linda.take(motif));
                System.out.println(linda.tryTake(motif));
                System.out.println(linda.tryTake(motif));
                linda.write(new Tuple(1, "foo"));
                linda.write(new Tuple(1, "foo"));
                System.out.println(linda.readAll(motif));
                System.out.println(linda.read(motif));
                System.out.println(linda.tryRead(motif));
                System.out.println(linda.takeAll(motif));
                System.out.println(linda.tryRead(motif));
                linda.debug("(1)");
            }
        }.start();
	}
}
