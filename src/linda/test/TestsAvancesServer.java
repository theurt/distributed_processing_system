package linda.test;

import java.util.Collection;

import linda.*;

public class TestsAvancesServer {
	
	public static void main(String[] a) {
        final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
        ((linda.server.LindaClient) linda).wipe();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        new Thread() {  
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, String.class);
                Collection<Tuple> res = linda.readAll(motif);
                System.out.println("(1) Resultat readAll1 : " + res);
                
                Tuple res2 = linda.tryRead(motif);
                System.out.println("(2) Resultat tryRead1 : " + res2);
                
                Tuple res3 = linda.tryTake(motif);
                System.out.println("(3) Resultat tryTake1 : " + res3);
                
                Collection<Tuple> res4 = linda.takeAll(motif);
                System.out.println("(4) Resultat takeAll : " + res4);
                
                Collection<Tuple> res5 = linda.takeAll(motif);
                System.out.println("(5) Resultat takeAll2 : " + res5);
                
                Collection<Tuple> res6 = linda.readAll(motif);
                System.out.println("(6) Resultat readAll2 : " + res6);
                
                Tuple res7 = linda.tryRead(motif);
                System.out.println("(7) Resultat tryRead2 : " + res7);
                
                Tuple res8 = linda.tryTake(motif);
                System.out.println("(8) Resultat tryTake2 : " + res8);
                
                Tuple motif2 = new Tuple(Integer.class, Integer.class);
                Tuple res9 = linda.read(motif2);
                System.out.println("(9) Resultat read : " + res9);
                
                linda.debug("(9)");
            }
        }.start();
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(10) write: " + t1);
                linda.write(t1);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(11) write: " + t2);
                linda.write(t2);

                linda.debug("(11)");

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(12) write: " + t3);
                linda.write(t3);
                
                Tuple t4 = new Tuple(31102000, "Akina");
                System.out.println("(13) write: " + t4);
                linda.write(t4);
                
                Tuple t5 = new Tuple(666, "WillM");
                System.out.println("(14) write: " + t5);
                linda.write(t5);
                                
                linda.debug("(14)");

            }
        }.start();
                
    }
}