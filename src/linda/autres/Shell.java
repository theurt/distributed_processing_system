package linda.autres;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaServerImpl;

public class Shell {

	public static void main(String[] args) {
	   
		//Identifier si le serveur est local ou non 
		String address = args[0];
		Pattern patternLocal = Pattern.compile(".*localhost:([0-9]+).*");
		Matcher matcheLocal = patternLocal.matcher(address);
		Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
		Matcher m = r.matcher(address);
		if (matcheLocal.matches()) {
			//local
		} else if (m.matches()){
			//distant
		} else {
			System.out.println("No valid address given");
		}
		
		//Le serveur est il joignable ?
		try {
			Naming.lookup("rmi://" + address + "/ServerLinda");
		}catch(NotBoundException e) {
				System.out.println("Adresse du serveur inconnue " + address);
				return ;
		}catch(RemoteException e ) {
				System.out.println("Adresse du serveur inconnue" + address);
				return ;
		}catch(Exception e) {
				e.printStackTrace();
				return;
		}
		
		//Boucle de l'interpréteur 
		Scanner scanner = new Scanner(System.in);
		String choix;
		boolean fin = false;
		while(!fin) {
			System.out.println("Choisissez un mode d'utilisation de l'outil [dev|test|deploy|quit] :");
			choix = scanner.nextLine();
			if(choix.contentEquals("dev")) {
				new AbstractShell("dev").launch();
			}else if(choix.contentEquals("test")) {
				new AbstractShell("test").launch();

			}else if(choix.contentEquals("deploy")) {
				boolean fin2 = false;
				while(!fin2) {
					System.out.println("Pour un système réparti, choisissez Client ou Server [client|server] :");
					choix = scanner.nextLine();
					if(choix.contentEquals("client")) {
						new AbstractShell("deploy","client").launch();
					}else if(choix.contentEquals("server")) {
						new AbstractShell("deploy","server").launch();
					}else {
						System.out.println("Choix non reconnu");
					}
				}

			} else if (choix.contentEquals("quit")) {
				fin = true;
			}else {
				System.out.println("Choix non reconnu !");
			}
		}
		scanner.close();
	}
}
