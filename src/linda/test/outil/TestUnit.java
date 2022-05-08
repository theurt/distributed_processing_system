package linda.test.outil;

import java.rmi.RemoteException;

import linda.Linda;
import linda.server.LindaClient;
import linda.shm.CentralizedLinda;

public abstract class TestUnit {

	protected static Linda linda;
	 
	public TestUnit(Linda lin) {
		 this.linda = lin;
	}
	
	public void fin() {
		return;
	}
	public void test() {
		if(this.linda instanceof LindaClient) {
			((linda.server.LindaClient) linda).wipe();
			try {
				((linda.server.LindaClient) linda).shutdown();
				((linda.server.LindaClient) linda).restart();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(this.linda instanceof CentralizedLinda) {
			((CentralizedLinda) linda).shutdown();
			((CentralizedLinda) linda).restart();
		}

	}

}
