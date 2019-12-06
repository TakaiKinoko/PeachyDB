package io;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db.*;
import parser.Parser;
import util.PrettyPrinter;

public class IO{
    /**
     * Once a database is initialized, the IO class helps establish input/output path for the associated database.
     *
     * Two query commands are implemented here:
     * 1. inputfromfile
     * 2. outputtofile
     * */

    Database db;  // the (unique) database this IO unit is associated with
    private String input_path;
    private String output_path;

    public IO(Database db, String input_path, String output_path) {
        /**
         * @param db: Database to associate this IO interface to.
         * @param input_path: Default input path.
         * @param output_path: Default output path.
         * Each IO can only input/output for this one database.
         * */
        this.db = db;
        this.input_path = input_path;
        this.output_path = output_path;
    }

    public void outputtofile(String s) {
        /**
         * @param s: query string (unparsed)
         * */
        String btwParens = Parser.get_conditions(s);
        String[] inside = btwParens.split(",");
        String table = inside[0].trim();
        String out_file = inside[1].trim();

        BufferedWriter bw = null;
        try {
            File file = new File(output_path + out_file);

            /* This logic will make sure that the file
             * gets created if it is not present at the
             * specified location*/
            if (!file.exists())
                file.createNewFile();

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            Table tb = db.getTable(table);

            // MAKE SURE THE TABLE HAS INDEX
            if(tb.index == null || tb.index.size() != tb.getTableSize()){
                tb.index = new HashMap<>();
                for(int i = 0; i < tb.getTableSize(); i++)
                    tb.index.put(i, i);
            }

            PrettyPrinter.prettyPrintTableToFile(bw, tb, false, true);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }
    }

    public boolean inputfromfile(String s) {
        /**
         * @param s: query string (unparsed)
         * */
        // parse file name and table name from s
        Pattern name_p = Pattern.compile("([a-zA-Z]+\\d*)(\\s*):=");
        Pattern path_p = Pattern.compile("\\((.*?)\\)");
        Matcher path_matcher = path_p.matcher(s);
        Matcher name_matcher = name_p.matcher(s);
        String name, path;
        try{
            if (path_matcher.find()) {
                // concatenate the input path to the input file name
                path = input_path + path_matcher.group(1);
                //TODO: delete test println
                //System.out.println(path);
            }else {
                System.out.println("Illegal format of file path");
                return false;
            }

            if(name_matcher.find()){
                name = name_matcher.group(1);
                //TODO: delete the test println
                //System.out.println(name);
            }else {
                System.out.println("Illegal name to assign to a table");
                return false;
            }
        }catch(Exception e){
            System.out.println("Something wrong with the syntax.");
            return false;
        }

        // parse each line of file into String[] s

        File file = new File(path);
        BufferedReader br;
        String st;
        String schema;
        String[] subs, names;
        String[] entry;  // container for data in each line
        int lines = 0;
        int col_num = 0;
        // ================================================ FIRST READ: GET LINE COUNTS AND COL COUNT===============
        try {
            br = new BufferedReader(new FileReader(file));

            // READ COLUMN NUMBER (WILL BE THE ROW-DIM OF THE RESULTING TABLE)
            if((schema = br.readLine()) != null)
                col_num = schema.trim().split("\\|").length;
            // LINE WILL BE THE COL-DIM OF THE RESULTING TABLE
            while (br.readLine() != null) lines++;

            // TODO create new table with data set up
            db.newTable(name, col_num, lines);
            br.close();
        }catch(Exception E){
            System.out.println("File not found or IO exception.");
            return false;
        }

        //================================================ SECOND READ: READ DATA ==============================
        // call db.insertData
        System.out.println("\nreading from file: " + path + " into table: " + name + "...\n");
        try{
            // split each line into an array of string/int
            // add the entry to the database as an heterogenous arraylist
            br = new BufferedReader(new FileReader(file));

            // READ SCHEMA IN
            if((schema = br.readLine()) != null){
                // IMPORTANT!! regex needs to be escaped!!
                names = schema.trim().split("\\|");
                Boolean success = db.setSchema(names, name);
                if(!success){
                    System.out.println("Error occurred while setting schema.");
                }
            }

            while ((st = br.readLine()) != null) {
                subs = st.trim().split("\\|");
                //entry = new ArrayList<>();
                entry = new String[col_num];
                for(int i = 0; i< col_num; i++){
                    /*
                    if(isNumeric(sub))
                        entry.add(Integer.parseInt(sub));
                    else
                        entry.add(sub);*/
                    entry[i] = subs[i];
                }
                db.insertData(entry, name);
            }

            // TODO delete the print
            db.getTable(name).printData();

            // TODO print out database size after all has been read in
            // below line is migrated to StaticTable.printData
            //System.out.println("Number of entries inserted is: " + db.getTable(name).getTableSize());
        }catch(Exception io_e){
            System.out.println("Something wrong happened while reading the file!");
            return false;
        }

        return true;
    }

}
