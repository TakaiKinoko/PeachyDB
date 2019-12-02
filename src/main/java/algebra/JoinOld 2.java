package algebra;

import db.*;
import parser.*;
import util.Utils;
import aggregation.*;
import java.util.*;
import java.util.List;

import util.*;

public class JoinOld {
    private Database db;
    private Table t1;
    private Table t2;
    private Table target;
    private String conditions;
    private List<String> shared_col1;
    private List<String> shared_col2;
    private Map<String, Long> times;
    private long START;
    private int groupby = 0;
    private int count = 1;
    private enum Op{
        NOT_EQUAL, MORE_THAN, LESS_THAN, LESS_OR_EQUAL, MORE_OR_EQUAL;
    }

    // UPDATED
    public JoinOld(Database db, String s){
        START = System.currentTimeMillis();
        this.db = db;
        try {
            //String toTable = Parser.get_toTable(s);
            //db.newEmptyTable(toTable); // toTable: new table to store the query result in
            //this.target = db.getTable(toTable);

            String withinParens = Parser.get_conditions(s); // query string between the parens
            t1 = db.getTable(Utils.getNthArg(withinParens, 1));
            t2 = db.getTable(Utils.getNthArg(withinParens, 2));
            conditions = Utils.getNthArg(withinParens, 3);

            shared_col1 = new ArrayList<>();
            shared_col2 = new ArrayList<>();

            times = new HashMap<>();
            long end = System.currentTimeMillis();
            times.put("#" + count + "(constructor)", end-START);
            count++;

        }catch(Exception e){
            System.out.println("Exception while parsing join conditions.");
        }
    }

    public void join(String s) {
        long end = System.currentTimeMillis();
        times.put("#" + count + "(join)", end-START);
        count++;

        long start = System.currentTimeMillis();
       // target.isDerivative();  // mark this table as derivative

        end = System.currentTimeMillis();
        times.put("#" + count + "(join)", end-START);
        count++;

        if(Parser.is_arith_string(conditions)) {
            CartesianArray cartesian;
            //System.out.println(conditions);
            cartesian = evaluateArith(conditions);
            //TODO cartesian product TO TABLE
            HashSet<Cartesian.IND_PAIR> res = new HashSet<>();
            for(int n = 0; n < cartesian.i; n++)
                res.add(new Cartesian.IND_PAIR(cartesian.res[n][0], cartesian.res[n][1]));
            cartesianProductTable(res);
        }
        else {
            end = System.currentTimeMillis();
            times.put("#" + count + "(join)", end-START);
            count++;
            // two conditions
            String[] parts = Parser.bool_match(conditions);
            //System.out.println("BEFORE TRIMMING: " + parts[0]);
            String cond1 = Parser.trim_cond(parts[0]);
            String cond2 = Parser.trim_cond(parts[2]);
            //System.out.println(cond1 + "\n" + cond2);

            end = System.currentTimeMillis();
            times.put("#" + count + "(join)", end-START);
            count++;
            times.put("parse within join", end-start);

            CartesianArray cartesian1;
            CartesianArray cartesian2;
            cartesian1 = evaluateArith(cond1);
            end = System.currentTimeMillis();
            times.put("#" + count + "(join)", end-START);
            count++;

            cartesian2 = evaluateArith(cond2);
            end = System.currentTimeMillis();
            times.put("#" + count + "(join)", end-START);
            count++;

            switch(parts[1].trim()){
                case "and":
                    and(cartesian1, cartesian2);
                    break;
                case "or":
                    or(cartesian1, cartesian2);
                    break;
                default:
                    System.out.println("Unknown boolean operator. Check syntax.");
                    return;
            }
        }
        printTime();
        long finish = System.currentTimeMillis();
        System.out.printf("Total cost of time: %.4f sec\n", ((double)finish - (double)start)/1000);
    }

    private void printTime(){
        for(String s: times.keySet()){
            System.out.println(s + " costs " + times.get(s) + " milliseconds.");
        }
    }
    //====================================================================================================== AND
    private void and(CartesianArray ind_pair1, CartesianArray ind_pair2){
        // take intersection of the pair of integers

        HashSet<Cartesian.IND_PAIR> res = new HashSet<>();

        //====================================== EXPERIMENT
        HashSet<Cartesian.IND_PAIR> smallSet = new HashSet<>();
        //HashSet<Cartesian.IND_PAIR> largeSet = new HashSet<>();
        //HashSet<Cartesian.IND_PAIR> alt2 = new HashSet<>();
        CartesianArray large = ind_pair1.i < ind_pair2.i? ind_pair2 : ind_pair1;
        CartesianArray small = ind_pair1.i < ind_pair2.i? ind_pair1 : ind_pair2;

        // STORE THE SMALLER ONE IN A HASHSET
        long start = System.currentTimeMillis();
        for(int i = 0; i < small.i; i++)
            smallSet.add(new Cartesian.IND_PAIR(small.res[i][0], small.res[i][1]));
        System.out.println("smallSet size: "+ smallSet.size());

        for(int i = 0; i < large.i; i++){
            Cartesian.IND_PAIR tmp = new Cartesian.IND_PAIR(large.res[i][0], large.res[i][1]);
            if(smallSet.contains(tmp))
                res.add(tmp);
        }


        System.out.println("SIZE AFTER ADD "+ res.size());

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        times.put("alternative and()", timeElapsed);

        cartesianProductTable(res);
    }


    //====================================================================================================== OR
    private void or(CartesianArray ind_pair1, CartesianArray ind_pair2){
        // take union of the pair of integers
        long start = System.currentTimeMillis();

        HashSet<Cartesian.IND_PAIR> res = new HashSet<>();
        for(int n = 0; n < ind_pair1.i; n++)
            res.add(new Cartesian.IND_PAIR(ind_pair1.res[n][0], ind_pair1.res[n][1]));
        for(int n = 0; n < ind_pair2.i; n++)
            res.add(new Cartesian.IND_PAIR(ind_pair2.res[n][0], ind_pair2.res[n][1]));

        // cartesian product table
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        times.put("or()", timeElapsed);

        //cartesianProductTable(ind_pair1);
        cartesianProductTable(res);
    }

    //====================================================================================================== ARITH
    private CartesianArray evaluateArith(String cond){
        long start = System.currentTimeMillis();
        String[] ops = Parser.arith_match(cond);
        String operator = ops[1].trim();  // operator

        String[] leftOperand = Parser.decomposeOperand(ops[0]);
        String[] rightOperand = Parser.decomposeOperand(ops[2]);
        String t1 = leftOperand[0];  // table name of the left operand
        String t2 = rightOperand[0]; // table name of the right operand
        String col1 = leftOperand[1]; // column name of the left operand
        String col2 = rightOperand[1]; // column name of the right operand

        long end = System.currentTimeMillis();
        times.put("#" + count + "(evalArith)", end-START);
        count++;

        long gb_start = System.currentTimeMillis();
        GroupAgg proxy = new GroupAgg(this.db);
        //System.out.println("!!! " + db.getTable(t1).getTableSize());
        TreeMap<GroupKey<String>, ArrayList> groups1 = proxy.groupby(db.getTable(t1), 0, new String[]{col1}, GroupAgg.Map_type.TREE);

        System.out.println("TIME ELAPSED AFTER THE FIRST GROUPBY: " + (System.currentTimeMillis() - START));

        /*
        end = System.currentTimeMillis();
        times.put("#" + count + "(evalArith)", end-START);
        count++; */
        TreeMap<GroupKey<String>, ArrayList> groups2 = proxy.groupby(db.getTable(t2), 0, new String[]{col2}, GroupAgg.Map_type.TREE);
        System.out.println("TIME ELAPSED AFTER THE SECOND GROUPBY: " + (System.currentTimeMillis() - START));

        /*end = System.currentTimeMillis();
        times.put("#" + count + "(evalArith)", end-START);
        count++;
        long gb_end = System.currentTimeMillis();
        times.put("groupby()" + groupby, gb_end - gb_start);
        groupby++;*/

        //for(GroupKey<String> k: groups1.keySet()){
        //    System.out.println(groups1.get(k));
        //}
        //List<pair.Pair<String, TreeMap<GroupKey, ArrayList>>> res = new ArrayList<>();  // <table_name> <groups> pairs
        //HashMap<String,  Map<GroupKey<String>, ArrayList>> res = new HashMap<>();

        TreeMap<GroupKey<String>, ArrayList> LEFT;
        TreeMap<GroupKey<String>, ArrayList> RIGHT;

        // make sure the local naming of tables align with the global (fields)
        if(t1.equals(this.t1.name)) {
            //res.put(t1, groups1);  // put table name with its placeholder groups into the result map
            //res.put(t2, groups2);
            LEFT = groups1;
            RIGHT = groups2;
        }else{
            //res.put(t2, groups2);  // put table name with its placeholder groups into the result map
            //res.put(t1, groups1);
            LEFT = groups2;
            RIGHT = groups1;
        }
        /*
        end = System.currentTimeMillis();
        times.put("#" + count + "(evalArith)", end-START);
        count++; */
        //this.shared_col1.add(col1);
        //this.shared_col2.add(col2);
/*
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        times.put("evaluateArith()", timeElapsed);

        end = System.currentTimeMillis();
        times.put("#" + count + "(evalArith)", end-START);
        count++;*/
        CartesianArray CA = new CartesianArray();
        switch(ops[1]){
            case "=":
                CA.equal(LEFT, RIGHT);
                return CA;
            case "<":
                CA.less(LEFT, RIGHT, false);
                return CA;
            case "!=":
                CA.not_equal(LEFT, RIGHT);
                return CA;
            case ">":
                CA.more(LEFT, RIGHT, false);
                return CA;
            case "<=":
                CA.less(LEFT, RIGHT, true);
                return CA;
            case ">=":
                CA.more(LEFT, RIGHT, true);
                return CA;
            default:
                System.out.println("Wrong operator!");
                return null;
        }
    }


    private void cartesianProductTable(HashSet<Cartesian.IND_PAIR> cartesian) {
        /**
         * @param t1_shared: names of columns as primary key to the join operation
         *                   (columns to the left of a (=) condition)
         * @param t2_shared: names of columns as primary key to the join operation
         *                   (columns to the rigth of a (=) condition) t1_shared and
         *                   t2_shared correspond to each other.
         */

        long start = System.currentTimeMillis();
        // ================== get tables and their names ==================
        System.out.println("\n...Busy generating joined table...\n");

        String name1 = this.t1.name;
        String name2 = this.t2.name;
        Table t1 = this.t1;
        Table t2 = this.t2;
        /*
         * try { name1 = P.get(0).table1; name2 = P.get(0).table2; t1 =
         * db.getTable(name1); // make sure the tables align with what's store in the
         * List<TablePair> t2 = db.getTable(name2); }catch(IndexOutOfBoundsException e){
         * System.out.println("Product of this joint operation is empty."); return; }
         */

        // ================== set schema ==================
        Map<String, Integer> schema1 = t1.getSchema();
        Map<String, Integer> schema2 = t2.getSchema();
        Map<String, Integer> new1 = new HashMap<>(); // mapping table 1 column names to the target table col number
        Map<String, Integer> new2 = new HashMap<>(); // mapping table 2 column names to the target table col number
        List<String> newSchema = new ArrayList<>();
        // add shared columns to the new table at the front
        // newSchema.addAll(t1_shared);
        // add the rest of t1's columns to the new table
        int i = 0;
        for (String col : schema1.keySet()) {
            /*
             * if(!t1_shared.contains(col)) newSchema.add(name1 + col);
             */
            newSchema.add(name1 + "_" + col);
            new1.put(name1 + "_" + col, i++);
        }
        // add the rest of t2's columns to the new table
        for (String col : schema2.keySet()) {
            /*
             * if(!t2_shared.contains(col)) newSchema.add(name2 + col);
             */
            newSchema.add(name2 + "_" + col);
            new2.put(name2 + "_" + col, i++);
        }
        //target.setSchema(newSchema.toArray(new String[0]));
        // test
        //System.out.println(target.schemaToString());

        // ================== initialize target data matrix ==================
        //target.initializeDataMatrix(new String[schema1.size() + schema2.size()][cartesian.size()]);
        System.out.println("RESULTING ARRAY SIZE " + cartesian.size());

        // ================== Take cartesian product ==================
        // TODO NOT NECESSARY TO PUT SHARED COLUMN AT THE FRONT. THERE SHOULD ALSO BE A
        // COPY
        //try {
            String[][] data1 = t1.getData();
            String[][] data2 = t2.getData();
            System.out.println("t1: " + t1.name + "t1 size: " + data1[0].length);
            System.out.println("t2: " + t2.name + "t2 size: " + data2[0].length);

            //String[][] targetData = target.getData();
            // Map<String, Integer> targetSchema = target.getSchema();
            // List<Pair<Integer>> cartesian = getCartesianList(P);

            i = 0;
            Integer ind1, ind2;
            for (Cartesian.IND_PAIR p : cartesian) {
                ind1 = Integer.valueOf(p.vals[0]);
                ind2 = Integer.valueOf(p.vals[1]);
                //System.out.println("ind1 " + ind1);
                //System.out.println("ind2 " + ind2);

                // add new index
                // targetData.get(0).add(i++);

                Integer toCol;
                Integer fromCol;
                /*
                 * // add shared cols for(String s: t1_shared){ fromCol = schema1.get(s); toCol
                 * = targetSchema.get(s);
                 * targetData.get(toCol).add(data1.get(fromCol).get(ind1));
                 * System.out.println("Adding shared col"); }
                 */

                // add t1's col
                try {
                    for (String s : schema1.keySet()) {
                        // System.out.println(s);
                        fromCol = schema1.get(s);
                        // toCol = targetSchema.get(name1+s);
                        toCol = new1.get(name1 + "_" + s);
                        // if(toCol == null) continue; // shared key
                        //targetData[toCol][i] = (data1[fromCol][ind1]);
                        System.out.print(data1[fromCol][ind1] + "\t");
                    }

                    // add t2's col
                    for (String s : schema2.keySet()) {
                        fromCol = schema2.get(s);
                        // toCol = targetSchema.get(name2+s);
                        toCol = new2.get(name2 + "_" + s);
                        //targetData[toCol][i] = (data2[fromCol][ind2]);
                        System.out.print(data2[fromCol][ind2] + "\t");
                    }
                    System.out.println();
                } catch (OutOfMemoryError E) {
                    System.out.println("The table resulted is too big. We've run out of memory.");
                    return;
                }
                i++;
            }
            // System.out.println("Size of cartesian product: " + cartesian.size());
        //} catch (Exception e) {
          //  System.out.println("Failed to compute cartesian product.");
            //return;
        //}
/*
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        times.put("cartesianProductTable()", timeElapsed);*/

        //start = System.currentTimeMillis();
        //target.printData();
        //finish = System.currentTimeMillis();
        //timeElapsed = finish - start;
        //times.put("printData()", timeElapsed);*/
    }
}
