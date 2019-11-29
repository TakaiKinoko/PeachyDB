package aggregation;

import java.util.*;
import db.*;
import parser.*;
import util.*;

public class GroupAgg {
    private Database db;

    public GroupAgg(Database db){
        this.db = db;
    }

    public void sumgroup(String s){
        try {
            Map<GroupKey, ArrayList> res = toGroups(s);

            // compute sum for each group
            for(GroupKey k: res.keySet()){
                int cnt = 0;
                for(Object g: k.getKey())
                    System.out.print(g + " ");
                ArrayList box = res.get(k);
                Double sum = Utils.calculateSum(box);
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
            Map<GroupKey, ArrayList> res = toGroups(s);

            // compute sum for each group
            for(GroupKey k: res.keySet()){
                int cnt = 0;
                for(Object g: k.getKey())
                    System.out.print(g + " ");
                ArrayList box = res.get(k);
                Double sum = Utils.calculateAverage(box);
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
            Map<GroupKey, ArrayList> res = toGroups(s);

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

    public TreeMap<GroupKey, ArrayList> groupby(Table tb, int target, String[] groupby){
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
        TreeMap<GroupKey, ArrayList> res = new TreeMap<>(new GroupKeyComp());
        ArrayList<ArrayList> data = tb.getData();

        // get index column of the data
        ArrayList index = data.get(0);
        //System.out.println("!!!" + index.size() + "!!!");

        // get the numbers of the columns that serve as the groupby condition
        int[] gb_cond = new int[groupby.length];
        //System.out.println("~~~" + gb_cond.length + "~~~");
        for(int i = 0; i < groupby.length; i++){
            gb_cond[i] = tb.getSchema().get(groupby[i]);
            //System.out.println("..." + gb_cond[i] + "...");
        }

        ArrayList target_col = data.get(target);
        //System.out.println(target_col.size());

        int ind, cnt;
        for(Object i: index) {
            ind = (Integer)i;  // get row index
            Object[] comp = new Object[groupby.length];
            cnt = 0;
            for(int col: gb_cond){
                comp[cnt] = data.get(col).get(ind);
                cnt++;
            }
            GroupKey key = new GroupKey(comp);
            ArrayList box = res.getOrDefault(key, new ArrayList());
            box.add(target_col.get(ind));
            res.put(key, box);
        }


        /*
        System.out.println("Map size: " + res.size());
        for(GroupKey k: res.keySet()){
            for(Object g: k.key)
                System.out.print(g + " ");
            System.out.println();
            ArrayList box = res.get(k);
            for(Object o: box)
                System.out.print(o + " ");
            System.out.println();
        } */

        return res;
    }

    public Map<GroupKey, ArrayList> toGroups(String s){
        try{
            String toVar = Parser.get_toTable(s);
            String btwParens = Parser.get_conditions(s);
            ArrayList col;
            Map<GroupKey, ArrayList> res;

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
            res = groupby(fromTable, target, groupby);

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
