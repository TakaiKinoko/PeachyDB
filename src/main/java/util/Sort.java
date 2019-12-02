package util;

import aggregation.GroupAgg;
import db.*;
import parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Sort {
    Database db;

    public Sort(Database db){
        this.db = db;
    }

    public void sort(String s){
        String toTable = Parser.get_toTable(s);
        // INITIALIZE TO_TABLE
        db.newEmptyTable(toTable);
        Table target = db.getTable(toTable);

        String btwParens = Parser.get_conditions(s);
        String[] inside = btwParens.split(",");

        // SYNTAX: <fromTable> <Col1> ... <Coln>
        Table fromTable = db.getTable(inside[0].trim());
        // SETUP TARGET SCHEMA AND DATA
        target.updateSchema(fromTable.getSchema());
        target.updateData(fromTable.getData());
        int key_num = inside.length - 1;  // number of keys
        //int[] key_cols = new int[key_num];  // index of sorting keys
        String[] key_cols = new String[key_num];

        for(int i = 0; i < key_num; i++) {
            //key_cols[i] = fromTable.getSchema().get(inside[i + 1].trim());
            key_cols[i] = inside[i+1].trim();
            //System.out.println(inside[i + 1].trim() + " " + key_cols[i]);
        }

        //getOrder(fromTable, key_cols);

        GroupAgg proxy = new GroupAgg(this.db);
        TreeMap<GroupKey<String>, ArrayList> groups = proxy.groupby(fromTable, 0, key_cols, GroupAgg.Map_type.TREE, true);
        HashMap<Integer, Integer> index = new HashMap<>();

        int cnt = 0;
        // get key set in ascending order
        for(GroupKey k: groups.navigableKeySet()){
            for(Object ind: groups.get(k)){
                //fromTable.printEntry((Integer)ind);
                // CREATED SORTED INDEX
                index.put(cnt, (Integer)ind);
                cnt++;
            }
        }

        target.updateIndex(index);
        target.printData();
        //System.out.println("\nNumber of entries: "+ cnt);

    }


/*
    private void getOrder(Table t, int[] key_cols){
        TreeMap<GroupKey, ArrayList<Integer>> index = new TreeMap<>();
        //int[] res = new int[t.getTableSize()];
        int keylen = key_cols.length;

        for(int i = 0; i < t.getTableSize(); i++){
            String[] tmp = new String[keylen];
            // make key
            for(int n = 0; n < key_cols.length; n++)
                tmp[n] = t.getData()[key_cols[n]][i];
            // insert <key, index> pair into the treemap
            GroupKey k = new GroupKey(tmp);
            ArrayList<Integer> vals = index.getOrDefault(k, new ArrayList<>());
            vals.add(i);
            index.put(k, vals);
        }

        System.out.println("size of map: " + index.size());
        for(GroupKey k: index.keySet()){
            //t.printEntry(index.get(k));
        }

    }
*/
}
