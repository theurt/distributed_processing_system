package linda.test.outil;

import java.rmi.RemoteException;

import linda.*;

public class BasicTest1Server extends TestUnit {


    public BasicTest1Server(Linda lin) {
		super(lin);
	}

    @Override
	public void test(){
                
    	super.test();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
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
                System.out.println("(1) Resultat:" + res);
                linda.debug("(1)");
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
                System.out.println("(2) write: " + t1);
                linda.write(t1);

                Tuple t11 = new Tuple(4, 5);
                System.out.println("(3) write: " + t11);
                linda.write(t11);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(3) write: " + t2);
                linda.write(t2);

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(4) write: " + t3);
                linda.write(t3);
                                
                linda.debug("(FinTEST1)");

            }
        }.start();
                
    }
}