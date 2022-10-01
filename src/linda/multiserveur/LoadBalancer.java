package linda.multiserveur;

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
import linda.autre.ToolSwissKnife;
import linda.server.IRemoteCallback;
import linda.server.LindaServer;
import linda.server.LindaServerImpl;

public class LoadBalancer extends UnicastRemoteObject implements ILoadBalancer, LindaServer {
	private static final long serialVersionUID = 1L;
	private ILindaMultiserver servers[];
	private Random rd = new Random();
	
	int nb_cores = Runtime.getRuntime().availableProcessors();
	ExecutorService pool = Executors.newFixedThreadPool(nb_cores);
	
	public LoadBalancer(int nServers,String[] listeServ,boolean create) throws RemoteException {
		servers = new ILindaMultiserver[nServers];
		int i = 0;
		for (String nameServ : listeServ) {
			try {
				ILindaMultiserver server;
				
				//If we have to instantiate the server
				if (create) {
					String address = "rmi://" + nameServ + "/ServerLinda";
					int port = ToolSwissKnife.getPort(nameServ);
			        LocateRegistry.createRegistry(port);
			        server = new LindaMultiserver(nServers);
					Naming.bind(address, server);
					
				//Else we build a connection with it
				} else {
					server = (ILindaMultiserver) Naming.lookup("rmi://"+nameServ + "/ServerLinda");
				}
				this.servers[i] = (ILindaMultiserver) server;
				System.out.println("LoadBalancer launched " + nameServ);
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}

		for (int j=0; j<nServers; j++) {
			this.servers[j].setNext(this.servers[(j+1)%nServers]);
		}
	}
	
	public static void main(String[] args) {

		try {
			
			//Identify if the server is supposed to be local or distant
			String address = args[0];
			Pattern patternLocal = Pattern.compile(".*localhost:([0-9]+).*");
			Matcher matcheLocal = patternLocal.matcher(address);
			Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-clear"
					+ "9]{1,3}):([0-9]+)");
    		Matcher m = r.matcher(address);
    		int nServ = Integer.parseInt(args[1]);			//Number of servers

			String[] listeServ = new String[nServ];			//Containing the adresses of the slave servers
    		
			//If a list of slave servers is precised we build the connections
			boolean create = (args.length == 2);
    		if(!create) {
    			for (int i=0; i<nServ; i++) {
    				listeServ[i] = args[i+2];
    				if( !ToolSwissKnife.isServerAccessible(listeServ[i])) {
    					return;
    				}
       			}
    		//Else we create slave servers as locahosts
    		}else {
    			for (int i=0; i<nServ; i++) {
    					int port = 8080 + i;
    					listeServ[i] =  "localhost:" + port;
    			}
    					
    		}
    		
    		//create the loadBalancer server local or not
			if (matcheLocal.matches()) {
				int port = Integer.parseInt(matcheLocal.group(1));
				LocateRegistry.createRegistry(port);
				Naming.bind("rmi://" +address + "/ServerLinda", new LoadBalancer(nServ,listeServ,create));
    			System.out.println("Server started on " + address);
			} else if (m.matches()){
				int port = Integer.parseInt(m.group(5));
    			LocateRegistry.createRegistry(port);
    			Naming.bind("rmi://" + address + "/ServerLinda", new LoadBalancer(nServ,listeServ,create));
    			System.out.println("Server started on " + address);
			} else {
				System.out.println("No valid address given " + address);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(args[0]);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ILindaMultiserver pickServer() {
		return this.servers[this.rd.nextInt(this.servers.length)];	
	}
	
	public void wipe() {
		for (ILindaMultiserver s : servers) {
			try {
				s.wipe();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void shutdown() throws RemoteException {
		for (ILindaMultiserver s : servers) {
			try {
				s.shutdown();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void restart() throws RemoteException {
		for (ILindaMultiserver s : servers) {
			try {
				s.restart();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void write(Tuple t) {
		try {
			this.pickServer().write(t);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public Tuple take(Tuple template) {
		try {
			return this.pickServer().take(template, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Tuple read(Tuple template) {
		try {
			return this.pickServer().read(template, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Tuple tryTake(Tuple template) {
		try {
			return this.pickServer().tryTake(template, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Tuple tryRead(Tuple template) {
		try {
			return this.pickServer().tryRead(template, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Collection<Tuple> takeAll(Tuple template) {
		try {
			return this.pickServer().takeAll(template, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Collection<Tuple> readAll(Tuple template) {
		try {
			return this.pickServer().readAll(template, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, IRemoteCallback callback) {
		try {
			this.pickServer().eventRegister(mode, timing, template, new CapsuleCallback(callback));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void debug(String prefix) {
		for (ILindaMultiserver s : servers) {
			System.out.println("--- Subserver " + s + " ---");
			try {
				s.debug(prefix);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
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
