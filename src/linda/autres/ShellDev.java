package linda.autres;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class ShellDev implements ShellType {

	/** Taille d'u tuple en train d'être parser */
	static int tailleTupleCourant; 
	
	/** Linda à utiliser */
	Linda linda;
	
	/** Mots autorisés dans le shell */
	private static Set<String> motAutorise = new HashSet<> ();
	static {
		motAutorise.addAll(Arrays.asList("write","take","tryTake","read","tryRead","takeAll","readAll","eventRegister","debug","help","quit","clear"));
	}	
	
	public ShellDev(Linda linda) {
		this.linda = linda;
	}
	
	/* Vérifier si un mot appartient à la liste de mot autorisés */
	private boolean checkAppartenance(String word) {
		return motAutorise.contains(word);
	}
	

	
	/* Boucle principale du shell */
	public void run() {
		
		//Effacer la console
		System.out.print("\033[H\033[2J");
        System.out.flush();
		System.out.println("Bienvenu dans ce mini interpréteur de commande Linda !\nSi vous souhaitez savoir quelles commandes sont possibles tapez help :) \n");
		
		boolean fin = false;
		String input;
		System.out.print(">>>");
		Scanner sc = new Scanner(System.in);
		
		//Boucle principale du menu initial
		while(!fin && sc.hasNextLine()) {
			
			//Demander la ligne
			input = sc.nextLine();

			//Ligne vide
			if(input.isEmpty()) {
				continue;
			}
			
			//Séparer la chaine selon les espaces
			String[] token = input.split("\\s+");
			
			//Vérifier que le premier mot fait partie de la grammaire 
			if (this.checkAppartenance(token[0])) {
				
				//Traiter le mot quit
				if(token[0].contentEquals("quit") && (token.length != 1)) {
						System.out.print("\033[H\033[2J");
				        System.out.flush();
						fin = true;
				}
				
				//Parser et traiter la ligne
				else {
					traiterLigne(token);
				}
			}else {
				System.out.println("Command non reconnue " + token[0] + "\n");
			}
			if(!fin)
			System.out.print(">>>");
		}
	}
		
	/* Traiter une ligne en lancant les commandes basiques  du shell comme help, debug,...*/
	private void traiterLigne(String[] line) {
			
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
			default:
				parseCommande(line);
			}
	}
		
	/* Parser toutes les commandes linda */
	private void parseCommande(String[] line) {
		switch (line[0]) {
			case "write":
			case "read":
			case "take":
			case "tryTake":
			case "tryRead":
			case "takeAll":
			case "readAll":
				//traitement commun pour ces commandes lindas simples
				this.parseCommandeSimple(line);				
				break;
			case "eventRegister":
				this.parseEventRegister(line);
				break;
			default:
				linda.debug("shell");
			}
		}
	
	/* Parser et exécuter les commandes simples de linda (toutes sauf eventRegister)  */
	private void parseCommandeSimple(String[] line) {
			
		if(line.length <= 1) {
			System.out.println("Il manque des arguments....\n Le format est " + line[0] + "NBELEMENT ELEMENT1 ELEMENT2 ...");
			return;
		}
		String tuple = this.traiterUnTuple(line);
		if (tuple!=null) {
			Tuple t = Tuple.valueOf(tuple);
			switch (line[0]) {
				case "write":
					linda.write(t);
					break;
				case "read":
					linda.read(t);
					break;
				case "take":
					linda.take(t);					
					break;
				case "tryTake":
					linda.tryTake(t);					
					break;
				case "tryRead":
					linda.tryRead(t);					
					break;
				case "takeAll":
					linda.takeAll(t);					
					break;
				case "readAll":
					linda.readAll(t);					
					break;
			default:
					;
		}
		}
	}
		
	/* Parser et créer un event Register*/
	private void parseEventRegister(String[] line) {
					
		//Vérifier que les paramètres minimaux sont présents 
		if (line.length < 6) {
			System.out.println("Il manque des paramètres : eventRegister (recharge|norecharge) (read|take) (immediate|future) LISTE_ELEMENT");
			return;
		}
		
		//Vérifier paramètre 1 : callback qui se recharge ou callback à usage unique
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
		
		//Vérifier paramètre 2 : mode 
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
		
		//Vérifier paramètre 3 : le timing
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
		
		//Préparer les token correspondant au tuple pour être utilisée dans la méthode traiter un Tuple
		String[] tabTuple = Arrays.copyOfRange(line, 4, line.length);
		String[] argument = new String[line.length-3];
		argument[0] = "eventRegister";
		for(int i = 1;i<=(line.length-4);i++) {
			argument[i] = tabTuple[i-1];
		}

		//Transformer le reste de la ligne de commande en tuple
		String t = this.traiterUnTuple(argument);
		if (t!= null) {
			
			//Récupérer le tuple
			Tuple tuple = Tuple.valueOf(t);
			
			/* Définition de deux types de callbacks */
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
			
			//Type de callback à utiliser
			Callback callback = null;
			if(recharge)
				callback = new RechargeCallback();
			else
				callback = new NoRechargeCallback();

			//Enregistrer l'événèment
			this.linda.eventRegister(mode, timing, tuple, callback);
		}
	}
	

	/* Analyse la chaine en entrée pour renvoyer une string pouvant être castée en tuple */
	private String traiterUnTuple(String[] token) {
				
		if(!this.typeOfToken(token[1]).contentEquals("int")){
			System.out.println("Veuillez rentrer : " + token[0] + " NBELEMENT ELT1 ELT2 ELT3...");
			return null;
		}else {
			int tailleTuple = Integer.parseInt(token[1]);

			//Vérifier que l'utilisateur n'a pas oublié d'élément dans le tuple
			if(tailleTuple != (token.length -2)){
				System.out.println("Veuillez rentrer : " + token[0] + " NBELEMENT ELT1 ELT2 ELT3...");
				return null;
			}
			
			tailleTupleCourant = tailleTuple;
			
			//Concaténer les arguments pour former une chaine [ ELT1 ELT2 ... ] pouvant être caster en Tuple par Tuple.Valueof	
			String arg = "";
			boolean erreur = false;
			for(int i = 0;i<tailleTuple;i++) {
				
				//Analyse du type du token
				String type = this.typeOfToken(token[i+2]);
				
				//Type Inconnu
				if(type.contentEquals("autres")){
					System.out.println(token[i+2] + " est de type inconnu ");
					System.out.println("Types autorisés : int, boolean, string, Integer.class, String.class, Boolean.class");
					erreur = true;
					break;
				
				//Type int/bool/String
				}else if((!type.contentEquals("int")) && !(type.contentEquals("bool")) &&  !(type.contentEquals("str"))) {
					arg= arg + " "+  type;
				
				//Type "Motif" Integer.class String.class ou Boolean.class
				}else {
					arg= arg + " "+  token[i+2];
				}
			}
			
			if(erreur)
				return null;
			else {
				return "[" + arg + " ]";
				}
		}
	}

	/* Détecte si une chaine en entrée est un int, bool, une string, Integer.class, String.class, Boolean.class */
	private String typeOfToken(String token) {
		Pattern patternInt = Pattern.compile("([0-9]*)");
		Pattern patternStr = Pattern.compile("\"*\"");
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

}