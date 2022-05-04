package linda.autres;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Linda;
import linda.server.LindaServer;
import linda.server.LindaServerImpl;

public class AbstractShell {

    /** URL du serveur */
    protected String nomServeur = "//localhost:8080/ServerLinda";
    
    /** Dev, Test ou Deploy */
    protected String mode;
   
	
    public AbstractShell(String mode,String nomServeur) {
    	this.mode = mode;
    	this.nomServeur = nomServeur;
	}
    
    public ShellType createShell(String type,Linda linda) {
    	
    	if(type.contentEquals("dev")) {
    		return (ShellType) new ShellDev(linda);
    	}else if(type.contentEquals("test")){
    		return (ShellType) new ShellTest(linda);
    	}else if(type.contentEquals("deploy")) {
    		return (ShellType) new ShellDeploy(linda);
    	}
		return null;
    }
	public void launch() {
		Scanner scanner = new Scanner(System.in);		//Fermé dans la boucle principale (cf Shell)
		String choix;
		boolean fin = false;
		while(!fin) {
			System.out.println("[" + this.mode + "]" + "Choisissez un type de version de linda [central|basic|cache|multiserver|quit] :");
			choix = scanner.nextLine();
			
			//Version centralisée
			if(choix.contentEquals("central")) {
				Linda lin = new linda.shm.CentralizedLinda();
				this.createShell(this.mode,lin).run();
			
			//Version serveur basique
			}else if(choix.contentEquals("basic")) {
				Linda lin = new linda.server.LindaClient(this.nomServeur,false);
				this.createShell(this.mode,lin).run();
				
			
			//Version serveur avec cache
			}else if(choix.contentEquals("cache")) {
				Linda lin = new linda.server.LindaClient(this.nomServeur,false);
				this.createShell(this.mode,lin).run();

				
			//Version multiserveur
			} else if (choix.contentEquals("multiserver")) {
				System.out.println("Non implémenté !");
				//TO DO : demander une liste de serveur et/ou lancer chaque serveur ?
			
			//Revenir au menu précédent
			} else if (choix.contentEquals("quit")) {
				fin = true;
			}else {
				System.out.println("Choix non reconnu !");
			}
		}

	}
}
