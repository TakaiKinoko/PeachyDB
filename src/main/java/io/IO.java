package io;

import java.io.*;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import index.*;
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

        // create new table
        db.newTable(name);

        // parse each line of file into String[] s
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            // call db.insertData
            String st;
            String schema;
            String[] subs, names;
            ArrayList entry;
            try{
                // split each line into an array of string/int
                // add the entry to the database as an heterogenous arraylist
                //TODO the first line is schema!!!!
                if((schema = br.readLine()) != null){
                    names = schema.trim().split("|");
                    db.setSchema(names, name);
                }

                while ((st = br.readLine()) != null) {
                    subs = st.trim().split("|");
                    entry = new ArrayList<>();
                    for(String sub: subs){
                        if(isNumeric(sub))
                            entry.add(Integer.parseInt(sub));
                        else
                            entry.add(sub);
                    }
                    db.insertData(entry, name);
                }

                // TODO print out database size after all has been read in
                System.out.println("Number of entries inserted is: " + db.getData(name).size());
            }catch(IOException io_e){
                System.out.println("Something wrong happened while reading the file!");
                return false;
            }
        }catch(FileNotFoundException e){
            System.out.println("File not found!");
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
