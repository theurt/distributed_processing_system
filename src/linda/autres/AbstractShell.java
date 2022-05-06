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
    
    /* Frabique de shell */
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
    
    /* Lance le menu de choix du type de linda (et lance le shell associé ) */
	public void launch() {
		Scanner scanner = new Scanner(System.in);		//Fermé dans la boucle principale (cf Shell)
		String choix;
		boolean fin = false;		
		if(this.nomServeur != null)
			while(!fin) {
				System.out.println("[" + this.mode + "]" + "Choisissez un type de version de linda [basic|cache|multiserver|quit] :");
				choix = scanner.nextLine();
				
				//Version serveur basique (pas de cache, pas de multiserveur )
				if(choix.contentEquals("basic")) {
					Linda lin = new linda.server.LindaClient(this.nomServeur,false);
					this.createShell(this.mode,lin).run();
					
				
				//Version serveur avec cache
				}else if(choix.contentEquals("cache")) {
					Linda lin = new linda.server.LindaClient(this.nomServeur,true);
					this.createShell(this.mode,lin).run();
	
					
				//Version multiserveur
				} else if (choix.contentEquals("multiserver")) {
					System.out.println("Avant de vous demander pourquoi ça ne marche pas, pensez à utiliser les scripts"
							+ "loadBalancerDeployment et éventuellement serverSlaveDeployment"
							+ " et assurez vous d'avoir lancer l'outil avec l'adresse d'un serveur LoadBalancer");
					boolean finLocal = false;
					Scanner sc = new Scanner(System.in);
					boolean suite = false;
					while(!finLocal) {
						System.out.println("Si vous êtes confiant tapez yes sinon tapez no ");
						String choix2 = sc.nextLine();
						if(choix2.contentEquals("yes")) {
							finLocal = true;
							suite = true;
						}else if (choix2.contentEquals("no")){
							finLocal = true;
							suite = false;
						}else {
							System.out.println("Mot non reconnu");
						}
					}
					
					if(suite) {
						Linda lin = new linda.server.LindaClient(this.nomServeur,false);
						this.createShell(this.mode,lin).run();
					}
				
				//Revenir au menu précédent
				} else if (choix.contentEquals("quit")) {
					fin = true;
				}else {
					System.out.println("Choix non reconnu !");
				}
			}
		//Version centralisée
		else {
			Linda lin = new linda.shm.CentralizedLinda();
			this.createShell(this.mode,lin).run();
		}
	}
	
}
