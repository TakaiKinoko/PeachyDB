package index;

import btree.*;
import db.Database;
import db.Table;
import parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Btree {
    Database db;

    public Btree(Database db, String s) {
        this.db = db;

        // this query syntax doesn't have toTable
        String btwParens = Parser.get_conditions(s);
        String[] inside = btwParens.split(",");

        String col = inside[1].trim();

        Table target = db.getTable(inside[0].trim());
        buildBtree(target, col);
    }

    public void buildBtree(Table target, String col) {
        /**
         * Btree_indices
         *
         * <column_name> ==> Mapping between values and list of indices
         * */

        HashMap<String, BTree<BtreeKey, List<Integer>>> btree_indices;
        try {
            btree_indices = target.btree_indices;
        }catch(NullPointerException e){
            System.out.println("Table doesn't exist.");
            return;
        }


        if(btree_indices == null) {
            target.btree_indices = new HashMap();
            btree_indices = target.btree_indices;
        }
        else if(btree_indices.get(col) != null)
            return;

        BTree<BtreeKey, List<Integer>> index = new BTree<>();    // store [key, index]
        BTTestIteratorImpl<BtreeKey, List<Integer>> iter = new BTTestIteratorImpl<>();

        btree_indices.put(col, index);

        // get the corresponding column number of the column indexed upon
        int col_num = target.getSchema().get(col);
        String[][] data = target.getData();

        for(int i = 0; i < target.getTableSize(); i++){
            BtreeKey key = new BtreeKey(data[col_num][i]);
            List<Integer> l = index.search(key);
            if(l == null){
                l = new ArrayList<>();
            }
            l.add(i);
            index.insert(key, l);
        }

        // List all the key-val pairs in the btree -- true indicates to print to StdOut
        try {
            index.list(iter, false); // don't print
        }catch(IOException E){
            System.out.println("IO Exception.");
        }

        System.out.printf("\nSuccessfully built btree index for column: %s \n", col);
        if(target.btree_indices != null && target.btree_indices.get(col) != null){
            System.out.println("Column " + col + " has been btree indexed.");
        }
    }

}
