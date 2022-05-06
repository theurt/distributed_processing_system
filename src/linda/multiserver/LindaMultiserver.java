package linda.multiserver;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Tuple;
import linda.Callback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.server.LindaServerImpl;
import linda.shm.CentralizedLinda;

public class LindaMultiserver extends UnicastRemoteObject implements ILindaMultiserver {
	private static final long serialVersionUID = 1L;
	private CentralizedLinda linda = new CentralizedLinda();
	private String log;
	private int nServers;
	private ILindaMultiserver next;
	
	public static void main(String[] args) {
		try {
			String address = args[0];
			Pattern patternLocal = Pattern.compile(".*localhost:([0-9]+).*");
			Matcher matcheLocal = patternLocal.matcher(address);
			Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
    		Matcher m = r.matcher(address);
    		int nServ = Integer.parseInt(args[1]);			//Nombre de serveur

			if (matcheLocal.matches()) {
				int port = Integer.parseInt(matcheLocal.group(1));
				LocateRegistry.createRegistry(port);
				Naming.bind("rmi://" +address + "/ServerLinda", new LindaMultiserver(nServ));
    			System.out.println("Server started on " + address);
			} else if (m.matches()){
				int port = Integer.parseInt(m.group(5));
    			LocateRegistry.createRegistry(port);
    			Naming.bind("rmi://" + address + "/ServerLinda", new LindaMultiserver(nServ));
    			System.out.println("Server started on " + address);
			} else {
				System.out.println("No valid address given " + address);
			}
		}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	public LindaMultiserver(int nServers) throws RemoteException {
		this.nServers = nServers;
	}
	
	public void setNext(ILindaMultiserver next) {
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
				try {
					return this.next.take(template, distance+1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			return this.linda.take(template);
		}
		return null;
	}

	public Tuple read(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryRead(template);
			if (t != null) {
				return t;
			} else {
				try {
					return this.next.read(template, distance+1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			return this.linda.read(template);
		}
		return null;
	}

	public Tuple tryTake(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryTake(template);
			if (t != null) {
				return t;
			} else {
				try {
					return this.next.tryTake(template, distance+1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			return this.linda.tryTake(template);
		}
		return null;
	}

	public Tuple tryRead(Tuple template, int distance) {
		if (distance < nServers) {
			Tuple t = this.linda.tryRead(template);
			if (t != null) {
				return t;
			} else {
				try {
					return this.next.tryRead(template, distance+1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			return this.linda.tryRead(template);
		}
		return null;
	}

	public Collection<Tuple> takeAll(Tuple template, int distance) {
		Collection<Tuple> res = this.linda.takeAll(template);
		if (distance < nServers)
			try {
				res.addAll(this.next.takeAll(template, distance+1));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return res;
	}

	public Collection<Tuple> readAll(Tuple template, int distance) {
		Collection<Tuple> res = this.linda.readAll(template);
		if (distance < nServers)
			try {
				res.addAll(this.next.readAll(template, distance+1));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
