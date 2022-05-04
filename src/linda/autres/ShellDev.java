package linda.autres;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linda.Linda;
import linda.Tuple;

public class ShellDev implements ShellType {

	Linda linda;
	private static Set<String> motAutorise = new HashSet<> ();
	static {
		motAutorise.addAll(Arrays.asList("write","take","tryTake","read","tryRead","takeAll","eventRegister","debug","help","quit","clear"));
	}	
	
	public ShellDev(Linda linda) {

		this.linda = linda;
	}
	
	private boolean checkAppartenance(String word) {
		return motAutorise.contains(word);
	}
	
	public void run() {
		
		//Effacer la console
		System.out.print("\033[H\033[2J");
        System.out.flush();
		System.out.println("Bienvenu dans ce mini interpréteur de commande Linda !\nSi vous souhaitez savoir quelles commandes sont possibles tapez help :) \n");
		//Boucle principale du menu initial
		boolean fin = false;
		String input;
		System.out.print(">>>");
		Scanner sc = new Scanner(System.in);
		while(!fin && sc.hasNextLine()) {
			
			input = sc.nextLine();


			//Ligne vide
			if(input.isEmpty()) {
				continue;
			}
			
			//Séparer la chaine selon les espaces
			String[] token = input.split("\\s+");
			
			//Vérifier que le premier mot fait partie de la grammaire 
			if (this.checkAppartenance(token[0])) {
				
				//Quitter
				if(token[0].contentEquals("quit")) {
					System.out.print("\033[H\033[2J");
			        System.out.flush();
					fin = true;
				}
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
		
		private void traiterLigne(String[] line) {
			
			switch (line[0]) {
				case "debug":
					linda.debug(line[1]);
					break;
				case "help":
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
							+ "eventRegister (read|take) (immediate|future) LISTE_ELEMENT\n");
					break;
				case "clear":
					System.out.print("\033[H\033[2J");
			        System.out.flush();
			        break;
				default:
					parseCommande(line);
			}
		}
		
	private void parseCommande(String[] line) {
			switch (line[0]) {
			case "write":
			case "read":
			case "take":
			case "tryTake":
			case "tryRead":
				this.parseCommandeSimple(line);				
				break;
			case "takeAll":
				linda.debug("shell");
				break;
			case "eventRegister":
				linda.debug("shell");
				break;
			default:
				linda.debug("shell");
			}
		}
		private void parseCommandeSimple(String[] line) {
			
			if(line.length <= 1) {
				System.out.println("Il manque des arguments....\n");
				return;
			}
			Tuple t = null;
			t = Tuple.valueOf(this.traiterMethodeSimple(line));
			if (t!=null) {
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
					default:
						;
				}
			}
		}
		
		private String traiterMethodeSimple(String[] token) {
			
			if(!this.typeOfToken(token[1]).contentEquals("int")){
				System.out.println("Veuillez rentrer [commande] 3 ELT1 ELT2 ELT3, par exemple");
				return null;
			}else {
				int tailleTuple = Integer.parseInt(token[1]);
				//Concaténer les arguments
				String arg = "";
				boolean erreur = false;
				for(int i = 0;i<tailleTuple;i++) {
					//type du token
					String type = this.typeOfToken(token[i+2]);
					//Inconnu
					if(type.contentEquals("autres")){
						System.out.println(token[i+2] + " est de type inconnu ");
						System.out.println("Types autorisés : int, boolean, string, Integer.class, String.class, Boolean.class");
						erreur = true;
						break;
					//Classes
					}else if((!type.contentEquals("int")) && !(type.contentEquals("bool")) &&  !(type.contentEquals("str"))) {
						arg= arg + " "+  type;
					//TYpe de base
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
		//Détecte si c'est un int, bool ou une string
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