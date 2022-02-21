package linda.search.basic;

/** Class to use if you want to launch the search application */
public class Main {

    public static void main(String args[]) {
    	if (args.length < 2) {
            System.err.println("linda.search.basic.Main search1 ... searchN file.");
            return;
    	}
    	
    	SuperManager sm = new SuperManager();
    	sm.start(args);
    }
}
