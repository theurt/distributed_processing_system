package linda.test;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.CentralizedLinda;

/*Test avec un callback que l'on réenregistre */
public class BasicTestCallback {

    private static Linda linda;
    private static Tuple cbmotif;
    
    private static class MyCallback implements Callback {

		public void call(Tuple t) {
            System.out.println("CB got "+t);
            linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, this);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("CB done with "+t);
        }
    }

    public static void main(String[] a) {
        linda = new linda.shm.CentralizedLinda();
        //linda = new linda.server.LindaClient("//localhost:4000/aaa");

        cbmotif = new Tuple(Integer.class, String.class);
        Callback callback = new MyCallback();
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, callback);

        Tuple t1 = new Tuple(4, 5);
        System.out.println("(1) write: " + t1);
        linda.write(t1);

        Tuple t2 = new Tuple("hello", 15);
        System.out.println("(2) write: " + t2);
        linda.write(t2);
        linda.debug("(2)");

        Tuple t3 = new Tuple(4, "foo");
        System.out.println("(3) write: " + t3);
        linda.write(t3);
        
        Tuple t4 = new Tuple(10, "foo");
        System.out.println("(4) write: " + t4);
        linda.write(t4);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        linda.debug("(4)");

        try {
			Thread.sleep(10000);
	        ((CentralizedLinda) linda).shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}