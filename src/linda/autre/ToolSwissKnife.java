package linda.autre;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import linda.autre.MenuSelectLinda;

/** Tool used for developpment, tests and deployment of applications on Linda Platform. */
public class ToolSwissKnife {

	/** Check if an IP adresse is accessible from the current computer on 
	 * which the tool is launched 
	 * @param address adress to check
	 */
	public static boolean isServerAccessible(String address) {
		
		//Does the adress is reachable ?
		try {
			address = "rmi://" + address + "/ServerLinda";
			Naming.lookup(address);
			return true;
		
		//Adress unknown from the current JVM
		}catch(NotBoundException e) {
			System.out.println("Il semble que le serveur "+ address + " soit inaccessible, contactez l'administrateur et demandez lui d'exécuter le script serverDeployment");
			return false;
			
		//Impossible to establish a connection with the adress
		}catch(RemoteException e ) {	
			System.out.println("Il semble que le serveur "+ address + " soit inaccessible, contactez l'administrateur et demandez lui d'exécuter le script serverDeployment");
			return false;
		
		//Other
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Distinguish local and distant IP adresses
	 * @param address adress to proceed
	 * @return String is it local (l), distant (d) or not an adress (e)
	 */
	public static String isLocal(String address) {
		
		//Regex to identify an adress of type "localhost:port"
		Pattern patternLocal = Pattern.compile(".*localhost:([0-9]+).*");
		Matcher matcheLocal = patternLocal.matcher(address);
		
		//Regex to identify an adress of type "XXX.XXX.XXX.XXX:port"
		Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
		Matcher m = r.matcher(address);
		
		//Local adress case
		if (matcheLocal.matches()) {
			return "l";
			
		//AnyType adress case
		} else if (m.matches()){
			return "d";
			
		//Error case
		} else {
			System.out.println("No valid address given " + address);
			return "e";
		}
	}
	
	/** Extract the port of an IP adress
	 * @param address adress to proceed
	 * @return int port 
	 */
	public static int getPort(String address) {
		
		//Regex to identify an adress of type "localhost:port"
		Pattern patternLocal = Pattern.compile(".*localhost:([0-9]+).*");
		Matcher matcheLocal = patternLocal.matcher(address);
		
		//Regex to identify an adress of type "XXX.XXX.XXX.XXX:port"
		Pattern r = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}):([0-9]+)");
		Matcher m = r.matcher(address);
		
		//Local adress case
		if (matcheLocal.matches()) {
			return Integer.parseInt(matcheLocal.group(1));
			
		//AnyType adress case
		} else if (m.matches()){
			return Integer.parseInt(m.group(5));
			
		} else {
			System.out.println("No valid address given " + address);
		}
		return 0;
	}
	
	/** Main method to launch the Swiss Knife tool */
	public static void main(String[] args) {
	   
		@SuppressWarnings("unused")
		boolean local = true;							//Local or distant server ?
		Scanner scanner = new Scanner(System.in);		//Scanner to read String written by user
		String choice;									//String written by user
		
		//Primary parsing to avoid useless computing of arguments
		if(args.length > 2) {
			System.out.println("Trop d'arguments !");
			scanner.close();
			return;
		}
		
		boolean centralizedVersion = Boolean.parseBoolean(args[0]);			//Do we have to use the tool for a centralized linda ?
		String address = null;												//Adress of a server containing linda implementation
		
		// "Normal" case => Linda is distributed on server(s) if centralized we don't care about the adress...
		if(!centralizedVersion) {
			
			address = args[1];

			//Is the server's adresse of the form localhost or normal IP ?
			switch(isLocal(address)) {
			case "l" : 
				local = true;
				break;
			case "d":
				local = false;
				break;
			default :
				scanner.close();
				return;
			}

			//Can we contact it ?
			if(! isServerAccessible(address)) {
				scanner.close();
				return;
			}
		}
		address = "rmi://" + address + "/ServerLinda";


		//Main Loop of Main Menu
		boolean end = false;	
		while(!end) {

			
			//Guideline for the user
			System.out.println("Choisissez un mode d'utilisation de l'outil [dev|test|deploy|quit] :");
			choice = scanner.nextLine();
			
			//Launch the next menu with adress of Linda'server
			
			//To launch interaction with linda
			if(choice.contentEquals("dev")) {
				new MenuSelectLinda("dev",address).launch();
			
			//To launch tests
			}else if(choice.contentEquals("test")) {
				new MenuSelectLinda("test",address).launch();

			//To deploy applications on linda Servers 
			}else if(choice.contentEquals("deploy")) {
				
				/** TO DO */
				new MenuSelectLinda("deploy",address).launch();

			//Leave the tool
			} else if (choice.contentEquals("quit")) {
				end = true;
			}else {
				System.out.println("choice non reconnu !");
			}
		}
		scanner.close();
	}
}
