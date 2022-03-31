package linda.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import linda.Callback;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.CentralizedLinda;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LindaServerImpl extends UnicastRemoteObject implements LindaServer {
	private static final long serialVersionUID = 1L;
	private CentralizedLinda linda = new CentralizedLinda();
	//private String log;
	
	public LindaServerImpl() throws RemoteException {}
	
//	public String getLogServer() {
//		return this.log;
//	}
	
	public void wipe() {
		this.linda.reset();
	}
	
	
	public void write(Tuple t) {
		this.linda.write(t);
	}

	public Tuple take(Tuple template) {
		return this.linda.take(template);
	}

	public Tuple read(Tuple template) {
		return this.linda.read(template);
	}

	public Tuple tryTake(Tuple template) {
		return this.linda.tryTake(template);
	}

	public Tuple tryRead(Tuple template) {
		return this.linda.tryRead(template);
	}

	public Collection<Tuple> takeAll(Tuple template) {
		return this.linda.takeAll(template);
	}

	public Collection<Tuple> readAll(Tuple template) {
		return this.linda.readAll(template);
	}

	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, IRemoteCallback callback) {
		this.linda.eventRegister(mode, timing, template, new CapsuleCallback(callback));
	}

	public void debug(String prefix) {
		
		this.linda.debug(prefix);
		//this.log = this.linda.getLog();
	}
	
	public static void main(String[] args) {
		try {
			String address = args[0];
			Pattern r = Pattern.compile(".*localhost:([0-9]+).*");
			Matcher m = r.matcher(address);
			if (m.matches()) {;
				int port = Integer.parseInt(m.group(1));
				LocateRegistry.createRegistry(port);
				Naming.bind(address, new LindaServerImpl());
			} else {
				System.out.println("No valid address given");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//????
	private class CapsuleCallback implements Callback {
		IRemoteCallback cb;
		
		public CapsuleCallback(IRemoteCallback cb) {
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
}
