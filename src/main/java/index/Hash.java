package index;

import hash.*;
import index.hash.HashMap;
import pair.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hash<K,V> implements IIndex<K,V> {
    private ArrayList<ArrayList> data;   // pointer to DATA store  index <-> [values]
    private HashMap<K, V> db_chaining;  // store [key, index]

    public Hash(String path, ArrayList<ArrayList> DATA) throws Exception {
        db_chaining = new HashMap(HashMap.Type.CHAINING);
        data = DATA;
        readFile(path, tableWriter);
    }

    public void readFile(String path, BufferedWriter tableWriter) throws Exception {
        File file = new File(path);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        int index = 0;
        while ((st = br.readLine()) != null) {
            String[] strs = st.split("\\|"); // use escape character!
            try {
                int key = Integer.valueOf(strs[0]);
                int val = Integer.valueOf(strs[1]);
                data.add(new Pair(key, val));
                db_chaining.put(key, index);
                index++;
            } catch (Exception e) {  // handles abnormal lines e.g. the 1st (column names)
                continue;
            }
        }

        //printSize();
        /*
        StdOut.println("\n=========================================\n"
                + "   INITIAL SIZE OF DATABASE IS " + db_chaining.size() +
                "\n=========================================\n"); */
        tableWriter.write("\n=========================================\n"
                + "   INITIAL SIZE OF DATABASE IS " + db_chaining.size() +
                "\n=========================================\n");
        tableWriter.newLine();


        /*
        String[] entries = db_chaining.toString().split(",");
        for(String e: entries) {
            String[] strs = e.trim().split("=");
            int key = Integer.valueOf(strs[0]);
            int ind = Integer.valueOf(strs[1]);
            //StdOut.println(e.trim());
            //StdOut.println(key + ": " + data.get(ind));
        }*/
    }

    public void printSize(BufferedWriter tableWriter) throws IOException {
        //if(db_chaining.size() != db_probing.size())
        //    StdOut.println("Inconsistency!");
        //StdOut.println("Current number of items in HashMap DB is: " + db_chaining.size());
        tableWriter.write("Current number of items in HashMap DB is: " + db_chaining.size());
        tableWriter.newLine();
    }

    public boolean insert(K key, V value){
        int ind;
        if(db_chaining.contains(key)) {
            ind = db_chaining.get(key);
            //data.remove(ind);
            //delete(key);
            //data.add(ind, value); // add new value to the indexed position

            if(data.get(ind).deleted()) {
                data.get(ind).update(value);
                /*StdOut.println("KEY:" + key + " VALUE:" + value + " inserted.");
                tableWriter.write("\nKEY:" + key + " VALUE:" + value + " inserted.");
                tableWriter.newLine();*/
            }
            else{
                int old = data.get(ind).getValue();
                data.get(ind).update(value);
                /*StdOut.println("VALUE of KEY:" + key + " updated from " + old + " to " + value);
                tableWriter.write("\nVALUE of KEY:" + key + " updated from " + old + " to " + value);
                tableWriter.newLine();*/
            }
        }else {
            ind = data.size();
            db_chaining.put(key, ind);
            data.add(new Pair(key, value));
            /*StdOut.println("KEY:" + key + " VALUE:" + value + " inserted.");
            tableWriter.write("\nKEY:" + key + " VALUE:" + value + " inserted.");
            tableWriter.newLine();*/
        }
        // TODO fix
        return true;
    }

    public boolean delete(K key){
        //System.out.println("Delete key = " + key);
        Integer ind = db_chaining.remove(key);
        // case 1: key doesn't exist or has already been deleted
        if(ind == null || data.get(ind).deleted()) {
            /*StdOut.println("The KEY:" + key + " doesn't exist in the database.");
            tableWriter.write("\nThe KEY:" + key + " doesn't exist in the database.");
            tableWriter.newLine(); */
            return false;
        }
        // case 2: key present and successfully deleted
        if(data.get(ind).remove()){
            /*StdOut.println("KEY: " + key + " deleted!");
            tableWriter.write("\nKEY: " + key + " deleted!");
            tableWriter.newLine(); */
            return true;
        }else {
            /*StdOut.println("ERROR while deleting!");
            tableWriter.write("\nERROR while deleting!");
            tableWriter.newLine(); */
            return false;
        }
    }

    public V search(K key){
        V ind = db_chaining.get(key);
        if(ind == null || data.get(ind).deleted()) {
            /*StdOut.println("The KEY:" + key + " doesn't exist in the database.");
            tableWriter.write("\nThe KEY:" + key + " doesn't exist in the database.");
            tableWriter.newLine();*/
        }
        else{
            /*StdOut.println("VALUE of the given KEY:" + key + " is: " + data.get(ind).getValue());
            tableWriter.write("\nVALUE of the given KEY:" + key + " is: " + data.get(ind).getValue());
            tableWriter.newLine();*/
        }
        return ind;
    }

    public void readCommands(String path, BufferedWriter timingWriter, BufferedWriter tableWriter) throws Exception {
        File file = new File(path);
        Pattern ins_pattern = Pattern.compile("^insert");
        Pattern del_pattern = Pattern.compile("^delete");
        Pattern ser_pattern = Pattern.compile("^search");
        Matcher ins_m, del_m, ser_m, integer_m;
        long startTime, endTime, timeElapsed;

        BufferedReader br = new BufferedReader(new FileReader(file));

        long totalstartTime = System.nanoTime();
        String st;
        while ((st = br.readLine()) != null) {
            ins_m = ins_pattern.matcher(st);
            del_m = del_pattern.matcher(st);
            ser_m = ser_pattern.matcher(st);
            integer_m = Pattern.compile("\\d+").matcher(st);
            Integer[] numbers = new Integer[2];
            int i = 0;
            while(integer_m.find()) {
                numbers[i++] = (Integer.parseInt(integer_m.group()));
            }
            if (ins_m.find()) {
                startTime = System.nanoTime();
                insert(numbers[0], numbers[1], tableWriter);
                printSize(tableWriter);
                endTime = System.nanoTime();
                timeElapsed = endTime - startTime;
                //StdOut.println("hash insert costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.write("hash insert costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.newLine();
                /*
                String[] entries = db_chaining.toString().split(",");
                for(String e: entries)
                    StdOut.println(e.trim());*/
            } else if (del_m.find()) {
                startTime = System.nanoTime();
                if(delete(numbers[0], tableWriter)){
                    // if delete was successful, print out the updated hashmap
                    printSize(tableWriter);
                    endTime = System.nanoTime();
                    timeElapsed = endTime - startTime;
                    //StdOut.println("hash delete costs: " + timeElapsed + " nanoseconds.\n");
                    timingWriter.write("hash delete costs: " + timeElapsed + " nanoseconds.\n");
                    timingWriter.newLine();
                    /*
                    String[] entries = db_chaining.toString().split(",");
                    for(String e: entries)
                        StdOut.println(e.trim());*/
                }
            } else if (ser_m.find()) {
                startTime = System.nanoTime();
                search(numbers[0], tableWriter);
                endTime = System.nanoTime();
                timeElapsed = endTime - startTime;
                //StdOut.println("hash search costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.write("hash search costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.newLine();
            }
        }

        long totalendTime = System.nanoTime();
        tableWriter.write("\n=========================================\n"
                + "TOTAL TIME ELAPSED: " + (totalendTime - totalstartTime) + " nanoseconds");

        //printSize();
        //TODO: print all elements in the database
        //StdOut.println("=========================================\n"
        //+ "RESULTING DATABASE SIZE: " + db_chaining.size() +
        //        "\n=========================================");
        tableWriter.write("\n=========================================\n"
                + "RESULTING DATABASE SIZE: " + db_chaining.size() +
                        "\n=========================================\n");

        timingWriter.write("\n=========================================\n"
                + "TOTAL TIME ELAPSED: " + (totalendTime - totalstartTime) + " nanoseconds" +
                "\n=========================================\n");

        String[] entries = db_chaining.toString().split(",");
        for(String e: entries) {
            try{
                String[] strs = e.trim().split("=");
                int key = Integer.valueOf(strs[0]);
                int ind = Integer.valueOf(strs[1]);
                //StdOut.println(e.trim());
                //StdOut.println("Key: " + key + " Value: " + data.get(ind).getValue());
                tableWriter.write("Key: " + key + " Value: " + data.get(ind).getValue());
                tableWriter.newLine();
            }catch(Exception ex){
                continue;
            }
        }
    }

    public static void main(String[] args) throws Exception{
        String inPath = args[0];
        String commandsPath = args[1];
        BufferedWriter tableWriter = new BufferedWriter(new FileWriter("../../../output/hash_resultTable.txt"));
        BufferedWriter timingWriter = new BufferedWriter(new FileWriter("../../../output/hash_timing.txt"));
        Hash t = new Hash(inPath, tableWriter);

        //writer.write(str);

        t.readCommands(commandsPath, timingWriter, tableWriter);
        tableWriter.close();

        timingWriter.close();

        /*
        Scanner s = new Scanner(System.in);
        Pattern ins_pattern = Pattern.compile("^insert");
        Pattern del_pattern = Pattern.compile("^delete");
        Pattern ser_pattern = Pattern.compile("^search");
        Matcher ins_m, del_m, ser_m, integer_m;

        StdOut.println("\nPlease follow strictly to the following query format: \n" +
                "insert(k, v)\n" +
                "delete(k)\n" +
                "search(k)\n" +
                "Please note that only INTEGER key-value pairs are allowed.\n");

        while(s.hasNext()){
            String q = s.nextLine();
            ins_m = ins_pattern.matcher(q);
            del_m = del_pattern.matcher(q);
            ser_m = ser_pattern.matcher(q);
            integer_m = Pattern.compile("\\d+").matcher(q);
            Integer[] numbers = new Integer[2];
            int i = 0;
            while(integer_m.find()) {
                numbers[i++] = (Integer.parseInt(integer_m.group()));
            }
            if (ins_m.find()) {
                t.insert(numbers[0], numbers[1]);
                t.printSize();
                String[] entries = t.db_chaining.toString().split(",");

                //for(String e: entries)
                //    StdOut.println(e.trim());
            } else if (del_m.find()) {
                if(t.delete(numbers[0])){
                    // if delete was successful, print out the updated hashmap
                    t.printSize();
                    String[] entries = t.db_chaining.toString().split(",");

                    //for(String e: entries)
                    //    StdOut.println(e.trim());
                }
            } else if (ser_m.find()) {
                t.search(numbers[0]);
            }
        }*/

    }

}
