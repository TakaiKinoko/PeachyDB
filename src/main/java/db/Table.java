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
    //private ArrayList<ArrayList> data;   // pointer to DATA store  index <-> [values]
    private String[][] data;
    //TODO FIX INDEX
    HashMap<Integer, Integer> index;  // sorted ordering mapped to it's physical index
    public HashMap<String, HashMap<String, List<Integer>>> hash_indices;  // column name mapped to the hash index built on it
    public HashMap<String, BTree<BtreeKey, List<Integer>>> btree_indices;
    //private List<IIndex> indices;
    public String name;  // name is the string before ":=" in a command
    private Map<String, Integer> schema; // map column names to its index
    private boolean isDerivative = false;  // indicates that the table is built as a subset of another table
    private int entry_num = 0;  // track the position to insert entry

    // UPDATED
    public Table(String name, int col_num, int lines){
        /**
         * @param col_num: the number of columns in the data, which is the ROW-DIM of the 2D array
         * @param lines: the number of entries of the data, which is the COL-DIM of the 2D array
         * */
        data = new String[col_num][lines];
        //data = new ArrayList();
        //data.add(new ArrayList()); // the only column added when initializing table is the index column
        this.name = name;
        this.schema = new HashMap<>();
        this.index = new HashMap<>();
    }

    // ADDED
    public Table(String name){
        this.name = name;
        schema = new HashMap<>();
        this.index = new HashMap<>();
    }

    // ADDED -- sample use: JoinOld.java line 519
    public boolean initializeDataMatrix(String[][] data){
        try {
            this.data = data;
            return true;
        }catch(Exception e){
            System.out.println("Couldn't initialize data matrix.");
            return false;
        }
    }
    public void isDerivative(){
        isDerivative = true;
    }

    // UPDATED
    public int getTableSize(){
        /**
         * @return the number of entries in this table
         * */
        return data[0].length;
    }


    public void printEntry(Integer ind){
        for(int i = 0; i < schema.size(); i++){
            System.out.print(data[i][ind] + "\t");
        }
        System.out.println();
    }

    // UPDATED
    public void printData() {
        PrettyPrinter.prettyPrintTableToStdOut(this, true); //TODO
        /*
        if(index == null || index.size() != getTableSize()){
            index = new HashMap<>();
            for(int i = 0; i < getTableSize(); i++)
                index.put(i, i);
        }

        // TODO pretty printer
        System.out.println("\n\n========================\n" +
                           "  TABLE: " + name +
                         "\n========================");
        System.out.println(schemaToString());
        //System.out.println("FINE AFTER PRINTING SCHEMA");
        // TODO USE INDEX
        for(int n = 0; n < getTableSize(); n++){
            int ind = index.get(n);
            //System.out.println("Index: " + ind);
            StringBuilder entry = new StringBuilder();
            //entry.append(ind+": ");
            for(int m = 0; m < data.length; m++){
                entry.append(data[m][ind] + "\t");
            }
            System.out.println(entry.toString());
        }
        System.out.println("Number of entries inserted is: " + getTableSize() + "\n\n");  */
    }

    public void updateIndex(HashMap<Integer, Integer> index){
        this.index = index;
    }

    // UPDATED
    public boolean insertData(String[] entry){
        /**
         * @param entry: a new entry to the database
         *             containing heterogenous data which is either string or int
         * @return true if no error, false otherwise
         * */
        ///try{
            // iterate over data columns (rows of 2D array data)
            for(int i = 0; i < entry.length; i++)
                data[i][entry_num] = entry[i];
            index.put(entry_num, entry_num);
            entry_num++;
            // TODO manage indices
            return true;
        //}catch(Exception e) {
       //     System.out.println("Exception while inserting data into the table.");
        //    return false;
       // }
    }

    public void updateData(String[][] data){
        this.data = data;
    }


    // UPDATED
    public boolean setSchema(String[] cols) {
        /**
         * @param cols: array of column names read from the input data file
         * @return: true if no error, false otherwise
         * */
        //System.out.println("in Table.java setSchema!!!");

        try{
            for(int i = 0; i < cols.length; i++){
                //System.out.println(cols[i]);
                // index is zero-based, because the 0th data column is its index which was created in constructor
                schema.put(cols[i], i);
                // each column correspond to an arrayList
                //data.add(new ArrayList());
            }
            //System.out.println("SCHEMA:" + schemaToString());
            schema = Utils.sortMapByValue(schema);
            return true;
        }catch(Exception e) {
            System.out.println("Error when setting schema!");
            return false;
        }

    }

    // SAME
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

    // SAME
    public Map<String, Integer> getSchema(){
        return schema;
    }

    // UPDATED
    public String[][] getData() {
        return data;
    }

    // UPDATED
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
                    //res += schema.get(s) + ": " + s + " | ";   // index + colname
                    res += s + " | ";   // just index
                else
                    //res += schema.get(s) + ": " + name + "_" + s + " | "; // index + colname
                    res += name + "_" + s + " | ";  // just index
            }
            return res;
        }catch(Exception e){
            System.out.println("Error happened while reading the schema.");
            return "ERROR";
        }
    }

}
