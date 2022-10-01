package linda.autre;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

import linda.Linda;
import linda.test.outil.TestUnit;
import linda.test.metrics.Metrics;

/** Menu allowing unit tests and performance tests on the different linda's versions */
public class MenuTest implements MenuType {
	
	/** Linda */
	private Linda linda;
	
	/** Duration of the current test (in sec) */
	private int durationBeforeStop = 30;
	
	/** Using of cache ? */
	private boolean cache;	
	
	/** Constructor */
	public MenuTest(Linda linda,boolean cache) {
		this.linda = linda;
		this.cache = cache;
	}
	

	/**Main loop of the the menu */
	@Override
	public void run() {
		
		//Erase terminal
		System.out.print("\033[H\033[2J");
		System.out.flush();
		System.out.println("Bienvenu dans la section test !\nSi vous souhaitez savoir quelles commandes sont possibles tapez help :) \n");
				
		boolean end = false;						//To control the end of the loop
		String input;								//Input string of the user
		System.out.print(">>>");
		Scanner sc = new Scanner(System.in);
				
		//Main loop
		while(!end && sc.hasNextLine()) {
					
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
						+ "Pour la section des tests unit : unit \n"
						+ "Pour la section des tests de performances : perf \n");
				break;
				
			case "unit":
				lancerTestUnit();
				break;
				
			case "perf":
				lancerTestPerf();
				break;
				
			default:
				System.out.println("Choix non reconnu !");
				break;
			}
			if(!end)
			System.out.print(">>>");
		}
	}
	
	/** Menu about unit tests */
	public void lancerTestUnit() {
		
		//Calculate current path
		Path currentRelativePath = Paths.get("");
		Path s = currentRelativePath.toAbsolutePath();
		String fileSeparator = FileSystems.getDefault().getSeparator();
		
		//Retrieve path of the tests
		String pathTest = s.toString() + fileSeparator + "linda" + fileSeparator + "test" + fileSeparator + "outil" + fileSeparator;
		File dir = new File(pathTest);
	
		//Save name of tests in outil.test directory
		HashSet<String> ensembleTest = new HashSet<String>();
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".java") && !(file.getName().contentEquals("TestUnit.java")))
			ensembleTest.add(file.getName());
		}
		

		boolean end = false;
		String input;
		System.out.print("[unit] >>>");
		Scanner sc = new Scanner(System.in);
				
		//Main loop
		while(!end && sc.hasNextLine()) {
			
			//Display available tests
			System.out.println("Tests accessibles :");
			for (String test : ensembleTest) {
				System.out.println(test);
			}
			System.out.println("\n");
			
			//Ask for the test to launch
			input = sc.nextLine();
	
			switch (input) {
			
			case "quit":
				end = true;
				break;
			
			case "help":
				System.out.println("Rentrer simplement le nom du test à lancer (n'oubliez pas .java)");
				break;
			
			//Launch the test
			default:
				
				//Detected tests ?
				if(ensembleTest.contains(input)){
					
					//Define a duration maximum for the test
					this.durationBeforeStop = demanderDuree();
					
					//Instantiate the test
					Class<?> c = null;
					try {
						c = Class.forName("linda.test.outil." +input.substring(0, input.length()-5));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					try {
						Class<?>[] cArg = new Class[1]; 
						cArg[0] = Linda.class; 
						Object o = c.getDeclaredConstructor(cArg).newInstance(this.linda);
						
						//Launch the test in a thread to stop it after the maximum duration
						Thread th1 = new Thread() {
				            public void run() {
								((TestUnit) o).test();
				            }
					    };
					    th1.start();
					    
					    //Stop of the test
					    try {
					    Thread.sleep(this.durationBeforeStop *1000);
					    } catch (InterruptedException ex) { }
					    System.out.println("Test Interminable, stop....");
					    th1.interrupt();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}else {
					System.out.println("Nom de test inconnu, n'oubliez pas l'extension .java ");
				}
				break;
			}
			if(!end)
			System.out.print("[unit] >>>");
		}
	}

	/** Ask the duration of the test (non robust )
	 * @return int duration of the test*/
	public int demanderDuree() {
		boolean end = false;
		String input;
		System.out.print("Rentrer la durée (en sec) avant stop du test :");
		Scanner sc = new Scanner(System.in);
					
		//Demander la ligne
		return sc.nextInt();		
	}
	
	/** Menu to launch performance tests*/
	public void lancerTestPerf() {

		int nThreads = 10;					//Number of threads
		int nRequests = 10000;				//Total number of requests
		double takeF = .05;					//frequency of take
		double readAllF = 0;				//frequency of readAll
		
		String input;						//String input by user
		boolean end = false;
		
		System.out.print("[perf] >>>");
		Scanner sc = new Scanner(System.in);
		String errorMessage = "Pour lancer un test de performance lancez \" test [num_thread(int)] [requests per thread(int)] [take frequency(double)] [readAll frequency(double)]  \" ";

		//Main Loop
		while(!end && sc.hasNextLine()) {
			
			input = sc.nextLine();
	
			//Deal token of menu 
			switch (input) {
			
			case "quit":
				end = true;
				break;
			
			case "help":
				System.out.println(errorMessage);
				break;
			
			default:
				
				if(input.isEmpty()) {
					continue;
				}
				
				//Separate input according to spaces
				String[] token = input.split("\\s+");
				
					//Parse test
					if (token[0].contentEquals("test")) {
						try {
							
							//Parse number of threads
							nThreads = Integer.parseInt(token[1]);
							
							//Parse number of requests	
							nRequests = Integer.parseInt(token[2]);
							
							//Parse frequency of take
							takeF = Double.parseDouble(token[3]);
							if (takeF >= .5)
								System.out.println("Warning: high take frequencies can hinder result reliability");
							
							//Parse frequency of readAll
							readAllF = Double.parseDouble(token[4]);
							if (readAllF + 2 * takeF >= 1)
								System.out.println("Warning: refrain from using both high take and readAll frequencies for better results");
				
							//Launching the test
							Metrics.test(this.cache, nThreads, nRequests, takeF, readAllF, linda);
							
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println(errorMessage);
						}
					}
					
					
					else {
						System.out.println(errorMessage);
					}
			}
		}
	}
}
