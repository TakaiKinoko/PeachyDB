package db;

import index.IIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.*;


public class Database {
    /**
     * The Database should support multiple tables. With each new file being read in, a new Table is initialized.
     * Database class manages all tables invisibly.
     * */

    private Map<String, Table> tables;  // name table pair
    //private String[] names;         // keep track of the names of tables
    //private String currentTable;   // keep track of the current table being accessed.

    public Database(){
        /**
         * constructor
         * */
        tables = new HashMap<>();
    }

    public boolean newTable(String name){
        try {
            tables.put(name, new Table(name));
            return true;
        }catch(Exception e){
            System.out.println("Error creating new table!");
            return false;
        }
    }

    public boolean insertData(ArrayList entry, String table){
        /**
         * @param entry: a new entry to the database
         *             containing heterogenous data which is either string or int
         * @return true if no error, false otherwise
         * */

        return tables.get(table).insertData(entry);
    }

    public boolean setSchema(String[] cols, String table) {
        /**
         * @param cols: array of column names read from the input data file
         * @param table: name of the table to operate on
         * @return: true if no error, false otherwise
         * */
        return tables.get(table).setSchema(cols);
    }

    public Map<String, Integer> getSchema(String table){
        /***
         * @param table: name of the table to get schema for
         */
        return tables.get(table).getSchema();
    }

    public Table getTable(String name){
        return tables.get(name);
    }

    public boolean addTable(Table table){
        /**
         * Add table to the database (likely as a result of a query)
         * If the table passed in share the same name with an existing table, the old one will be overwritten.
         *
         * @param table: the table to be added to the database
         * @return: true if no error has occurred
         * */
        try {
            tables.put(table.name, table);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public ArrayList<ArrayList> getData(String table) {
        /***
         * @param table: name of the table to get data for
         */
        return tables.get(table).getData();
    }

    public void showtables(){
        //TODO prettyprint : keep track of the max len
        int maxName = "Table".length(), maxSize = "Size".length();
        int[] tmp;
        int padding = 2;
        for(String name: tables.keySet()){
            tmp = tables.get(name).prettyPrintLen();
            maxName = maxName < tmp[0]? tmp[0] : maxName;
            maxSize = maxSize < tmp[1]? tmp[1] : maxSize;
        }

        maxName += padding*2;
        maxSize += padding*2;
        //String nameBar = new String(new char[maxName]).replace("\0", "-");
        //String sizeBar = new String(new char[maxSize]).replace("\0", "-");

        String boundary = PrettyPrinter.getBorder(maxName, "+", "-")
                        + PrettyPrinter.getBorder(maxSize, "+", "-") + "+";
        String header = PrettyPrinter.getBox("Table", maxName, "|")
                        + PrettyPrinter.getBox("Size", maxSize, "|") + "|";

        // print out boundary
        System.out.println(boundary + "\n" + header + "\n" + boundary);

        for(String name: tables.keySet()) {
            int size = tables.get(name).getData().size();
            System.out.println(PrettyPrinter.getBox(name, maxName, "|")
                    + PrettyPrinter.getBox(String.valueOf(size), maxSize, "|") + "|");
        }
        System.out.println(boundary + "\nTable counts: " + tables.size());

    }

    /*
    public List<IIndex> getIndices() {
        return indices;
    }*/

}
