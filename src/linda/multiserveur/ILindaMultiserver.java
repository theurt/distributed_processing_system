package linda.multiserveur;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import linda.Tuple;
import linda.Callback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public interface ILindaMultiserver extends Remote {
	public void write(Tuple t) throws RemoteException;
	public Tuple take(Tuple template, int distance) throws RemoteException;
	public Tuple read(Tuple template, int distance) throws RemoteException;
	public Tuple tryTake(Tuple template, int distance) throws RemoteException;
	public Tuple tryRead(Tuple template, int distance) throws RemoteException;
	public Collection<Tuple> takeAll(Tuple template, int distance) throws RemoteException;
	public Collection<Tuple> readAll(Tuple template, int distance) throws RemoteException;
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) throws RemoteException;
    public void debug(String prefix)throws RemoteException;
	public String getLogServer()throws RemoteException;
	public void wipe()throws RemoteException;
	public void shutdown() throws RemoteException;
	public void restart() throws RemoteException;
	public void setNext(ILindaMultiserver next) throws RemoteException;
}
