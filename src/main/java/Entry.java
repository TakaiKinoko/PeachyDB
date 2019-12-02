import db.*;
import io.*;
import util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Entry {

    public static void main(String[] args) throws IOException {
        String output_path = "output/";
        String input_path = "input/";

        // create a new database
        Database db = new Database();

        // set up scanner, parser and IO for parsing commandline input
        IO io = new IO(db, input_path, output_path);
        QueryParser parser = new QueryParser(io);
        Scanner s = new Scanner(System.in);
        PrettyPrinter.printWelcome();

        System.out.println("Do you want to run all the course commands from the course handout? (Y/y for yes, N/n for no)");
        while(s.hasNext()){
            String ans = s.nextLine();
            if(ans.charAt(0) == 'Y'|| ans.charAt(0) == 'y'){
                File file = new File("input/handout");
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null) {
                    parser.decodeQuery(st);
                }
                System.out.println("Done executing.");
                br.close();
                break;
            }else if(ans.charAt(0) == 'N'|| ans.charAt(0) == 'n')
                break;
            else {
                System.out.println("Wrong commands.");
                continue;
            }
        }

        Timer timer = new Timer();
        timer.schedule(new enterCommand(), 0, 30000);

        while(s.hasNext()) {
            String q = s.nextLine();
            parser.decodeQuery(q);
        }

    }

    static class enterCommand extends TimerTask {
        public void run() {
            PrettyPrinter.enterCommandMsg();
        }
    }
}
