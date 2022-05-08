package linda.autre;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

/** Menu which offer to select a version of Linda. */
public class MenuDev implements MenuType {

	/** Length of the parsed tuple */
	static int tailleTupleCourant; 
	
	/** Linda */
	Linda linda;
	
	/** Authorized words in the shell */
	private static Set<String> aWords = new HashSet<> ();
	static {
		aWords.addAll(Arrays.asList("write","take","tryTake","read","tryRead","takeAll","readAll","eventRegister","debug","help","quit","clear"));
	}	
	
	/** Constructor */
	public MenuDev(Linda linda) {
		this.linda = linda;
	}
	
	/** Check if a word is authorized
	 * @param word word to check
	 * @return boolean appartenance
	 */
	private boolean checkAppartenance(String word) {
		return aWords.contains(word);
	}
	

	
	/** Main Loop of our shell */
	@Override
	public void run() {
		
		//Erase the terminal 
		System.out.print("\033[H\033[2J");
        System.out.flush();
		System.out.println("Bienvenu dans ce mini interpréteur de commande Linda !\nSi vous souhaitez savoir quelles commandes sont possibles tapez help :) \n");
		
		boolean end = false;					//Do we have to end this shell sessions ? 
		String input;							//Input of the user
		System.out.print(">>>");				//Prompt characters
		
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);	
		
		//Main loop of the menu
		while(!end && sc.hasNextLine()) {
			
			input = sc.nextLine();

			if(input.isEmpty()) {
				continue;
			}
			
			//Separate the line according to spaces
			String[] token = input.split("\\s+");
			
			//Check if the word written by user is legal
			if (this.checkAppartenance(token[0])) {
				
				//Special word quit
				if(token[0].contentEquals("quit") && (token.length == 1)) {
						System.out.print("\033[H\033[2J");
				        System.out.flush();
						end = true;
				}
				
				//Parse the line
				else {
					parseLine(token);
				}
			}else {
				System.out.println("Command non reconnue " + token[0] + "\n");
			}
			if(!end)
			System.out.print(">>>");
		}
	}
		
	/** Deal with a line by executing the command entered (linda command or not)
	 * @param line line to execute
	 */
	private void parseLine(String[] line) {
			
		switch (line[0]) {
			
			case "quit":
				if(line.length != 1)
						System.out.println("Essayez juste quit");
			case "debug":
				if(line.length == 2)
				linda.debug(line[1]);
				else
					System.out.println("Essayez debug  TAG");
				break;
				
			case "help":
				if (line.length == 1) {
					System.out.println("\nCommandes acceptées :\n"
						+ "Pour effacer la console : clear \n"
						+ "Pour quitter : quit \n"
						+ "Pour debug TAG : debug \n"
						+ "write NB_ELEMENT LISTE_ELEMENT:	\n"
						+ "take NB_ELEMENT LISTE_ELEMENT:	\n"
						+ "read NB_ELEMENT LISTE_TUPLES:	\n"
						+ "tryTake NB_ELEMENT LISTE_TUPLES:	\n"
						+ "tryRead NB_ELEMENT LISTE_ELEMENT:	\n"
						+ "takeAll NB_ELEMENT LISTE_ELEMENT next NB_ELEMENT LISTE_ELEMENT ...\n"
						+ "eventRegister (recharge|norecharge) (read|take) (immediate|future) LISTE_ELEMENT (recharge permet de réenclencher le callback) \n");
				}else {
					System.out.println("Essayez juste help");
				} 
				break;
			
			case "clear":
				if (line.length == 1) {
					System.out.print("\033[H\033[2J");
				    System.out.flush();
				}else {
					System.out.println("Essayez juste clear");
				}
			    break;
			
			//Parse and excute linda's commands
			default:
				parseCommand(line);
			}
	}
		
	/** Parse all linda's command */
	private void parseCommand(String[] line) {
		
		switch (line[0]) {
			case "write":
			case "read":
			case "take":
			case "tryTake":
			case "tryRead":
			case "takeAll":
			case "readAll":
				// Commonn treatments for simple commands
				this.parseCommandSimple(line);				
				break;
			//eventRegister is special
			case "eventRegister":
				this.parseEventRegister(line);
				break;
			default:
				linda.debug("shell");
			}
		}
	
	/** Parse and execute simple linda's command  
	 * @param line line to treat 
	 * */
	private void parseCommandSimple(String[] line) {
			
		//All the commands have at least 3 parameters (command length of tuple and at least one element)
		if(line.length <= 2) {
			System.out.println("Il manque des arguments....\n Le format est " + line[0] + "NBELEMENT ELEMENT1 ELEMENT2 ...");
			return;
		}
		
		//Parse the tuple
		String tuple = this.parseATuple(line);
		
		if (tuple!=null) {
			
			//Convert the string to a real tuple
			Tuple t = Tuple.valueOf(tuple);
			Tuple op= null;
			switch (line[0]) {
			
				case "write":
					linda.write(t);
					System.out.println(t.toString() + " written " );
					break;
				
				case "read":
					op = linda.read(t);
					System.out.println(op.toString() + " read " );
					break;
				
				case "take":
					op = linda.take(t);
					System.out.println(op.toString() + " taken " );
					break;
				
				case "tryTake":
					op = linda.tryTake(t);					
					System.out.println(op.toString() + " taken " );
					break;
				
				case "tryRead":
					op = linda.tryRead(t);					
					System.out.println(op.toString() + " read " );
					break;
				
				case "takeAll":
					Collection<Tuple> collOp = linda.takeAll(t);	
					System.out.println(collOp.toString() + " taken " );
					break;
				
				case "readAll":
					Collection<Tuple> collOp2 = linda.readAll(t);	
					System.out.println(collOp2.toString() + " read " );
					break;
				default:
					;
				}
		}
	}
		
	/** Parse and execute an event Register 
	 * @param line containing the parameters */
	private void parseEventRegister(String[] line) {
					
		//Check if there are enough arguments
		if (line.length < 6) {
			System.out.println("Il manque des paramètres : eventRegister (recharge|norecharge) (read|take) (immediate|future) LISTE_ELEMENT");
			return;
		}
		
		//Parse parameter 1 : type of callback
		boolean recharge = false;
		switch (line[1]) {
		case "recharge":
			recharge = true;
			break;
		case "norecharge":
			recharge = false;
			break;
		default:
			System.out.println("eventRegister (recharge|norecharge) est obligatoire");
			return;
		}
		
		//Parse parameter 2 : mode 
		eventMode mode= eventMode.READ;
		switch (line[2]) {
		case "read":
			mode = eventMode.READ;
			break;
		case "take":
			mode = eventMode.TAKE;
			break;
		default:
			System.out.println("eventRegister ... (read|take) est obligatoire");
			return;
		}
		
		//Parse parameter 3 : timing
		eventTiming timing;
		switch (line[3]) {
		case "immediate":
			timing = eventTiming.IMMEDIATE;
			break;
		case "future":
			timing = eventTiming.FUTURE;
			break;
		default:
			System.out.println("eventRegister ... ... (immediate|future) est obligatoire");
			return;
		}
		
		//Prepare token corresponding to the tuple to reuse method parseATuple
		String[] tabTuple = Arrays.copyOfRange(line, 4, line.length);
		String[] argument = new String[line.length-3];
		argument[0] = "eventRegister";
		for(int i = 1;i<=(line.length-4);i++) {
			argument[i] = tabTuple[i-1];
		}

		//Parse the tuple in a string
		String t = this.parseATuple(argument);
		if (t!= null) {
			
			//Convert the string a real tuple
			Tuple tuple = Tuple.valueOf(t);
			
			/** First type of callback which register another time itself */
			class RechargeCallback implements Callback {

				public void call(Tuple t) {
		            System.out.println("CB got "+t);
		            linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, tuple, this);
		            try {
		                Thread.sleep(1000);
		            } catch (InterruptedException e) {
		            }
		            System.out.println("CB done with "+t);
		        }
		    }
			
			/** Second type of callback which does'nt record another time */
			class NoRechargeCallback implements Callback {

				public void call(Tuple t) {
		            System.out.println("CB got "+t);
		            try {
		                Thread.sleep(1000);
		            } catch (InterruptedException e) {
		            }
		            System.out.println("CB done with "+t);
		        }
		    }
			
			//Create the callback
			Callback callback = null;
			if(recharge)
				callback = new RechargeCallback();
			else
				callback = new NoRechargeCallback();

			//Register the event in linda
			this.linda.eventRegister(mode, timing, tuple, callback);
		}
	}
	

	/** Analyse the input to return a string which will be cast in a Tuple
	 * @param token possible tuple 
	 * @return String parsed tuple in a string */
	private String parseATuple(String[] token) {
		
		//Check if first element of the token is tuple's length
		if(! MenuSelectLinda.typeOfToken(token[1]).contentEquals("int")){
			System.out.println("Veuillez rentrer : " + token[0] + " NBELEMENT ELT1 ELT2 ELT3...");
			return null;
		
		}else {
			int tailleTuple = Integer.parseInt(token[1]);

			//Check if there are enough elements in the tuple
			if(tailleTuple != (token.length -2)){
				System.out.println("Veuillez rentrer : " + token[0] + " NBELEMENT ELT1 ELT2 ELT3...");
				return null;
			}
			
			tailleTupleCourant = tailleTuple;
			
			//Create the string [ ELT1 ELT2 ... ] , convertible in a Tuple
			String arg = "";						//String calculated
			boolean error = false;					//Problem wit the input ?
			for(int i = 0;i<tailleTuple;i++) {
				
				//Token' type
				String type = MenuSelectLinda.typeOfToken(token[i+2]);
				
				//Unknown
				if(type.contentEquals("autres")){
					System.out.println(token[i+2] + " est de type inconnu ");
					System.out.println("Types autorisés : int, boolean, string, Integer.class, String.class, Boolean.class");
					error = true;
					break;
				
				//Type int/bool/String
				}else if((!type.contentEquals("int")) && !(type.contentEquals("bool")) &&  !(type.contentEquals("str"))) {
					arg= arg + " "+  type;
				
				//Type Integer.class String.class ou Boolean.class
				}else {
					arg= arg + " "+  token[i+2];
				}
			}
			
			if(error)
				return null;
			else {
				return "[" + arg + " ]";
				}
		}
	}

}