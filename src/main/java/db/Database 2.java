package db;

//import index.IIndex;
import java.lang.reflect.Array;
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
    private Map<String, DynamicTable> dynamicTables;
    //private String[] names;         // keep track of the names of tables
    //private String currentTable;   // keep track of the current table being accessed.
    private Map<String, Variable> variables;  // store results of e.g. avg, count...

    public Database(){
        /**
         * constructor
         * */
        tables = new HashMap<>();
        variables = new HashMap<>();
        dynamicTables = new HashMap<>();
    }

    public boolean newVariable(Variable var){
        try {
            variables.put(var.name, var);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    // UPDATED
    public boolean newTable(String name, int col_num, int lines){
        try {
            tables.put(name, new Table(name, col_num, lines));
            return true;
        }catch(Exception e){
            System.out.println("Error creating new table!");
            return false;
        }
    }

    // ADDED -- sample use: Select.java line 71
    public boolean newEmptyTable(String name){
        try {
            tables.put(name, new Table(name));
            // TODO delete
            System.out.println();
            for(String t: tables.keySet())
                System.out.println("table: " + t);
            System.out.println();
            return true;
        }catch(Exception e){
            System.out.println("Error creating new table!");
            return false;
        }
    }

    public boolean newDynamicTable(String name){
        try{
            this.dynamicTables.put(name, new DynamicTable(name));
            return true;
        }catch(Exception e){
            System.out.println("Couldn't initialize new dynamic table.");
            return false;
        }
    }

    public DynamicTable getDynamicTable(String name){
        // TODO SAFE GUARD
        return dynamicTables.get(name);
    }
    // UPDATED
    public boolean insertData(String[] entry, String table){
        /**
         * @param entry: a new entry to the database
         *             containing heterogenous data which is either string or int
         * @return true if no error, false otherwise
         * */

        return tables.get(table).insertData(entry);
    }

    // UPDATED
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

        // copy index column from fromTable to toTable
        //toTable.getData().remove(0);
        //toTable.getData().add(0, fromTable.getData().get(0));

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

    // UPDATED
    public boolean concatTables(String table1, String table2, String target){
        /**
         * concatenate @param table2 to the back of @param table1 into a new table @param target.
         *
         * table1 and table2 must have the same schema.
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

            /*
            for (int i = 0; i < t1.length; i++) {
                ArrayList newCol = new ArrayList(t1.get(i));
                newCol.addAll(t2.get(i));
                getTable(target).addColumn(newCol);
            }
            // update index of the target table
            ArrayList index = new ArrayList();
            for (int i = 0; i < t1.get(0).size() + t2.get(0).size(); i++)
                index.add(i);
            getTable(target).updateColumn(0, index);
            */
        }catch(Exception e){
            System.out.println("Error while concatenating tables.");
            return false;
        }

        return true;
    }

    // UPDATED -- sample use: Select.java line 140
    public boolean copySubset(String from_table, String to_table, List<Integer> subset){
        /**
         * copy the subset of entries from @param from_table to @param to_table, where the indices of the subset
         * is specified in the @param subset
         *
         * @param from_table: table to copy data from
         * @param to_table: target table
         * @param subset: indices of the items of from_table to be copied to to_table
         *
         * Condition: the to_table should have been constructed with Table(String name), so that the data field is still null
         * */
        // mark to_table as derivative
        String[][] to, from;
        int col_num;

        try {
            getTable(to_table).isDerivative();
            col_num = getSchema(from_table).size();
            to = new String[col_num][subset.size()];
            getTable(to_table).initializeDataMatrix(to);
            System.out.println("Table size: " + col_num + " x " + subset.size());
            from = getTable(from_table).getData();
            // data columns of to_table is already set up when setup schema before
            //ArrayList<ArrayList> from = getTable(from_table).getData();
            //System.out.println("FROM TABLE: " + from_table + " column: " + from.size());
            //ArrayList<ArrayList> to = getTable(to_table).getData();
            //System.out.println("TO TABLE: " + to_table + " column: " + to.size());
            //int col_num = from.size(), i = 0;  // number of columns
        }catch(Exception e){
            System.out.println("Tables don't exist.");
            return false;
        }

        int n = 0;

        try{
        // iterate over items
            for(Integer i: subset){
                //StringBuilder entry = new StringBuilder();
                //entry.append(i + ": ");
                // iterate over columns
                //to.get(0).add(i); // index (reordered from 0)
                for(int m = 0; m < col_num; m++){
                    to[m][n] = from[m][i];
                    //to.get(k).add(from.get(k).get(j));
                    //entry.append(from.get(k).get(j));
                }
                n++;
                //System.out.println(entry.toString());
            }
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    private boolean sameSchema(String table1, String table2){
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

    public boolean copySchema(String from_table, String to_table){
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
        //System.out.println("In Database.java, setSchema");
        return getTable(table).setSchema(cols);
    }

    public Map<String, Integer> getSchema(String table){
        /***
         * @param table: name of the table to get schema for
         */
        return tables.get(table).getSchema();
    }

    public Table getTable(String name){
        return tables.get(name);
        /*
        for(String t: tables.keySet()){
            if(t.equals(name))
                return tables.get(t);
        }
        return null; */
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

    // UPDATED
    public String[][] getData(String table) {
        /***
         * @param table: name of the table to get data for
         */
        return tables.get(table).getData();
    }

    // UPDATED
    public void showtables(){
        //TODO prettyprint : keep track of the max len
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
        //String nameBar = new String(new char[maxName]).replace("\0", "-");
        //String sizeBar = new String(new char[maxSize]).replace("\0", "-");

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

    /*
    public List<IIndex> getIndices() {
        return indices;
    }*/

    // UPDATED
    public void showSchema(){
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
