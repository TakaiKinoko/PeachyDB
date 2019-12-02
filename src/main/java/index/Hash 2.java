package index;

import db.Database;
import db.Table;
import parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hash {
    Database db;

    public Hash(Database db, String s) {
        this.db = db;

        // TODO parse query and call buildHash
        // this query syntax doesn't have toTable
        String btwParens = Parser.get_conditions(s);
        String[] inside = btwParens.split(",");

        String col = inside[1].trim();

        Table target = db.getTable(inside[0].trim());
        buildHash(target, col);
    }


    public void buildHash(Table target, String col) {
        /**
         * hash_indices
         *
         * <column_name> ==> Mapping between values and list of indices
         * */
        HashMap<String, HashMap<String, List<Integer>>> hash_indices;
        try {
            hash_indices = target.hash_indices;
        }catch(NullPointerException e){
            System.out.println("Table doesn't exist.");
            return;
        }

        if(hash_indices == null) {
            target.hash_indices = new HashMap();
            hash_indices = target.hash_indices;
        }
        else if(hash_indices.get(col) != null)
            return;

        hash_indices.put(col, new HashMap<>());
        HashMap<String, List<Integer>> newHash = hash_indices.get(col);

        // get the corresponding column number of the column indexed upon
        int col_num = target.getSchema().get(col);
        String[][] data = target.getData();

        for(int i = 0; i < target.getTableSize(); i++){
            String key = data[col_num][i];
            List<Integer> l = newHash.getOrDefault(key, new ArrayList<>());
            l.add(i);
            newHash.put(key, l);
        }

        System.out.printf("\nSuccessfully built hash index for column: %s \n", col);
    }
}
