package linda.test;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class MultiserverTest2 {
	private static Linda linda = new linda.server.LindaClient("//localhost:4000/aaa", false);
	private static Tuple cbmotif;
	
	private static class MyCallback implements Callback {
		public void call(Tuple t) {
            System.out.println("CB got "+t);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, this);
        }
    }
	
	public static void main(String[] args) {
		((linda.server.LindaClient) linda).wipe();
		
		cbmotif = new Tuple(Integer.class, String.class);
        Callback callback = new MyCallback();
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, callback);
        
        Tuple t = new Tuple(1, "foo");
        for (int i=0; i<30; i++) {
        	linda.write(t);
        	System.out.println("Wrote tuple " + i);
        	try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        linda.debug("");
	}
}
