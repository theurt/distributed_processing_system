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

	/** Instance de linda à utiliser */
    protected Linda linda = null;

    /** URL du serveur */
    protected String nomServeur = "//localhost:8080/ServerLinda";
    
    /** Dev, Test ou Deploy */
    protected String mode;
    
    /** Client ou Server */
    protected String side;

	
    public AbstractShell(String mode) {
    	this.mode = mode;
    	this.side = "";
	}
   
    public AbstractShell(String mode,String side) {
    	this.mode = mode;
    	this.side = side;
	}
	
    
    
//    private void installerServeurDistant(String address) {
//    	try {
//    		//Vérifier que ça a la tête d'une IP
//			Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
//    		Matcher m = r.matcher(address);
//    		if (m.matches()) {;
//    			int port = Integer.parseInt(m.group(4));
//    			LocateRegistry.createRegistry(port);
//    			Naming.bind(address, new LindaServerImpl());
//    		} else {
//    			System.out.println("No valid address given");
//    		}
//    	}
//    	catch (Exception ex) {
//    		ex.printStackTrace();
//    	}
//    }
    
    //Bind avec sur un localhost 
//    private void installerServeurLocal(String address) {
//    	try {
//    		Pattern r = Pattern.compile(".*localhost:([0-9]+).*");
//    		Matcher m = r.matcher(address);
//    		if (m.matches()) {;
//    			int port = Integer.parseInt(m.group(1));
//    			LocateRegistry.createRegistry(port);
//    			Naming.bind(address, new LindaServerImpl());
//    		} else {
//    			System.out.println("No valid address given");
//    		}
//    	}
//    	catch (Exception ex) {
//    		ex.printStackTrace();
//    	}
//    }
    //Demander le nom d'un serveur actif
    private String demanderNom(boolean client) {
    	
    	Scanner scanner = new Scanner(System.in);
		String addresse = null;
		boolean fin = false;
		while(true) {
			System.out.println("Rentrer le serveur (IP + port) sous le format suivant [127.0.0.1:8080]");
			addresse = scanner.nextLine();
			//Vérifier que ça a la tête d'une IP
			Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
    		Matcher m = r.matcher(addresse);
    		if (m.matches()) {
    			addresse = "rmi://" + addresse + "/LindaServer" ;
    			if (client) {
	    			try {
	    				Naming.lookup(addresse);
	        			return addresse;
	    			}catch(NotBoundException e) {
	    				System.out.println("Adresse du serveur inconnue");
	    			}catch(RemoteException e ) {
	    				System.out.println("Adresse du serveur inconnue");
	    			}catch(Exception e) {
	    				e.printStackTrace();
	    			}
    			}
    			else {
    				return addresse;
    			}
    		}
    		else {
				// TODO Auto-generated catch block
    			System.out.println("Format incorrect");
    		}			
		}
	}
    
	public void launch() {
		Scanner scanner = new Scanner(System.in);		//Fermé dans la boucle principale (cf Shell)
		String choix;
		boolean fin = false;
		while(!fin) {
			System.out.println("[" + this.mode + "- " + this.side + "]" + "Choisissez un type de version de linda [central|basic|cache|multiserver|quit] :");
			choix = scanner.nextLine();
			
			//Version centralisée
			if(choix.contentEquals("central")) {
				this.linda = new linda.shm.CentralizedLinda();
				
			
			//Version serveur basique
			}else if(choix.contentEquals("basic")) {
				
				//Deployer chez le client
				if(mode.contentEquals("deploy") && side.contentEquals("client")) {
					
					//Demander le nom du serveur
					this.nomServeur = demanderNom(true);
					//Créer le client
					this.linda = new linda.server.LindaClient(this.nomServeur);
				
				//Installer le serveur et le lancer
				} else if(mode.contentEquals("deploy") && side.contentEquals("server")) {
					
					//Demander un nom pour le serveur (et un port)
					String nom = demanderNom(false);
					//Associer le serveur à ce nom et y lancer Linda
					String[] name = new String[1];
					name[0] = nom;
					LindaServerImpl.main(name);
				}
				//Pas en déploiement => serveur local par défaut et lancement client dans la même JVM
				else {
					//Associer le serveur à ce nom et y lancer Linda
					String[] name = new String[1];
					name[0] = this.nomServeur;
					LindaServerImpl.main(name);
				}
				
				
			
			//Version serveur avec cache
			}else if(choix.contentEquals("cache")) {
//				if(mode.contentEquals("deploy"))
//					this.nomServeur = demanderNom();
//				
//				//Alumage du serveur par défaut
//				else {
//					
//				}
//				this.linda = new linda.server.LindaClient("//localhost:4000/aaa");
				
			//Version multiserveur
			} else if (choix.contentEquals("multiserver")) {
				System.out.println("Non implémenté !");
				//TO DO : demander une liste de serveur et/ou lancer chaque serveur ?
			} else if (choix.contentEquals("quit")) {
				fin = true;
			}else {
				System.out.println("Choix non reconnu !");
			}
		}

	}
}
