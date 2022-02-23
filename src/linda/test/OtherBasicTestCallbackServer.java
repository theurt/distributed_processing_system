
package linda.test;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
/**
 * classe de tests avec Callback permanent pour les timing Immediate et Future dans les appels à eventRegister 
 * @author groupe G5
 */
public class OtherBasicTestCallbackServer {

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

    public static void testImmediate() {
    	System.out.println("\n----------------TESTS IMMEDIATE----------------\n");
		
        linda = new linda.server.LindaClient("//localhost:4000/aaa");
		((linda.server.LindaClient) linda).wipe();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
        Tuple t3 = new Tuple(4, "foo");
        System.out.println("(2) write: " + t3);
        linda.write(t3);
        
        cbmotif = new Tuple(Integer.class, String.class);
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, new MyCallback());
        
        Tuple t4 = linda.tryRead(t3);
        System.out.println("(2) TryRead: " + t4);	//renvoit null car eventRegister l'a retiré immédiatement
        
        Tuple t1 = new Tuple(4, 5);
        System.out.println("(2) write: " + t1);
        linda.write(t1);

        Tuple t2 = new Tuple("hello", 15);
        System.out.println("(2) write: " + t2);
        linda.write(t2);
        linda.debug("(2)");


        linda.debug("(2)");
    }
    	
    public static void testFuture() {
System.out.println("\n----------------TESTS FUTURE----------------\n");
		
		linda = new linda.shm.CentralizedLinda();
        // linda = new linda.server.LindaClient("//localhost:4000/MonServeur");


        Tuple t3 = new Tuple(4, "foo");
        System.out.println("(2) write: " + t3);
        linda.write(t3);
        
        cbmotif = new Tuple(Integer.class, String.class);
        linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, cbmotif, new MyCallback());

        Tuple t4 = linda.tryRead(t3);
        System.out.println("(2) TryRead: " + t4);
        
        Tuple t1 = new Tuple(4, 5);
        System.out.println("(2) write: " + t1);
        linda.write(t1);

        Tuple t2 = new Tuple("hello", 15);
        System.out.println("(2) write: " + t2);
        linda.write(t2);
        
        Tuple t5 = new Tuple(4, "bonsoir");
        System.out.println("(2) write: " + t5);
        linda.write(t5);
        System.out.println(linda.tryTake(cbmotif));
        System.out.println(linda.tryTake(cbmotif));
        linda.debug("(2)");

        linda.debug("(2)");
    }
    
    public static void main(String[] a) {
        testImmediate();
        testFuture();

    }

}
