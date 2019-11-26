package db;

import java.util.ArrayList;
import java.util.Map;

public class Table {
    /**
     * the natural ordering of data works as the index into the data
     * each column is an ArrayList in itself
     * */
    private ArrayList<ArrayList> data;   // pointer to DATA store  index <-> [values]
    //TODO FIX INDEX
    //private List<IIndex> indices;
    public String name;  // name is the string before ":=" in a command
    private Map<String, Integer> schema; // map column names to its index

    public Table(String name){
        data = new ArrayList<>();
        this.name = name;
    }

    public boolean insertData(ArrayList entry){
        /**
         * @param entry: a new entry to the database
         *             containing heterogenous data which is either string or int
         * @return true if no error, false otherwise
         * */
        try{
            data.add(entry);
            // TODO manage indices
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public Map<String, Integer> getSchema(){
        return schema;
    }

    public ArrayList<ArrayList> getData() {
        return data;
    }

    public boolean setSchema(String[] cols) {
        /**
         * @param cols: array of column names read from the input data file
         * @return: true if no error, false otherwise
         * */
        try{
            for(int i = 0; i < cols.length; i++)
                schema.put(cols[i], i);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public int[] prettyPrintLen(){
        int[] len = new int[2];
        len[0] = name.length();
        len[1] = String.valueOf(data.size()).length();
        return len;
    }
    /*
    public List<IIndex> getIndices() {
        return indices;
    }*/
}
