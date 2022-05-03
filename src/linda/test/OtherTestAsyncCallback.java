package linda.test;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
/**
 * classe de tests avec Callback asynchrone pour les modes Take et Read dans les appels à eventRegister
 * @author groupe G5
 *
 */
public class OtherTestAsyncCallback {

    private static class MyCallback implements Callback {
        public void call(Tuple t) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("Got "+t);
        }
    }

    private static void testTake() {
    	System.out.println("\n----------------TESTS TAKE----------------\n");
    	Linda linda = new linda.shm.CentralizedLinda();
        // Linda linda = new linda.server.LindaClient("//localhost:4000/MonServeur");

        Tuple motif = new Tuple(Integer.class, String.class);
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, motif, new AsynchronousCallback(new MyCallback()));

       
        Tuple t1 = new Tuple(4, 5);
        System.out.println("(2) write: " + t1);
        linda.write(t1);        
        
        Tuple t2 = new Tuple("hello", 15);
        System.out.println("(3) write: " + t2);
        linda.write(t2);
        linda.debug("(3)");
        
        Tuple t3 = new Tuple(4, "foo");
        System.out.println("(4) write: " + t3);
        linda.write(t3);

        Tuple t4 = linda.tryRead(motif);
        System.out.println("(5) TryRead: " + t4);	//renvoit null car eventRegister l'a retiré
        
        linda.debug("(5)");

        Tuple t5 = linda.read(t1);
        System.out.println("(6) read: " + t5);
    }
    
    private static void testRead() {
    	System.out.println("\n----------------TESTS READ----------------\n");
    	Linda linda = new linda.shm.CentralizedLinda();
        // Linda linda = new linda.server.LindaClient("//localhost:4000/MonServeur");

        Tuple motif = new Tuple(Integer.class, String.class);
        linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, motif, new AsynchronousCallback(new MyCallback()));
      
        Tuple t1 = new Tuple(4, 5);
        System.out.println("(7) write: " + t1);
        linda.write(t1);        
        
        Tuple t2 = new Tuple("hello", 15);
        System.out.println("(8) write: " + t2);
        linda.write(t2);
        linda.debug("(8)");
        
        Tuple t3 = new Tuple(4, "foo");
        System.out.println("(9) write: " + t3);
        linda.write(t3);

        Tuple t4 = linda.tryRead(motif);
        System.out.println("(10) TryRead: " + t4);		//ne renvoit pas null: eventRegister ne l'a pas retiré
        
        linda.debug("(10)");

        Tuple t5 = linda.read(t1);
        System.out.println("(11) read: " + t5);
    }
    public static void main(String[] a) {
    	testTake();
    	testRead();
        
    	//NB : les Got 4 foo apparaissent à la fin à cause des callback asynchrone

    }

}