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
        //try {
            String name = Parser.get_toTable(s);
            db.newEmptyTable(name);
            Table target = db.getTable(name);

            Map<GroupKey<String>, ArrayList> res = toGroups(s, target, "sum");

            int cols = target.getSchema().size(); // number of groupby cols
            String[][] data = new String[cols][res.size()];
            // compute sum for each group\\
            int cnt = 0;
            for(GroupKey k: res.keySet()){
                //int cnt = 0;
                //for(Object g: k.getKey())
                //    System.out.print(g + " ");
                ArrayList box = res.get(k);
                Double sum = Utils.calculateSumList(box);
                int i;
                for(i = 0; i < cols-1; i++)
                    data[i][cnt] = String.valueOf(k.getKey()[i]);
                data[i][cnt] = String.valueOf(sum);
                cnt++;
            }
            target.updateData(data);
            target.printData();
            //Variable var = new Variable(Parser.get_toTable(s), res);
            //db.newVariable(var);
        //} catch (Exception e) {
           // System.out.println("Exception ");
        //}
    }

    public void avggroup(String s){
        //try {
            String name = Parser.get_toTable(s);
            db.newEmptyTable(name);
            Table target = db.getTable(name);

            // toGroups will initialize the data matrix and schema
            Map<GroupKey<String>, ArrayList> res = toGroups(s, target,"avg");

            //target.setSchema(new String[]{"group", "avg"});
            int cols = target.getSchema().size(); // number of groupby cols plus avg col
            //System.out.println("Schema size: " + cols);
            String[][] data = new String[cols][res.size()];

            // compute sum for each group
            int cnt = 0;
            for(GroupKey k: res.keySet()){
                ArrayList box = res.get(k);
                Double avg = Utils.calculateAverageList(box);
                int i;
                for(i = 0; i < cols-1; i++)
                    data[i][cnt] = String.valueOf(k.getKey()[i]);
                data[i][cnt] = String.valueOf(avg);
                cnt++;
            }

            target.updateData(data);

            target.printData();

        //} catch (Exception e) {
       //     System.out.println("Exception ");
       // }
    }

    public void countgroup(String s){
        try {
            String name = Parser.get_toTable(s);
            db.newEmptyTable(name);
            Table target = db.getTable(name);

            Map<GroupKey<String>, ArrayList> res = toGroups(s, target, "count");

            int cols = target.getSchema().size(); // number of groupby cols
            String[][] data = new String[cols][res.size()];

            // compute sum for each group
            int cnt = 0;
            for(GroupKey k: res.keySet()){
                //for(Object g: k.getKey())
                //    System.out.print(g + " ");
                ArrayList box = res.get(k);
                int count = box.size();
                //String[] entry = new String[cols];
                int i;
                for(i = 0; i < cols-1; i++)
                    data[i][cnt] = String.valueOf(k.getKey()[i]);
                data[i][cnt] = String.valueOf(count);
                cnt++;
            }
            target.updateData(data);
            target.printData();

        } catch (Exception e) {
            System.out.println("Exception ");
        }
    }

    // UPDATED
    public TreeMap<GroupKey<String>, ArrayList> groupby(Table tb, int target, String[] groupby, Map_type t, boolean get_index){
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
        //try {
            for (ind = 0; ind < table_size; ind++) {
                //ind = (Integer)i;  // get row index
                /*System.out.println(groupby.length);
                for(int i : gb_cond)
                    System.out.print(data[i][ind]);
                System.out.println();
                */

                String[] comp = new String[groupby.length];
                cnt = 0;
                for (int col : gb_cond) {
                    //System.out.println(data[col][ind]);
                    comp[cnt] = data[col][ind];
                    cnt++;
                }
                GroupKey key = new GroupKey(comp);
                ArrayList box = res.getOrDefault(key, new ArrayList());
                if(get_index)
                    box.add(ind);
                else
                    box.add(data[target][ind]);

                res.put(key, box);
            }

            // TODO delete
            /*
            for(GroupKey k: res.keySet()){
                System.out.println(k.toString());
                System.out.println(res.get(k));
            }*/
       // }catch (Exception e){
       //     System.out.println("Couldn't read from target table.");
       //     return res;
       // }

        return res;
    }

    public Map<GroupKey<String>, ArrayList> toGroups(String s, Table tb, String op){
        /**
         * @param op is one of  {"avg", "sum", "count"}
         * */
        try{
            String toVar = Parser.get_toTable(s);
            String btwParens = Parser.get_conditions(s);
            ArrayList col;
            Map<GroupKey<String>, ArrayList> res;

            String[] inside = btwParens.split(",");
            Table fromTable = db.getTable(inside[0].trim());
            Integer target = fromTable.getSchema().get(inside[1].trim());
            //System.out.println("Target: " + fromTable.getSchema().get(target));
            String[] groupby = new String[inside.length - 2];

            //System.out.print("Group columns ");
            for(int i = 2; i < inside.length; i++) {
                groupby[i - 2] = inside[i].trim();
            }

            // get target, not index
            res = groupby(fromTable, target, groupby, Map_type.TREE, false);

            String[] schema = new String[groupby.length + 1];
            int i;
            for(i = 0; i < groupby.length; i++)
                schema[i] = "groupby_" + groupby[i];
            schema[i] = op + "_" + inside[1].trim();
            tb.setSchema(schema);


            return res;
        }catch (Exception e) {
            System.out.println("Exception when parsing grouping condition.");
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
