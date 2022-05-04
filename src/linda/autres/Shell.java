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
	   
		boolean local = true;							//Serveur local ou distant
		Scanner scanner = new Scanner(System.in);		//Scanner pour la lecture de string rentrées par l'utilisateur
		String choix;
		
		//Identifier si le serveur est local ou non 
		String address = args[0];
		Pattern patternLocal = Pattern.compile(".*localhost:([0-9]+).*");
		Matcher matcheLocal = patternLocal.matcher(address);
		Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
		Matcher m = r.matcher(address);
		if (matcheLocal.matches()) {
			local = true;
		} else if (m.matches()){
			local = false;
		} else {
			System.out.println("No valid address given");
			return;
		}
		
		//Le serveur est il joignable ?
		try {
			address = "rmi://" + address + "/ServerLinda";
			Naming.lookup(address);
		}catch(NotBoundException e) {
				if (local)
					System.out.println("Il semble que vous n'ayez pas lancer le serveur sur " + address + "essayez d'utiliser le script serverDeployment et réessayez");
				else {
					System.out.println("Il semble que le serveur "+ address + " soit inaccessible, contactez l'administrateur et demndez lui d'exécuter le script serverDeployment");
				}
				return ;
		}catch(RemoteException e ) {
			if (local)
				System.out.println("Il semble que vous n'ayez pas lancer le serveur sur " + address + "essayez d'utiliser le script serverDeployment et réessayez");
			else {
				System.out.println("Il semble que le serveur "+ address + " soit inaccessible, contactez l'administrateur et demndez lui d'exécuter le script serverDeployment");
			}				return ;
		}catch(Exception e) {
				e.printStackTrace();
				return;
		}

		//Boucle principale du menu initial
		boolean fin = false;
		while(!fin) {
			System.out.println("Choisissez un mode d'utilisation de l'outil [dev|test|deploy|quit] :");
			choix = scanner.nextLine();
			
			//Pour tester interactivement Linda
			if(choix.contentEquals("dev")) {
				new AbstractShell("dev",address).launch();
			
			//Pour lancer des tests
			}else if(choix.contentEquals("test")) {
				System.out.println("pas encore");
				//new AbstractShell("test",address).launch();

			//Pour lancer des applications 
			}else if(choix.contentEquals("deploy")) {
				System.out.println("pas encore");
				//new AbstractShell("deploy",address).launch();

			//Pour quitter l'outil
			} else if (choix.contentEquals("quit")) {
				fin = true;
			}else {
				System.out.println("Choix non reconnu !");
			}
		}
		scanner.close();
	}
}
