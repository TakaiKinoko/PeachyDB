package algebra;

import db.*;
import pair.Pair;
import parser.*;
import util.Utils;
import aggregation.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import util.*;

public class Join {
    private Database db;
    private Table t1;
    private Table t2;
    private Table target;
    private String conditions;
    private List<String> shared_col1;
    private List<String> shared_col2;

    private enum Op{
        NOT_EQUAL, MORE_THAN, LESS_THAN, LESS_OR_EQUAL, MORE_OR_EQUAL;
    }

    public Join(Database db, String s){
        this.db = db;
        try {
            String toTable = Parser.get_toTable(s);
            db.newTable(toTable); // toTable: new table to store the query result in
            this.target = db.getTable(toTable);

            String withinParens = Parser.get_conditions(s); // query string between the parens
            t1 = db.getTable(Utils.getNthArg(withinParens, 1));
            t2 = db.getTable(Utils.getNthArg(withinParens, 2));
            conditions = Utils.getNthArg(withinParens, 3);

            //System.out.println(target.name);
            //System.out.println(t1.name);
            //System.out.println(t2.name);
            //System.out.println(conditions);

            shared_col1 = new ArrayList<>();
            shared_col2 = new ArrayList<>();
        }catch(Exception e){
            System.out.println("Exception while parsing join conditions.");
        }
    }

    public void join(String s){
        evaluateJoin();

    }

    private void evaluateJoin() {
        target.isDerivative();  // mark this table as derivative

        //Map<String, Integer> schema = table.getSchema();

        if(Parser.is_arith_string(conditions)) {
            List<VPair<Integer>> cartesian;
            //System.out.println(conditions);
            cartesian = evaluateArith(conditions);
            //TODO cartesian product TO TABLE
            cartesianProductTable(cartesian);
        }
        else {
            // two conditions
            String[] parts = Parser.bool_match(conditions);
            //System.out.println("BEFORE TRIMMING: " + parts[0]);
            String cond1 = Parser.trim_cond(parts[0]);
            String cond2 = Parser.trim_cond(parts[2]);
            //System.out.println(cond1 + "\n" + cond2);

            List<VPair<Integer>> cartesian1;
            List<VPair<Integer>> cartesian2;
            cartesian1 = evaluateArith(cond1);
            cartesian2 = evaluateArith(cond2);

            switch(parts[1].trim()){
                case "and":
                    and(cartesian1, cartesian2);
                    return;
                case "or":
                    or(cartesian1, cartesian2);
                    return;
                default:
                    System.out.println("Unknown boolean operator. Check syntax.");
                    return;
            }
        }
    }

    //====================================================================================================== AND
    private void and(List<VPair<Integer>> ind_pair1, List<VPair<Integer>> ind_pair2){
        // take intersection of the pair of integers
        List<VPair<Integer>> res = new ArrayList<>();

        for(VPair<Integer> i: ind_pair1){
            if(ind_pair2.contains(i)) {
                res.add(i);
            }
        }
        Collections.sort(res);

        // cartesian product table
        cartesianProductTable(res);
    }
    //====================================================================================================== OR
    private void or(List<VPair<Integer>> ind_pair1, List<VPair<Integer>> ind_pair2){
        // take union of the pair of integers
        // TODO intermediary function that transfer List<TablePair> to List<Pair<Integer>>
        ind_pair1.removeAll(ind_pair2);
        // merge two lists
        ind_pair1.addAll(ind_pair2);
        // sort
        Collections.sort(ind_pair1);

        // cartesian product table
        // TODO change signature of cartesianProductTable to List<Pair<Integer>>
        cartesianProductTable(ind_pair1);
    }

    //====================================================================================================== ARITH
    private List<VPair<Integer>> evaluateArith(String cond){
        String[] ops = Parser.arith_match(cond);
        String operator = ops[1].trim();  // operator
        // test
        //System.out.println(ops[0] + "|" + ops[1] + "|" + ops[2]);

        String[] leftOperand = Parser.decomposeOperand(ops[0]);
        String[] rightOperand = Parser.decomposeOperand(ops[2]);
        String t1 = leftOperand[0];  // table name of the left operand
        String t2 = rightOperand[0]; // table name of the right operand
        String col1 = leftOperand[1]; // column name of the left operand
        String col2 = rightOperand[1]; // column name of the right operand

        // test
        //System.out.println(leftOperand[0] + "~~" + leftOperand[1]);
        //System.out.println(rightOperand[0] + "~~" + rightOperand[1]);

        GroupAgg proxy = new GroupAgg(this.db);
        TreeMap<GroupKey, ArrayList> groups1 = proxy.groupby(db.getTable(t1), 0, new String[]{col1});
        /*
        for(GroupKey k: groups1.keySet()){
            System.out.println(k.getKey()[0] + " | " + groups1.get(k));
        }
        */

        TreeMap<GroupKey, ArrayList> groups2 = proxy.groupby(db.getTable(t2), 0, new String[]{col2});
        /*
        for(GroupKey k: groups2.keySet()){
            System.out.println(k.getKey()[0] + " | " + groups2.get(k));
        }
        */
        List<pair.Pair<String, TreeMap<GroupKey, ArrayList>>> res = new ArrayList<>();  // <table_name> <groups> pairs

        // make sure the local naming of tables align with the global (fields)
        if(t1.equals(this.t1.name)) {
            res.add(new pair.Pair(t1, groups1));  // put table name with its placeholder groups into the result map
            res.add(new pair.Pair(t2, groups2));
        }else{
            res.add(new pair.Pair(t2, groups2));  // put table name with its placeholder groups into the result map
            res.add(new pair.Pair(t1, groups1));
        }

        this.shared_col1.add(col1);
        this.shared_col2.add(col2);

        try {
            switch (ops[1]) {
                case "=":
                    return equal(res, t1, t2);
                case "<":
                    return notEqual(res, t1, t2, Op.LESS_THAN);
                case "!=":
                    return notEqual(res, t1, t2, Op.NOT_EQUAL);
                case ">":
                    return notEqual(res, t1, t2, Op.MORE_THAN);
                case "<=":
                    return notEqual(res, t1, t2, Op.LESS_OR_EQUAL);
                case ">=":
                    return notEqual(res, t1, t2, Op.MORE_OR_EQUAL);
                default:
                    System.out.println("Wrong operator!");
                    return null;
            }
        }catch(OutOfMemoryError E){
            System.out.println("Out of memory.");
            return null;
        }
    }

    private List<VPair<Integer>> equal(List<pair.Pair<String, TreeMap<GroupKey, ArrayList>>> list, String table1, String table2){
        /***/
        TreeMap<GroupKey, ArrayList> g1;
        TreeMap<GroupKey, ArrayList> g2;

        // Make the sure that the order of Pair<Integer> is always: <index of table 1>, <index of table 2> ==> to facilitate building the cartesian table
        if(table1.equals(list.get(0).getKey())) {
            g1 = list.get(0).getValue(); // groups of table1
            g2 = list.get(1).getValue(); // groups of table2
        }else{
            g1 = list.get(1).getValue(); // groups of table1
            g2 = list.get(0).getValue(); // groups of table2
        }

        Set<GroupKey> keys1 = g1.keySet();    // keyset of table1
        Set<GroupKey> keys2 = g2.keySet();    // keyset of table2

        List<TablePair> intersection = new ArrayList<>();

        int tmp = 0;
        for(GroupKey k: keys1){
            if(keys2.contains(k)) {
                intersection.add(new TablePair(k, table1, table2, g1.get(k), g2.get(k)));
                tmp += g1.get(k).size() * g2.get(k).size();
            }
        }

        System.out.println("There will be "+ tmp + " items in the join product");

        return getCartesianList(intersection);
    }

    private List<VPair<Integer>> notEqual(List<pair.Pair<String, TreeMap<GroupKey, ArrayList>>> res, String table1, String table2, Op op){
        TreeMap<GroupKey, ArrayList> g1;
        TreeMap<GroupKey, ArrayList> g2;

        if(table1.equals(res.get(0).getKey())) {
            g1 = res.get(0).getValue(); // groups of table1
            g2 = res.get(1).getValue(); // groups of table2
        }else{
            g1 = res.get(1).getValue(); // groups of table1
            g2 = res.get(0).getValue(); // groups of table2
        }

        Set<GroupKey> keys1 = g1.keySet();    // keyset of table1
        Set<GroupKey> keys2 = g2.keySet();    // keyset of table2

        List<TablePair> ind_pairs = new ArrayList<>();

        // TODO delete
        //System.out.println("\nevaluating AND");
        int tmp = 0;
        for(GroupKey k1: keys1){
            ArrayList accum = new ArrayList();  // add qualifying ArrayLists in table2 to l2
            for(GroupKey k2: keys2){
                switch (op){
                    case NOT_EQUAL:
                        if(k1 != k2) {
                            ArrayList l = g2.get(k2);
                            accum.addAll(l);
                        }
                        break;
                    case LESS_THAN:
                        if((Integer)k1.getKey()[0] < (Integer)k2.getKey()[0]) {
                            ArrayList l = g2.get(k2);
                            accum.addAll(l);
                        }
                        break;
                    case MORE_THAN:
                        if((Integer)k1.getKey()[0] > (Integer)k2.getKey()[0]) {
                            ArrayList l = g2.get(k2);
                            accum.addAll(l);
                        }
                        break;
                    case LESS_OR_EQUAL:
                        if((Integer)k1.getKey()[0] <= (Integer)k2.getKey()[0]) {
                            ArrayList l = g2.get(k2);
                            accum.addAll(l);
                        }
                        break;
                    case MORE_OR_EQUAL:
                        if((Integer)k1.getKey()[0] >= (Integer)k2.getKey()[0]) {
                            ArrayList l = g2.get(k2);
                            accum.addAll(l);
                        }
                        break;
                    default:
                        System.out.println("Operator error.");
                        return null;
                }
            }
            //System.out.println("key " + k1.toString());
            //System.out.println(g1.get(k1));
            //System.out.println(accum);
            tmp += g1.get(k1).size() * accum.size();
            ind_pairs.add(new TablePair(k1, table1, table2, g1.get(k1), accum));
        }

        System.out.println("There will be "+ tmp + " items in the join product");

        return getCartesianList(ind_pairs);
    }

    //====================================================================================================== CARTESIAN
    private void cartesianProductTable(List<VPair<Integer>> cartesian){
        /**
         * @param t1_shared: names of columns as primary key to the join operation (columns to the left of a (=) condition)
         * @param t2_shared: names of columns as primary key to the join operation (columns to the rigth of a (=) condition)
         * t1_shared and t2_shared correspond to each other.
         * */
        // ================== get tables and their names ==================
        System.out.println("\n...Busy generating joined table...\n");

        String name1 = this.t1.name;
        String name2 = this.t2.name;
        Table t1 = this.t1;
        Table t2 = this.t2;
        /*
        try {
            name1 = P.get(0).table1;
            name2 = P.get(0).table2;
            t1 = db.getTable(name1);  // make sure the tables align with what's store in the List<TablePair>
            t2 = db.getTable(name2);
        }catch(IndexOutOfBoundsException e){
            System.out.println("Product of this joint operation is empty.");
            return;
        }*/

        // ================== set schema ==================
        Map<String, Integer> schema1 = t1.getSchema();
        Map<String, Integer> schema2 = t2.getSchema();
        Map<String, Integer> new1 = new HashMap<>();  // mapping table 1 column names to the target table col number
        Map<String, Integer> new2 = new HashMap<>();  // mapping table 2 column names to the target table col number
        List<String> newSchema = new ArrayList<>();
        // add shared columns to the new table at the front
        //newSchema.addAll(t1_shared);
        // add the rest of t1's columns to the new table
        int i = 1;
        for(String col: schema1.keySet()){
            /*
            if(!t1_shared.contains(col))
                newSchema.add(name1 + col); */
            newSchema.add(name1 + "_" + col);
            new1.put(name1 + "_" + col, i++);
        }
        // add the rest of t2's columns to the new table
        for(String col: schema2.keySet()){
            /*
            if(!t2_shared.contains(col))
                newSchema.add(name2 + col);*/
            newSchema.add(name2 + "_" + col);
            new2.put(name2 + "_" + col, i++);
        }
        target.setSchema(newSchema.toArray(new String[0]));
        // test
        System.out.println(target.schemaToString());

        // ================== Take cartesian product ==================
        // TODO NOT NECESSARY TO PUT SHARED COLUMN AT THE FRONT. THERE SHOULD ALSO BE A COPY
        try {
            ArrayList<ArrayList> data1 = t1.getData();
            ArrayList<ArrayList> data2 = t2.getData();
            ArrayList<ArrayList> targetData = target.getData();

            i = 0;
            Integer ind1, ind2;
            for (VPair<Integer> p : cartesian) {
                ind1 = p.val1;
                ind2 = p.val2;

                // add new index
                targetData.get(0).add(i++);

                Integer toCol;
                Integer fromCol;
                /*
                // add shared cols
                for(String s: t1_shared){
                    fromCol = schema1.get(s);
                    toCol = targetSchema.get(s);
                    targetData.get(toCol).add(data1.get(fromCol).get(ind1));
                    System.out.println("Adding shared col");
                 }*/

                 // add t1's col
                try {
                    for (String s : schema1.keySet()) {
                        //System.out.println(s);
                        fromCol = schema1.get(s);
                        //toCol = targetSchema.get(name1+s);
                        toCol = new1.get(name1 + "_" + s);
                        //if(toCol == null) continue; // shared key
                        targetData.get(toCol).add(data1.get(fromCol).get(ind1));
                        //System.out.println("from col: " + fromCol + " to col: " + toCol);
                    }


                    // add t2's col
                    for (String s : schema2.keySet()) {
                        fromCol = schema2.get(s);
                        //toCol = targetSchema.get(name2+s);
                        toCol = new2.get(name2 + "_" + s);
                        targetData.get(toCol).add(data2.get(fromCol).get(ind2));
                        //System.out.println("from col: " + fromCol + " to col: " + toCol);
                    }
                }catch(OutOfMemoryError E){
                    System.out.println("The table resulted is too big. We've run out of memory.");
                    return;
                }
            }
            //System.out.println("Size of cartesian product: " + cartesian.size());
        }catch(Exception e){
            System.out.println("Failed to compute cartesian product.");
            return;
        }

        target.printData();
    }

    private static List<VPair<Integer>> getCartesianList(List<TablePair> P){
        /**
         * return the list of (indices of) cartesian product for the two tables
         *
         * */
        List<VPair<Integer>> cart_prod = new ArrayList<>();

        try{
            for (TablePair t : P)
                cart_prod.addAll(t.computeCartesian());
        }catch(Exception e){
            System.out.println("Couldn't get cartesian product for table pairs");
        }

        return cart_prod;
    }

    private static class TablePair{
        GroupKey key;
        String table1;
        String table2;
        ArrayList t1_elems;
        ArrayList t2_elems;
        private List<VPair<Integer>> cartesian;

        private TablePair(GroupKey key, String t1, String t2, ArrayList t1_elems, ArrayList t2_elems){
            this.key = key;
            this.table1 = t1;
            this.table2 = t2;
            this.t1_elems = t1_elems;
            this.t2_elems = t2_elems;
        }

        private List<VPair<Integer>> computeCartesian() {
            try {
                cartesian = new ArrayList<>();
                ArrayList<Integer> elems1 = this.t1_elems;
                ArrayList<Integer> elems2 = this.t2_elems;
                for (Integer ind1 : elems1) {
                    for (Integer ind2 : elems2) {
                        cartesian.add(new VPair(ind1, ind2));
                    }
                }
                return cartesian;
            }catch(Exception e) {
                System.out.println("Couldn't compute Cartesian product for key: " + key.toString());
                return null;
            }
        }
    }

    private static class VPair<V> implements Comparable<VPair<V>>, Comparator<VPair<V>>{
        V val1;
        V val2;

        private VPair(V ind1, V ind2){
            this.val1 = ind1;
            this.val2 = ind2;
        }

        @Override
        public int compareTo(VPair<V> another){
            if(this.val1 instanceof Integer) {
                Integer res = Integer.parseInt(String.valueOf(this.val1)) - Integer.parseInt(String.valueOf(another.val1));
                //return Integer.parseInt(String.valueOf(this.key[0])) - Integer.parseInt(String.valueOf(anotherKey.getKey()[0]));
                //System.out.println(res);
                return res;
            }
            return String.valueOf(this.val1).compareTo(String.valueOf(another.val1));
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof VPair) {
                VPair<V> tmp = (VPair<V>)obj;
                return val1.equals(tmp.val1) && val2.equals(tmp.val2);
            }
            return false;
        }

        @Override
        public int compare(VPair<V> k1, VPair<V> k2){
            if(k1.val1 instanceof Integer) {
                Integer res = Integer.parseInt(String.valueOf(k1.val1)) - Integer.parseInt(String.valueOf(k2.val2));
                //return Integer.parseInt(String.valueOf(this.key[0])) - Integer.parseInt(String.valueOf(anotherKey.getKey()[0]));
                //System.out.println(res);
                if(res < 0)
                    return -1;
                if(res == 0)
                    return 0;
                return 1;
            }
            return String.valueOf(k1.val1).compareTo(String.valueOf(k2.val2));
        }
    }
}
