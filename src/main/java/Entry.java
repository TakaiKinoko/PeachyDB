import db.*;
import io.*;
import util.*;

import java.io.*;
import java.util.Scanner;

public class Entry {
    /**
     * This class is the entry to the database, with only a main function.
     * */

    public static void main(String[] args) throws IOException {
        // Default input path is /input, output path is /output. Default output file name prefix is "fh643_"
        String output_path = "output/fh643_";
        String input_path = "input/";

        // create a new database
        Database db = new Database();

        // set up scanner, parser and IO for parsing commandline input
        IO io = new IO(db, input_path, output_path);
        QueryParser parser = new QueryParser(io);
        Scanner s = new Scanner(System.in);
        PrettyPrinter.printWelcome();

        // option to read all queries from a file
        System.out.println("Do you want to run all commands from a file? (Y/y for yes, N/n for no)");
        while(s.hasNext()){
            String ans = s.nextLine();
            if(ans.charAt(0) == 'Y'|| ans.charAt(0) == 'y') {
                // read queries from a file
                boolean read = false;
                while (!read){
                    try {
                        System.out.println("File path:");
                        String path = s.nextLine();
                        File file = new File(path);
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String st;
                        while ((st = br.readLine()) != null) {
                            parser.decodeQuery(st);
                        }
                        System.out.println("Done executing.");
                        br.close();
                        read = true;
                    } catch (FileNotFoundException e) {
                        System.out.println("File doesn't exist");
                    }
                }
                break;
            }else if(ans.charAt(0) == 'N'|| ans.charAt(0) == 'n')
                break;
            else {
                System.out.println("Wrong commands.");
            }
        }

        /* if want to print out enterCommand message at each set interval, use the two lines below
        Timer timer = new Timer();
        timer.schedule(new enterCommand(), 0, 30000); */
        PrettyPrinter.enterCommandMsg();

        // enter interactive env where queries are accepted from StdIn one line at a time
        while(s.hasNext()) {
            String q = s.nextLine();
            parser.decodeQuery(q);
        }

    }
}
