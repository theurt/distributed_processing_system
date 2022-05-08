package linda.test;

import linda.*;
import linda.shm.CentralizedLinda;

public class BasicTest1 {

    public static void main(String[] a) {
                
        final Linda linda = new linda.shm.CentralizedLinda();
        //final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
            
       //Thread s'exécutant en premier
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2);	//Pour la désynchro ??
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res = linda.take(motif);	//La requête doit attendre
                System.out.println("(4) Resultat:" + res);
                linda.debug("(4)");
                return;
            }
        }.start();
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(1) write: " + t1);
                linda.write(t1);

                Tuple t11 = new Tuple(4, 5);
                System.out.println("(2) write: " + t11);
                linda.write(t11);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(3) write: " + t2);
                linda.write(t2);

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(4) write: " + t3);
                linda.write(t3);
                                
                linda.debug("(4)");
                return;
            }
        }.start();
        try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ((CentralizedLinda) linda).shutdown();    }
}