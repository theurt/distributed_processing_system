package linda.autre;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Linda;

/** Menu allowing the choice of a Linda's version */
public class MenuSelectLinda {

	/** To colorate the terminal */
	public static final String ANSI_BLUE = "\u001B[34m";

	/** Bring back previous color of the terminal */
	public static final String ANSI_RESET = "\u001B[0m";

    /** Server's URL */
    protected String serverName = "//localhost:8080/ServerLinda";
    
    /** Dev, Test ou Deploy */
    protected String mode;
 
    /** Constructor */
    public MenuSelectLinda(String mode,String serverName) {
    	this.mode = mode;
    	this.serverName = serverName;
	}
    
    /** Fabric of Menu
     * @param type type of menu wanted (dev,test or deploy)
     * @param linda instanceof linda to use
     * @param cache using of the cache ? (for instances of linda affected 
     * @return ShellType type of Menu created*/
    public MenuType createShell(String type,Linda linda,boolean cache) {
    	
    	if(type.contentEquals("dev")) {
    		if(linda==null)
    			System.err.println("souci");
    		return (MenuType) new MenuDev(linda);
    	}else if(type.contentEquals("test")){
    		return (MenuType) new MenuTest(linda,cache);
    	}else if(type.contentEquals("deploy")) {
    		return (MenuType) new MenuDeploy(linda);
    	}
		return null;
    }
    
	/** Detect if input string is int, bool, une string, Integer.class, String.class, Boolean.class (used in subclass)
	 * @param token token to identify 
	 * @return String string identifying the type of the token*/
	public static String typeOfToken(String token) {
		Pattern patternInt = Pattern.compile("([0-9]*)");
		Pattern patternStr = Pattern.compile("\"[^\"]+\"");;
		Pattern patternBool = Pattern.compile("(true|false)");
		
		Matcher mInt = patternInt.matcher(token);
		Matcher mStr = patternStr.matcher(token);
		Matcher mBool = patternBool.matcher(token);
	
		if(mStr.matches()) {
			return "str";
		}else if(mInt.matches()) {
			return "int";
		}else if(mBool.matches()) {
			return "bool";
		}else if(token.contentEquals("String.class")) {
			return "?String";
		}else if(token.contentEquals("Boolean.class")) {
			return "?Boolean";
		}else if(token.contentEquals("Integer.class")) {
			return "?Integer";
		}else {
			return "autre";
		}
	}
	
    /** Main loop of the Menu */
	public void launch() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);		//scanner to read user (closed in Main menu )
		String choice;									//string written by user	
		boolean end = false;							//To control the loop's end	
		
		//Distributed case
		if(this.serverName != null)
			
			//Main loop
			while(!end) {
				
				System.out.println("[" + this.mode + "]" + "Choisissez un type de version de linda [basic|cache|multiserver|quit] :");
				choice = scanner.nextLine();
				
				//Basic version of linda (no cache, no multiserver )
				if(choice.contentEquals("basic")) {
					Linda lin = new linda.server.LindaClient(this.serverName,false);
					this.createShell(this.mode,lin,false).run();
					
				
				//Version with cache
				}else if(choice.contentEquals("cache")) {
					Linda lin = new linda.server.LindaClient(this.serverName,true);
					this.createShell(this.mode,lin,true).run();
	
					
				//Multieserver version
				} else if (choice.contentEquals("multiserver")) {
					
					//Warning message (in blue :) )
					System.out.println(ANSI_BLUE + "Avant de vous demander pourquoi ça ne marche pas, pensez à utiliser les scripts "
							+ "loadBalancerDeployment et éventuellement serverSlaveDeployment pour déployer linda version Multiserver"
							+ " et assurez vous d'avoir lancer l'outil avec l'adresse du serveur LoadBalancer" + ANSI_RESET);
					
					//Local variables
					boolean endLocal = false;
					@SuppressWarnings("resource")
					Scanner sc = new Scanner(System.in);
					boolean next = false;
					
					//Ask the choice about the warning
					while(!endLocal) {
						
						System.out.println("Si vous êtes confiant tapez yes sinon tapez no :");
						String choice2 = sc.nextLine();
						
						if(choice2.contentEquals("yes")) {
							endLocal = true;
							next = true;
						}else if (choice2.contentEquals("no")){
							endLocal = true;
							next = false;
						}else {
							System.out.println("Mot non reconnu");
						}
					}
					
					//If user agree, launch a shell (dev/test/deploy)  for multiserver
					if(next) {
						Linda lin = new linda.server.LindaClient(this.serverName,false);
						this.createShell(this.mode,lin,false).run();
					}
				
				//Back
				} else if (choice.contentEquals("quit")) {
					end = true;
				}else {
					System.out.println("choice non reconnu !");
				}
			}
		
		//Centralized version
		else {
			Linda lin = new linda.shm.CentralizedLinda();
			this.createShell(this.mode,lin,false).run();
		}
	}
	
}
