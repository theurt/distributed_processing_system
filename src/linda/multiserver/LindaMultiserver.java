package linda.multiserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;

import linda.Tuple;
import linda.Callback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.CentralizedLinda;

public class LindaMultiserver extends UnicastRemoteObject implements ILindaMultiserver {
	private static final long serialVersionUID = 1L;
	private CentralizedLinda linda = new CentralizedLinda();
	private String log;
	private int nServers;
	private LindaMultiserver next;
	
	public LindaMultiserver(int nServers) throws RemoteException {
		this.nServers = nServers;
	}
	
	public void setNext(LindaMultiserver next) {
		this.next = next;
	}
	
	public String getLogServer() {
		return this.log;
	}
	
	public void wipe() {
		this.linda.reset();
	}
	
	public void write(Tuple t) {
		this.linda.write(t);
	}

	public Tuple take(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryTake(template);
			if (t != null) {
				return t;
			} else {
				return this.next.take(template, distance+1);
			}
		} else {
			return this.linda.take(template);
		}
	}

	public Tuple read(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryRead(template);
			if (t != null) {
				return t;
			} else {
				return this.next.read(template, distance+1);
			}
		} else {
			return this.linda.read(template);
		}
	}

	public Tuple tryTake(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryTake(template);
			if (t != null) {
				return t;
			} else {
				return this.next.tryTake(template, distance+1);
			}
		} else {
			return this.linda.tryTake(template);
		}
	}

	public Tuple tryRead(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryRead(template);
			if (t != null) {
				return t;
			} else {
				return this.next.tryRead(template, distance+1);
			}
		} else {
			return this.linda.tryRead(template);
		}
	}

	public Collection<Tuple> takeAll(Tuple template, int distance) {
		Collection<Tuple> res = this.linda.takeAll(template);
		if (distance < nServers)
			res.addAll(this.next.takeAll(template, distance+1));
		return res;
	}

	public Collection<Tuple> readAll(Tuple template, int distance) {
		Collection<Tuple> res = this.linda.readAll(template);
		if (distance < nServers)
			res.addAll(this.next.readAll(template, distance+1));
		return res;
	}

	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		this.linda.eventRegister(mode, timing, template, callback);
	}

	public void debug(String prefix) {
		
		this.linda.debug(prefix);
		this.log = this.linda.getLog();
	}
	
	public ArrayList<Tuple> getCache() {
		return this.linda.getCache();
	}
}
