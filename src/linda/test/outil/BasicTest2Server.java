package linda.test.outil;

import linda.*;

/*Test plus poussé avec plusieurs threads exécutant la même requête*/
public class BasicTest2Server extends TestUnit {

    public BasicTest2Server(Linda lin) {
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
		
        //Depot de 3 tuples identiques dans linda
        for (int i = 1; i <= 3; i++) {
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
                System.out.println("(0) write: " + t2);
                linda.write(t2);

                linda.debug("(0)");

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(0) write: " + t3);
                linda.write(t3);
                                
                linda.debug("(0)");

            }
        }.start();
                
    }
}
