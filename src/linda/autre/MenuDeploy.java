package linda.autre;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

import linda.Linda;
import linda.test.outil.TestUnit;

/** Menu of the tool used to display useful tools to deploy applications on Linda => TO DO */
public class MenuDeploy implements MenuType {

	/** Linda */
	private Linda linda;
	
	/** Constructor.
	 * @param linda version of linda on which we deploy the application */
	public MenuDeploy(Linda linda) {
		this.linda = linda;
	}

	/** Main loop for the display and parsing of the menu. */
	@Override
	public void run() {
		//Erase terminal
		System.out.print("\033[H\033[2J");
		System.out.flush();
		System.out.println("Bienvenu dans la section deploy !\nSi vous souhaitez savoir quelles commandes sont possibles tapez help :) \n");
						
		boolean end = false;						//To control the end of the loop
		String input;								//Input string of the user
		System.out.print(">>>");
		Scanner sc = new Scanner(System.in);
		
		//Calculate current path
		Path currentRelativePath = Paths.get("");
		Path s = currentRelativePath.toAbsolutePath();
		String fileSeparator = FileSystems.getDefault().getSeparator();
				
		//Retrieve path of the application
		String pathTest = s.toString() + fileSeparator + "linda" + fileSeparator + "autre" + fileSeparator + "application" + fileSeparator;
		File dir = new File(pathTest);
			
		//Save name of applications in autres.application directory
		HashSet<String> ensembleApplication = new HashSet<String>();
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith("Application.java") )
			ensembleApplication.add(file.getName());
		}
				
		//Main loop
		while(!end && sc.hasNextLine()) {
				
			//Display available app
			System.out.println("Applications accessibles :");
			for (String test : ensembleApplication) {
				System.out.println(test);
			}
			System.out.println("\n");
			
			input = sc.nextLine();
			
			//Deal with commands of the menu
			switch (input) {
				case "quit":
					end = true;
					break;
						
				case "clear":
					System.out.print("\033[H\033[2J");
					System.out.flush();
					break;
						
				case "help":
					System.out.println("\nCommandes acceptées :\n"
								+ "Pour effacer la console : clear \n"
								+ "Pour quitter : quit \n"
								+ "Pour lancer une application rentrer le nom de la classe (avec .java) "
								+ " suivi des parametres de la ligne de commande \n");
					break;
						
				default:
					String[] token = input.split("\\s+");

					//Detected tests ?
					if(ensembleApplication.contains(token[0])){
						
						
						//Instantiate the application
						Class<?> c = null;
						try {
							c = Class.forName("linda.autre.application." +token[0].substring(0, token[0].length()-5));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						try {
							Class<?>[] cArg = new Class[1]; 
							cArg[0] = Linda.class; 
							Object o = c.getDeclaredConstructor(cArg).newInstance(this.linda);
							
							//Launch the test in a thread to stop it after the maximum duration
							if(o==null)
								System.out.println("o null");
							((DeploymentApp) o).start(Arrays.copyOfRange(token, 0, token.length));
					  
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
							
						}
					}else {
						System.out.println("Nom de d'application inconnu, n'oubliez pas l'extension .java ");
					}
				
			}
			if(!end)
				System.out.print(">>>");
		}
	}
}
