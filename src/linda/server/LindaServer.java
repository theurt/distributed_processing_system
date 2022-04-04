package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public interface LindaServer extends Remote {
	public void write(Tuple t) throws RemoteException;
	public Tuple take(Tuple template) throws RemoteException;
	public Tuple read(Tuple template) throws RemoteException;
	public Tuple tryTake(Tuple template) throws RemoteException;
	public Tuple tryRead(Tuple template) throws RemoteException;
	public Collection<Tuple> takeAll(Tuple template) throws RemoteException;
	public Collection<Tuple> readAll(Tuple template) throws RemoteException;
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, IRemoteCallback callback) throws RemoteException;
    //public void debug(String prefix)throws RemoteException;
	public String getLogServer()throws RemoteException;
	public void wipe()throws RemoteException;
	public ArrayList<Tuple> getCache() throws RemoteException;
}
