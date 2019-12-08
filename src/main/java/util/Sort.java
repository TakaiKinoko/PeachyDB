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
        /**
         * @param s: query string
         * */
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
        String[] key_cols = new String[key_num];

        for(int i = 0; i < key_num; i++) {
            key_cols[i] = inside[i+1].trim();
        }

        GroupAgg proxy = new GroupAgg(this.db);
        TreeMap<GroupKey<String>, ArrayList> groups = proxy.groupby(fromTable, 0, key_cols, GroupAgg.Map_type.TREE, true);
        HashMap<Integer, Integer> index = new HashMap<>();

        int cnt = 0;
        // get key set in ascending order
        for(GroupKey k: groups.navigableKeySet()){
            for(Object ind: groups.get(k)){
                // CREATED SORTED INDEX
                index.put(cnt, (Integer)ind);
                cnt++;
            }
        }

        target.updateIndex(index);
        target.printData();

    }

}
