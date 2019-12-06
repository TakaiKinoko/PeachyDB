package db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.*;

public class Database {
    /**
     * The Database should support multiple tables. With each new file being read in, a new Table is initialized.
     * Database class manages all tables invisibly.
     * */
    private Map<String, Table> tables;  // map table name with its data
    public Map<String, DynamicTable> dynamicTables;

    public Database(){
        /**
         * constructor
         * */
        tables = new HashMap<>();
        dynamicTables = new HashMap<>();
    }

    // =================================================================================================================
    //                                  CREATE table
    // =================================================================================================================
    public boolean newTable(String name, int col_num, int lines){
        /**
         * Create a new table with size known ahead of time
         * @param name: name to associate this new table with
         * @param col_num: length of the 2D array to be created -- number of columns
         * @param lines: width of the 2D array -- number of records
         * */
        try {
            tables.put(name, new Table(name, col_num, lines));
            return true;
        }catch(Exception e){
            System.out.println("Error creating new table!");
            return false;
        }
    }

    public boolean newEmptyTable(String name){
        /**
         * Create an empty table with size unknown
         * @param name: name to associate this new table with
         * sample use: Select.java line 71
         * */
        try {
            tables.put(name, new Table(name));
            return true;
        }catch(Exception e){
            System.out.println("Error creating new table!");
            return false;
        }
    }

    public boolean newDynamicTable(String name){
        /**
         * Create a table which adjusts its size dynamically
         * @param name: name to associate this new table with
         * */
        try{
            this.dynamicTables.put(name, new DynamicTable(name));
            return true;
        }catch(Exception e){
            System.out.println("Couldn't initialize new dynamic table.");
            return false;
        }
    }

    // =================================================================================================================
    //                                  GET table
    // =================================================================================================================
    public Table getTable(String name){
        /**
         * Get the table associated with the name.
         * @param name: name of the tables
         * @return: the table with the specified name
         * WILL return null if the named table doesn't exist
         * */
        return tables.get(name);
    }

    public DynamicTable getDynamicTable(String name){
        /**
         * Get the dynamic table associated with the name.
         * @param name: name of the tables
         * @return: the table with the specified name
         * WILL return null if the named table doesn't exist
         * */
        return dynamicTables.get(name);
    }

    // =================================================================================================================
    //                                  SCHEMA related functions
    // =================================================================================================================

    public boolean copySchema(String from_table, String to_table){
        /**
         * Copy the schema of from_table to to_table
         * @param from_table: whose schema is being copied
         * @param to_table: it's schema is going to be a DEEP COPY of the from_table
         * @return true if no error, false otherwise
         * */
        try {
            Map<String, Integer> copy = new HashMap<>();
            Map<String, Integer> origin = getTable(from_table).getSchema();
            for (String col : origin.keySet()) {
                copy.put(col, origin.get(col));
            }
            return getTable(to_table).updateSchema(copy);
        }catch(Exception e){
            System.out.println("Exception while creating schema for the new table.");
            return false;
        }
    }

    public boolean setSchema(String[] cols, String table) {
        /**
         * @param cols: array of column names read from the input data file
         * @param table: name of the table to operate on
         * @return: true if no error, false otherwise
         * */
        return getTable(table).setSchema(cols);
    }

    public Map<String, Integer> getSchema(String table){
        /***
         * @param table: name of the table to get schema for
         */
        return tables.get(table).getSchema();
    }


    private boolean sameSchema(String table1, String table2){
        /**
         * Checks to see if table1 and table2 has the same schema.
         * Used e.g. before concatenating two tables.
         * */
        Map<String, Integer> s1;
        Map<String, Integer> s2;
        try{
            s1 = getTable(table1).getSchema();
            s2 = getTable(table2).getSchema();
        }catch(Exception e){
            System.out.println("Couldn't locate the two tables or their schemas.");
            return false;
        }

        // prove that s1's schema is necessarily the subset of s2's schema
        for(String k: s1.keySet()){
            if(!s2.keySet().contains(k))
                return false;
        }

        // prove that s1 and s2 has the same schema size
        return s1.keySet().size() == s2.keySet().size();
    }

    // =================================================================================================================
    //                                  DATA related functions
    // =================================================================================================================

    public boolean insertData(String[] entry, String table){
        /**
         * @param entry: a new entry to the database
         *             containing heterogenous data which is either string or int
         * @return true if no error, false otherwise
         * */

        return tables.get(table).insertData(entry);
    }

    public String[][] getData(String table) {
        /***
         * @param table: name of the table to get data for
         */
        return tables.get(table).getData();
    }

    // =================================================================================================================
    //                         functions that make NEW TABLES out of ANOTHER TABLE
    // =================================================================================================================
    public boolean projectTable(String from_table, String to_table, String[] cols){
        /**
         * project the columns in @params cols from @from_table to @to_table
         *
         * schema of to_table should have already been set up when this function is called
         *
         * */
        Table toTable, fromTable;
        Map<String, Integer> toSchema, fromSchema;

        try {
            toTable = getTable(to_table);
            fromTable = getTable(from_table);
            toSchema = toTable.getSchema();
            fromSchema = fromTable.getSchema();
        }catch(Exception e){
            System.out.println("Tables don't exist.");
            return false;
        }

        int toCol;
        int fromCol;

        // mark to_table as derivative
        toTable.isDerivative();

        try{
            for(String col: cols){
                toCol = toSchema.get(col);
                fromCol = fromSchema.get(col);

                toTable.getData()[toCol] = fromTable.getData()[fromCol];
            }
            return true;
        }catch(Exception e){
            System.out.println("Error projecting columns to a new table.");
            return false;
        }
    }

    public boolean concatTables(String table1, String table2, String target){
        /**
         * concatenate table2 to the back of table1 into a new table named target.
         * @param table1: table being concatenated
         * @param table2: table being concatenated
         * @param target: resulting table
         * @return true if no error
         * table1 and table2 must have the same schema.
         * Note that concatenation doesn't preserve Hash/Btree indices.
         * The natural order index is currently being restored when data is printed out after concat is finished. see printData() within Table.java
         * */
        assert sameSchema(table1, table2);

        try {
            String[][] t1 = getTable(table1).getData();
            String[][] t2 = getTable(table2).getData();
            int col_num = t1.length;
            int t1_size = t1[0].length;
            int t2_size = t2[0].length;

            // create new table
            newTable(target, col_num, t1_size+t2_size);
            String[][] dest = getData(target);
            // schema -- copy table1's schema to target table
            copySchema(table1, target);

            // add all data of t2 to the back of t1,
            for(int i = 0; i < col_num; i++) {
                System.arraycopy(t1[i], 0, dest[i], 0, t1_size);
                System.arraycopy(t2[i], 0, dest[i], t1_size, t2_size);
            }

        }catch(Exception e){
            System.out.println("Error while concatenating tables.");
            return false;
        }

        return true;
    }

    public boolean copySubset(String from_table, String to_table, List<Integer> subset){
        /**
         * copy the subset of ENTRIES(not columns) from @param from_table to @param to_table, where the indices of the subset
         * is specified in the @param subset
         *
         * @param from_table: table to copy data from
         * @param to_table: target table
         * @param subset: indices of the items of from_table to be copied to to_table
         *
         * Condition: the to_table should have been constructed with Table(String name), so that the data field is still null
         *
         * Note that the operation doesn't preserve Hash/Btree indices.
         * The natural order index is currently being restored when data is printed out after concat is finished. see printData() within Table.java
         *
         * sample use: Select.java line 140
         * */
        // mark to_table as derivative
        String[][] to, from;
        int col_num;

        try {
            getTable(to_table).isDerivative();
            col_num = getSchema(from_table).size();
            to = new String[col_num][subset.size()];
            getTable(to_table).initializeDataMatrix(to);
            from = getTable(from_table).getData();
        }catch(Exception e){
            System.out.println("Tables don't exist.");
            return false;
        }

        int n = 0;

        try{ // iterate over items
            for(Integer i: subset){
                for(int m = 0; m < col_num; m++){
                    to[m][n] = from[m][i];
                }
                n++;
            }
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    // =================================================================================================================
    //                                  Print out statistics
    // =================================================================================================================
    public void showtables(){
        /**
         * Pretty Print all the names of the tables in this database and their sizes
         * */
        int maxName = "Table".length(), maxSize = "Size".length();
        int[] tmp;
        int padding = 2;
        for(String name: tables.keySet()){
            tmp = tables.get(name).prettyPrintNameLen();
            maxName = maxName < tmp[0]? tmp[0] : maxName;
            maxSize = maxSize < tmp[1]? tmp[1] : maxSize;
        }

        maxName += padding*2;
        maxSize += padding*2;

        String boundary = PrettyPrinter.getBorder(maxName, "+", "-")
                        + PrettyPrinter.getBorder(maxSize, "+", "-") + "+";
        String header = PrettyPrinter.getBox("Table", maxName, "|")
                        + PrettyPrinter.getBox("Size", maxSize, "|") + "|";

        // print out boundary
        System.out.println(boundary + "\n" + header + "\n" + boundary);

        for(String name: tables.keySet()) {
            int size = tables.get(name).getTableSize();
            System.out.println(PrettyPrinter.getBox(name, maxName, "|")
                    + PrettyPrinter.getBox(String.valueOf(size), maxSize, "|") + "|");
        }
        System.out.println(boundary + "\nTable counts: " + tables.size());

    }

    public void showSchema(){
        /**
         * Pretty Print all the names of the tables in this database and their schemas
         * */
        int maxName = "Table".length(), maxSize = "Schema".length();
        int[] tmp;
        int sch_len;
        int padding = 2;
        for(String name: tables.keySet()){
            tmp = tables.get(name).prettyPrintNameLen();
            sch_len = tables.get(name).prettyPrintSchemaLen();
            maxName = maxName < tmp[0]? tmp[0] : maxName;
            maxSize = maxSize < sch_len? sch_len : maxSize;
        }

        maxName += padding*2;
        maxSize += padding*2;

        String boundary = PrettyPrinter.getBorder(maxName, "+", "-")
                + PrettyPrinter.getBorder(maxSize, "+", "-") + "+";
        String header = PrettyPrinter.getBox("Table", maxName, "|")
                + PrettyPrinter.getBox("Schema", maxSize, "|") + "|";

        // print out boundary
        System.out.println(boundary + "\n" + header + "\n" + boundary);

        for(String name: tables.keySet()) {
            int size = tables.get(name).getTableSize();
            System.out.println(PrettyPrinter.getBox(name, maxName, "|")
                    + PrettyPrinter.getBox(tables.get(name).schemaToString(), maxSize, "|") + "|");
        }
        System.out.println(boundary);
    }
}
