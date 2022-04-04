package linda.test;

import linda.*;

/*Test plus poussé avec plusieurs threads exécutant la même requête*/
public class BasicTest2 {

    public static void main(String[] a) {
        final Linda linda = new linda.shm.CentralizedLinda();
        //final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
              
        //Lecture de 3 tuples identiques dans linda
        for (int i = 5; i <= 7; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Tuple motif = new Tuple(Integer.class, String.class);
                    Tuple res = linda.read(motif);
                    System.out.println("("+j+") Resultat:" + res);
                    linda.debug("("+j+")");
                }
            }.start();
        }
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(0) write: " + t1);
                linda.write(t1);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(1) write: " + t2);
                linda.write(t2);

                linda.debug("(1)");

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(2) write: " + t3);
                linda.write(t3);
                                
                linda.debug("(2)");

            }
        }.start();
                
    }
}
