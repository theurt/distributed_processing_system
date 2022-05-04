package linda.multiserver;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Callback;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.server.IRemoteCallback;
import linda.server.LindaServer;

public class LoadBalancer extends UnicastRemoteObject implements ILoadBalancer, LindaServer {
	private static final long serialVersionUID = 1L;
	private LindaMultiserver servers[];
	private Random rd = new Random();
	
	int nb_cores = Runtime.getRuntime().availableProcessors();
	ExecutorService pool = Executors.newFixedThreadPool(nb_cores);
	
	public LoadBalancer(int nServers) throws RemoteException {
		servers = new LindaMultiserver[nServers];
		for (int i=0; i<nServers; i++) {
			try {
				int port = 8080 + i;
				String address = "//localhost:" + port + "/aaa";
		        LocateRegistry.createRegistry(port);
		        LindaMultiserver server = new LindaMultiserver(nServers);
				Naming.bind(address, server);
				this.servers[i] = server;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int i=0; i<nServers; i++) {
			this.servers[i].setNext(this.servers[(i+1)%nServers]);
		}
	}
	
	public static void main(String[] args) {
		try {
			String address = args[0];
			Pattern r = Pattern.compile(".*localhost:([0-9]+).*");
			Matcher m = r.matcher(address);
			if (m.matches()) {
				int port = Integer.parseInt(m.group(1));
				LocateRegistry.createRegistry(port);
				int nServers = Integer.parseInt(args[1]);
				Naming.bind(address, new LoadBalancer(nServers));
			} else {
				System.out.println("No valid address given");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public LindaMultiserver pickServer() {
		return this.servers[this.rd.nextInt(this.servers.length)];	
	}
	
	public void wipe() {
		for (LindaMultiserver s : servers) {
			s.wipe();
		}
	}
	
	
	public void write(Tuple t) {
		this.pickServer().write(t);
	}

	public Tuple take(Tuple template) {
		return this.pickServer().take(template, 1);
	}

	public Tuple read(Tuple template) {
		return this.pickServer().read(template, 1);
	}

	public Tuple tryTake(Tuple template) {
		return this.pickServer().tryTake(template, 1);
	}

	public Tuple tryRead(Tuple template) {
		return this.pickServer().tryRead(template, 1);
	}

	public Collection<Tuple> takeAll(Tuple template) {
		return this.pickServer().takeAll(template, 1);
	}

	public Collection<Tuple> readAll(Tuple template) {
		return this.pickServer().readAll(template, 1);
	}

	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, IRemoteCallback callback) {
		this.pickServer().eventRegister(mode, timing, template, new CapsuleCallback(callback));
	}

	public void debug(String prefix) {
		for (LindaMultiserver s : servers) {
			System.out.println("--- Subserver " + s + " ---");
			s.debug(prefix);
			System.out.println();
		}
	}
	
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

	@Override
	public String getLogServer() throws RemoteException {
		return null;
	}

	@Override
	public ArrayList<Tuple> getCache() throws RemoteException {
		return null;
	}
}
