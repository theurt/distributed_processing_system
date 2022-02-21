package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import linda.Callback;
import linda.Tuple;
import linda.server.IRemoteCallback;

public class RemoteCallback extends UnicastRemoteObject implements IRemoteCallback, Callback {
	private static final long serialVersionUID = 1L;
	Callback cb;
	
	public RemoteCallback(Callback cb) throws RemoteException {
		this.cb = cb;
	}
	
	public void call(Tuple t) {
		try {
			cb.call(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}