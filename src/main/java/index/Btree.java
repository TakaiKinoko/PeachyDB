package index;//import java.util.Map;
//import java.util.Random;
//import java.util.TreeMap;
import btree.*;
import index.btree.BTIteratorIF;
import index.btree.BTree;
import pair.*;

import java.util.ArrayList;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Description: This class contains all the test cases for BTree
 */
public class Btree<K,V> implements IIndex<K,V> {
    /**
     * Main Entry for the test
     *
     * @param args
     */
    //private List<Pair<Integer, Integer>> data;   // store  index <-> [key, value]
    private BTree<K, V> db;    // store [key, index]
    private BTTestIteratorImpl<K, V> iter;

    /**
     * Constructor
     */
    public Btree(String path, BufferedWriter tableWriter) throws Exception{
        db = new BTree<Integer, Integer>();
        data = new ArrayList<>();
        iter = new BTTestIteratorImpl<Integer, Integer>();
        readFile(path, tableWriter);
    }

    public void listItems(BufferedWriter tableWriter) throws IOException {
        iter = new BTTestIteratorImpl<Integer, Integer>();
        db.list(iter, data, tableWriter);
    }

    public void printSize(BufferedWriter tableWriter) throws  IOException{
        /*
        StdOut.println(
                "=========================================\n" +
                " Current number of items in B-tree DB is: " + Database.Database.size() + " \n" +
                "=========================================");*/
        //StdOut.println("Current number of items in B-tree DB is: " + Database.Database.size());
        tableWriter.write("Current number of items in B-tree DB is: " + db.size());
        tableWriter.newLine();
    }

    public void readFile(String path, BufferedWriter tableWriter) throws Exception {
        File file = new File(path);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        int index = 0;
        while ((st = br.readLine()) != null){
            String[] strs = st.split("\\|"); // use escape character!
            try {
                int key = Integer.valueOf(strs[0]);
                int val = Integer.valueOf(strs[1]);
                data.add(new Pair<Integer, Integer>(key, val));
                this.db.insert(key, index);
                index++;
            }catch(Exception e){  // handles abnormal lines e.g. the 1st (column names)
                continue;
            }
        }

        /*
        StdOut.println("=========================================\n"
                + "   INITIAL SIZE OF DATABASE IS " + Database.Database.size() +
                "\n=========================================\n");
        */
        tableWriter.write("\n=========================================\n"
                + "   INITIAL SIZE OF DATABASE IS " + db.size() +
                "\n=========================================\n");

        /*
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String[] strs = StdIn.readString().split("\\|"); // use escape character!
            try {
                int key = Integer.valueOf(strs[0]);
                int val = Integer.valueOf(strs[1]);
                this.Database.Database.insert(key, i);
                this.Database.Database.insert(key, val);
            }catch(Exception e){  // handles abnormal lines e.g. the 1st (column names)
                continue;
            }
        }
        */

        /*
        StdOut.println("\n######    The original B-tree    ######");
        // printout checksum and tree items
        checkTree(); */
    }

    public boolean insert(K key, V value){
        int ind;
        Integer pos = db.search(key);
        // key doesn't exist
        if(pos == null){
            ind = data.size();
            db.insert(key, ind);
            data.add(new Pair<Integer, Integer>(key, value));
            /*StdOut.println("KEY:" + key + " VALUE:" + value + " inserted.");
            tableWriter.write("\nKEY:" + key + " VALUE:" + value + " inserted.");
            tableWriter.newLine(); */
        }
        // insert when key has previously been inserted
        else if(data.get(pos).deleted()){
            data.get(pos).update(value);
            /*StdOut.println("KEY:" + key + " VALUE:" + value + " inserted.");
            tableWriter.write("\nKEY:" + key + " VALUE:" + value + " inserted.");
            tableWriter.newLine();*/
        }
        // upsert
        else{
            int oldval = data.get(pos).getValue();
            data.get(pos).update(value);
            /*StdOut.println("VALUE of KEY:" + key + " updated from " + oldval + " to " + value);
            tableWriter.write("\nVALUE of KEY:" + key + " updated from " + oldval + " to " + value);
            tableWriter.newLine(); */
        }
        //TODO: fix
        return true;
    }

    public boolean delete(K key){
        //System.out.println("Delete key = " + key);
        Integer ind = db.delete(key);
        if(ind == null || data.get(ind).deleted()) {
            /*StdOut.println("The KEY:" + key + " doesn't exist in the database.");
            tableWriter.write("\nThe KEY:" + key + " doesn't exist in the database.");
            tableWriter.newLine();*/
        }
        else if(data.get(ind).remove()){
            // successfully mark the entry in data array as deleted
            /*StdOut.println("KEY: " + key + " deleted!");
            tableWriter.write("\nKEY: " + key + " deleted!");
            tableWriter.newLine();*/
        }else {
            /*StdOut.println("ERROR while deleting!");
            tableWriter.write("\nERROR while deleting!");
            tableWriter.newLine();*/
        }
        return true;
    }

    public V search(K key){
        V ind = db.search(key);
        if(ind == null || data.get(ind).deleted()) {
            /*StdOut.println("The KEY:" + key + " doesn't exist in the database.");
            tableWriter.write("\nThe KEY:" + key + " doesn't exist in the database.");
            tableWriter.newLine(); */
        }
        else {
            /*StdOut.println("VALUE of the given KEY:" + key + " is: " + data.get(ind).getValue());
            tableWriter.write("\nVALUE of the given KEY:" + key + " is: " + data.get(ind).getValue());
            tableWriter.newLine(); */
        }
        return ind;
    }

    public void checkTree(BufferedWriter tableWriter) throws IOException {
        //printSize();
        listItems(tableWriter);
        tableWriter.newLine();
        //StdOut.println();
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
                //printSize();
                endTime = System.nanoTime();
                timeElapsed = endTime - startTime;
                //StdOut.println("btree insert costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.write("btree insert costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.newLine();
                /*
                String[] entries = db_chaining.toString().split(",");
                for(String e: entries)
                    StdOut.println(e.trim());*/
            } else if (del_m.find()) {
                startTime = System.nanoTime();
                delete(numbers[0], tableWriter);
                // if delete was successful, print out the updated hashmap
                printSize(tableWriter);
                endTime = System.nanoTime();
                timeElapsed = endTime - startTime;
                //StdOut.println("btree delete costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.write("btree delete costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.newLine();
                    /*
                    String[] entries = db_chaining.toString().split(",");
                    for(String e: entries)
                        StdOut.println(e.trim());*/

            } else if (ser_m.find()) {
                startTime = System.nanoTime();
                search(numbers[0], tableWriter);
                endTime = System.nanoTime();
                timeElapsed = endTime - startTime;
                //StdOut.println("btree search costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.write("btree search costs: " + timeElapsed + " nanoseconds.\n");
                timingWriter.newLine();
            }
        }

        //TODO: print all elements in the database
        /*
        StdOut.println("=========================================\n"
                + "RESULTING DATABASE SIZE: " + Database.Database.size() +
                "\n=========================================");
        */
        long totalendTime = System.nanoTime();
        tableWriter.write("\n=========================================\n"
                + "TOTAL TIME ELAPSED: " + (totalendTime - totalstartTime) + " nanoseconds");


        tableWriter.write("\n=========================================\n"
                + "RESULTING DATABASE SIZE: " + db.size() +
                "\n=========================================\n");

        checkTree(tableWriter);
        timingWriter.write("\n=========================================\n"
                + "TOTAL TIME ELAPSED: " + (totalendTime - totalstartTime) + " nanoseconds" +
                "\n=========================================\n");
    }

    public static void main(String[] args) throws Exception{
        String path = args[0];
        String commandsPath = args[1];
        BufferedWriter tableWriter = new BufferedWriter(new FileWriter("../../../output/btree_resultTable.txt"));
        BufferedWriter timingWriter = new BufferedWriter(new FileWriter("../../../output/btree_timing.txt"));
        Btree t = new Btree(path, tableWriter);

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
            List<Integer> numbers = new ArrayList<>();

            while(integer_m.find()) {
                numbers.add(Integer.parseInt(integer_m.group()));
            }
            if (ins_m.find()) {
                if(numbers.size() != 2)
                    StdOut.println("Wrong number of arguments.");
                else{
                    t.insert(numbers.get(0), numbers.get(1));
                    t.checkTree();
                }
            } else if (del_m.find()) {
                if(numbers.size() != 1)
                    StdOut.println("Wrong number of arguments.");
                else{
                    t.delete(numbers.get(0));
                    //t.checkTree();
                }
            } else if (ser_m.find()) {
                if(numbers.size() != 1)
                    StdOut.println("Wrong number of arguments.");
                else t.search(numbers.get(0));
            }
        }*/

    }

    /**
     * Inner class to implement BTree iterator
     */
    class BTTestIteratorImpl<K extends Comparable, V> implements BTIteratorIF<K, V> {
        private K mCurrentKey;
        private K mPreviousKey;
        private boolean mStatus;

        public BTTestIteratorImpl() {
            reset();
        }

        @Override
        public boolean item(K key, V value) {
            mCurrentKey = key;
            if ((mPreviousKey != null) && (mPreviousKey.compareTo(key) > 0)) {
                mStatus = false;
                return false;
            }
            mPreviousKey = key;
            return true;
        }

        public boolean getStatus() {
            return mStatus;
        }

        public K getCurrentKey() {
            return mCurrentKey;
        }

        public final void reset() {
            mPreviousKey = null;
            mStatus = true;
        }
    }
}