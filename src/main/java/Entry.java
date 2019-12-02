import db.*;
import io.*;
import util.*;

import java.util.Scanner;

public class Entry {

    public static void main(String[] args){
        String output_path = "output/";
        String input_path = "input/";

        // create a new database
        Database db = new Database();

        // set up scanner, parser and IO for parsing commandline input
        IO io = new IO(db, input_path, output_path);
        QueryParser parser = new QueryParser(io);
        Scanner s = new Scanner(System.in);
        PrettyPrinter.printWelcome();

        while(s.hasNext()) {
            String q = s.nextLine();
            parser.decodeQuery(q);
        }

    }
}
