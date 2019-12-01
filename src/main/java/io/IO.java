package io;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import index.*;

import db.*;

public class IO{

    Database db;

    public IO(Database db) {
        /**
         * @param db: Database to associate this IO interface to.
         *
         * Each IO can only input/output for this one database.
         * */
        this.db = db;
    }

    public boolean inputfromfile(String s) {
        // parse file name and table name from s
        Pattern name_p = Pattern.compile("([a-zA-Z]+\\d*)(\\s*):=");
        Pattern path_p = Pattern.compile("\\((.*?)\\)");
        Matcher path_matcher = path_p.matcher(s);
        Matcher name_matcher = name_p.matcher(s);
        String name, path;
        try{
            if (path_matcher.find()) {
                path = path_matcher.group(1);
                //TODO: delete test println
                System.out.println(path);
            }else {
                System.out.println("Illegal format of file path");
                return false;
            }

            if(name_matcher.find()){
                name = name_matcher.group(1);
                //TODO: delete the test println
                System.out.println(name);
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
            // below line is migrated to Table.printData
            //System.out.println("Number of entries inserted is: " + db.getTable(name).getTableSize());
        }catch(Exception io_e){
            System.out.println("Something wrong happened while reading the file!");
            return false;
        }

        return true;
    }


    public void outputtofile() {
        return;
    }


    private static boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

}
