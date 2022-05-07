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

	/** Couleurs pour le terminal */
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
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
					System.out.println(ANSI_BLUE + "Avant de vous demander pourquoi ça ne marche pas, pensez à utiliser les scripts "
							+ "loadBalancerDeployment et éventuellement serverSlaveDeployment pour déployer linda version Multiserver"
							+ " et assurez vous d'avoir lancer l'outil avec l'adresse du serveur LoadBalancer" + ANSI_RESET);
					boolean finLocal = false;
					Scanner sc = new Scanner(System.in);
					boolean suite = false;
					while(!finLocal) {
						System.out.println("Si vous êtes confiant tapez yes sinon tapez no :");
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
