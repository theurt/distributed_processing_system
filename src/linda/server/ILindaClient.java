package linda.server;

import java.rmi.RemoteException;
import java.util.Collection;

import linda.Callback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public interface ILindaClient {
	public void write(Tuple t);
	public Tuple take(Tuple template);
	public Tuple read(Tuple template);
	public Tuple tryTake(Tuple template);
	public Tuple tryRead(Tuple template);
	public Collection<Tuple> takeAll(Tuple template);
	public Collection<Tuple> readAll(Tuple template);
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback);
	public void debug(String prefix);
	public void wipe() throws RemoteException;
	public void shutdown() throws RemoteException;
	public void restart() throws RemoteException;
	public void refreshCache();
}
