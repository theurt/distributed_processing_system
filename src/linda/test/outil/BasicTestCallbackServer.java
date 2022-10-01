package linda.test.outil;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

/*Test avec un callback que l'on réenregistre */
public class BasicTestCallbackServer extends TestUnit {

    public BasicTestCallbackServer(Linda lin) {
		super(lin);
		// TODO Auto-generated constructor stub
	}

    private static Tuple cbmotif;
    
    private static class MyCallback implements Callback {

		public void call(Tuple t) {
            System.out.println("CB got "+t);
    		//System.out.printf("EVENT REGISTER : Thread %s  with Id %d \n",Thread.currentThread().getName(), Thread.currentThread().getId());
            linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, this);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("CB done with "+t);
        }
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
		
        cbmotif = new Tuple(Integer.class, String.class);
        Callback callback = new MyCallback();
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, callback);

        Tuple t1 = new Tuple(4, 5);
        System.out.println("(2) write: " + t1);
        linda.write(t1);

        Tuple t2 = new Tuple("hello", 15);
        System.out.println("(2) write: " + t2);
        linda.write(t2);
        linda.debug("(2)");

        Tuple t3 = new Tuple(4, "foo");
        System.out.println("(2) write: " + t3);
        linda.write(t3);
        
        Tuple t4 = new Tuple(10, "foo");
        System.out.println("(2) write: " + t4);
        linda.write(t4);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        linda.debug("(2)");

    }

}