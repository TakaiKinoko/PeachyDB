package aggregation;

import java.util.*;
import db.*;
import parser.*;
import util.*;

public class GroupAgg {
    private Database db;

    public enum Map_type {
        TREE, HASH;
    }

    public GroupAgg(Database db){
        this.db = db;
    }

    public void sumgroup(String s){
        try {
            Map<GroupKey<String>, ArrayList> res = toGroups(s);

            // compute sum for each group
            for(GroupKey k: res.keySet()){
                int cnt = 0;
                for(Object g: k.getKey())
                    System.out.print(g + " ");
                ArrayList box = res.get(k);
                Double sum = Utils.calculateSumList(box);
                System.out.println("sum: " + sum);
            }
            Variable var = new Variable(Parser.get_toTable(s), res);
            db.newVariable(var);
        } catch (Exception e) {
            System.out.println("Exception ");
        }
    }

    public void avggroup(String s){
        try {
            Map<GroupKey<String>, ArrayList> res = toGroups(s);

            // compute sum for each group
            for(GroupKey k: res.keySet()){
                int cnt = 0;
                for(Object g: k.getKey())
                    System.out.print(g + " ");
                ArrayList box = res.get(k);
                Double sum = Utils.calculateAverageList(box);
                System.out.println("sum: " + sum);
            }

            Variable var = new Variable(Parser.get_toTable(s), res);
            db.newVariable(var);

        } catch (Exception e) {
            System.out.println("Exception ");
        }
    }

    public void countgroup(String s){
        try {
            Map<GroupKey<String>, ArrayList> res = toGroups(s);

            // compute sum for each group
            for(GroupKey k: res.keySet()){
                for(Object g: k.getKey())
                    System.out.print(g + " ");
                ArrayList box = res.get(k);
                int cnt = box.size();
                System.out.println("sum: " + cnt);
            }
            Variable var = new Variable(Parser.get_toTable(s), res);
            db.newVariable(var);

        } catch (Exception e) {
            System.out.println("Exception ");
        }
    }

    // UPDATED
    public TreeMap<GroupKey<String>, ArrayList> groupby(Table tb, int target, String[] groupby, Map_type t){
        /**
         * Group by one column at a time.
         * @param tb: table to operate on
         * @param target: number of the target column to perform aggregation on
         * @param groupby: names of the columns to perform "groupby" by
         * @return indices grouped by @param: cols
         *
         * Explanation:
         * Iterate over the data, drop each entry's index in the "box"
         * determined by the unique combination of groupby constraint tuple
         * */
        TreeMap<GroupKey<String>, ArrayList> res;

        //if(t == Map_type.TREE)
        res = new TreeMap<>(new GroupKeyComp());
        //else
            //res = new HashMap<>();


        String[][] data;
        try {
            data = tb.getData();
        }catch(NullPointerException Ne){
            System.out.println("table doesn't exist.");
            return null;
        }

        int table_size = tb.getTableSize();
        // get index column of the data
        //ArrayList index = data.get(0);

        // get the numbers of the columns that serve as the groupby condition
        int[] gb_cond = new int[groupby.length];
        //System.out.println("~~~" + gb_cond.length + "~~~");
        for(int i = 0; i < groupby.length; i++){
            gb_cond[i] = tb.getSchema().get(groupby[i]);
            //System.out.println("..." + gb_cond[i] + "...");
        }

        //String[] target_col = data[target];

        int ind, cnt;
        try {
            for (ind = 0; ind < table_size; ind++) {
                //ind = (Integer)i;  // get row index
                String[] comp = new String[groupby.length];
                cnt = 0;
                for (int col : gb_cond) {
                    comp[cnt] = data[col][ind];
                    cnt++;
                }
                GroupKey<String> key = new GroupKey(comp);
                ArrayList box = res.getOrDefault(key, new ArrayList());
                box.add(ind);
                res.put(key, box);
            }

            // TODO delete
            /*
            for(GroupKey k: res.keySet()){
                System.out.println(k.toString());
                System.out.println(res.get(k));
            }*/
        }catch (Exception e){
            System.out.println("Couldn't read from target table.");
            return res;
        }

        return res;
    }

    public Map<GroupKey<String>, ArrayList> toGroups(String s){
        try{
            String toVar = Parser.get_toTable(s);
            String btwParens = Parser.get_conditions(s);
            ArrayList col;
            Map<GroupKey<String>, ArrayList> res;

            String[] inside = btwParens.split(",");
            Table fromTable = db.getTable(inside[0].trim());
            Integer target = fromTable.getSchema().get(inside[1].trim());
            System.out.println("Target: " + target);
            String[] groupby = new String[inside.length - 2];

            System.out.print("Group columns ");
            for(int i = 2; i < inside.length; i++) {
                groupby[i - 2] = inside[i].trim();
                System.out.print(" " + groupby[i-2]);
            }
            System.out.println();
            res = groupby(fromTable, target, groupby, Map_type.TREE);

            return res;
        }catch (Exception e) {
            System.out.println("Exception.");
            return null;
        }
    }

    private class GroupKeyComp implements Comparator<GroupKey>{

        @Override
        public int compare(GroupKey e1, GroupKey e2) {
            return e1.compareTo(e2);
        }
    }
}
