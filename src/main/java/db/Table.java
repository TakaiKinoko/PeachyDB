package db;

import btree.BTree;
import index.BtreeKey;
import util.*;

import java.util.*;

public class Table {
    /**
     * the natural ordering of data works as the index into the data
     * each column is an ArrayList in itself
     * */
    private String[][] data;
    public HashMap<Integer, Integer> index;  // sorted ordering mapped to it's physical index
    public HashMap<String, HashMap<String, List<Integer>>> hash_indices;  // column name mapped to the hash index built on it
    public HashMap<String, BTree<BtreeKey, List<Integer>>> btree_indices;
    public String name;  // name is the string before ":=" in a command
    private Map<String, Integer> schema; // map column names to its index
    private boolean isDerivative = false;  // indicates that the table is built as a subset of another table
    private int entry_num = 0;  // track the position to insert entry

    public Table(String name, int col_num, int lines){
        /**
         * @param col_num: the number of columns in the data, which is the ROW-DIM of the 2D array
         * @param lines: the number of entries of the data, which is the COL-DIM of the 2D array
         * */
        data = new String[col_num][lines];
        this.name = name;
        this.schema = new HashMap<>();
        this.index = new HashMap<>();
    }

    public Table(String name){
        this.name = name;
        schema = new HashMap<>();
        this.index = new HashMap<>();
    }

    public void isDerivative(){
        isDerivative = true;
    }

    public int getTableSize(){
        /**
         * @return the number of entries in this table
         * */
        return data[0].length;
    }

    // =================================================================================================================
    //                                  INDEX related functions
    // =================================================================================================================

    public void updateIndex(HashMap<Integer, Integer> index){
        this.index = index;
    }

    // =================================================================================================================
    //                                  SCHEMA related functions
    // =================================================================================================================
    public boolean setSchema(String[] cols) {
        /**
         * @param cols: array of column names read from the input data file
         * @return: true if no error, false otherwise
         * */

        try{
            for(int i = 0; i < cols.length; i++){
                schema.put(cols[i], i);
            }
            schema = Utils.sortMapByValue(schema);
            return true;
        }catch(Exception e) {
            System.out.println("Error when setting schema!");
            return false;
        }

    }

    public boolean updateSchema(Map<String, Integer> schema){
        /**
         * Note that this function doesn't add new columns to the table as setSchema() does
         * */
        try {
            this.schema = Utils.sortMapByValue(schema);
            return true;
        }catch(Exception e){
            System.out.println("Exception while updating schema for the target table.");
            return false;
        }
    }

    public Map<String, Integer> getSchema(){
        return schema;
    }

    public String schemaToString() {
        /**
         * @return the string representation of the schema as a one-liner
         *
         * if the table is derivative, it's columns names are going to be of the format: <name>_<column name>
         * */
        schema = Utils.sortMapByValue(schema);  // otherwise doesn't come in order

        try {
            String res = "";
            for (String s : schema.keySet()) {
                if(!isDerivative)
                    res += s + " | ";   // just index
                else
                    res += name + "_" + s + " | ";  // just index
            }
            return res;
        }catch(Exception e){
            System.out.println("Error happened while reading the schema.");
            return "ERROR";
        }
    }

    // =================================================================================================================
    //                                  DATA related functions
    // =================================================================================================================
    public String[][] getData() {
        return data;
    }

    public boolean insertData(String[] entry){
        /**
         * @param entry: a new entry to the database
         *             containing heterogenous data which is either string or int
         * @return true if no error, false otherwise
         * */
        try{
            // iterate over data columns (rows of 2D array data)
            for(int i = 0; i < entry.length; i++)
                data[i][entry_num] = entry[i];
            index.put(entry_num, entry_num);
            entry_num++;
            return true;
        }catch(Exception e) {
            System.out.println("Exception while inserting data into the table.");
            return false;
        }
    }

    //sample use: JoinOld.java line 519
    public boolean initializeDataMatrix(String[][] data){
        try {
            this.data = data;
            return true;
        }catch(Exception e){
            System.out.println("Couldn't initialize data matrix.");
            return false;
        }
    }

    public void updateData(String[][] data){
        this.data = data;
    }

    public void printData() {
        // INITIALIZE INDEX TO ITS PHYSICAL ORDERING
        if(index == null || index.size() != getTableSize()){
            index = new HashMap<>();
            for(int i = 0; i < getTableSize(); i++)
                index.put(i, i);
        }

        PrettyPrinter.prettyPrintTableToStdOut(this, true); //TODO
    }

    public int[] prettyPrintNameLen(){
        /**
         * @return an integer pair of <table_name_len, data_size_len>
         * */
        int[] len = new int[2];
        len[0] = name.length();
        len[1] = String.valueOf(getTableSize()).length();
        return len;
    }

    public int prettyPrintSchemaLen(){
        return schemaToString().length();
    }

}
