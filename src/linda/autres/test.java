package linda.autres;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.server.LindaServerImpl;

public class test {

	public static void main(String[] args) {
		
		String address = args[0];
		Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
		Matcher m = r.matcher(address);
		if (m.matches()){
			int port = Integer.parseInt(m.group(5));
			try {
				LocateRegistry.createRegistry(port);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Naming.bind("rmi://" + address + "/ServerLinda", new LindaServerImpl());
			} catch (MalformedURLException | RemoteException | AlreadyBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("No valid address given");
		}

	}
}
