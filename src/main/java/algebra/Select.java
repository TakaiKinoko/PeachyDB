package algebra;

import btree.BTree;
import index.BTTestIteratorImpl;
import index.BtreeKey;
import parser.*;
import db.*;

import java.io.IOException;
import java.util.*;

public class Select {
    Database db;

    public Select(Database db){
        this.db = db;
    }

    public void select(String s){
        /**
         * @param "select" query
         * @return a table containing the result of this query
         *
         * Calls ConditionEvaluator.evaluate() to parse the conditions string
         * TODO haven't dealt with the arithops within each condition!!
         * TODO equal sign is =, not ==
         * */
        Table res;

        try {
            String toTable = Parser.get_toTable(s); // toTable: new table to store the query result in
            String withinParens = Parser.get_conditions(s); // query string between the parens
            String cond = "";// cond: query conditions
            Table fromTable = null; // fromTable: name of the table to realize the query in; res: the resulting table of the query

            if (withinParens != null && withinParens.length() != 0) {
                String[] inside = withinParens.split(",");
                System.out.println("Select from table: "+ inside[0].trim());
                fromTable = db.getTable(inside[0].trim());
                if(fromTable == null)
                    System.out.println("The table " + inside[0].trim() + " doesn't exist in the database.");
                cond = inside[1].trim();
                System.out.println("Select from table: "+ fromTable.name);
                System.out.println("Conditions: "+ cond);
            }


            // update database
            if(!cond.equals("") && !toTable.equals("") && fromTable != null) {
                res = evaluateSelect(toTable.trim(), cond, fromTable, db);
                System.out.println("FINE AFTER EVALUATESELECT");
                // add resulting table to the database
                //db.addTable(res);
            }

        }catch(Exception e){
            System.out.println("Exception happened while analyzing query string.");
        }
    }

    // UPDATED
    public static Table evaluateSelect(String to_table, String cond, Table from_table, Database db) throws IOException{
        /**
         * used with "select" query, called from algebra.Select.
         * @param to_table: name of the resulting table
         * @param cond: string containing the query conditions
         * @param db: database to get the target table from
         * @param from_table: name of the table to perform the query on
         *
         * @return: a new table with name of @to_table in the @db that satisfies query conditions @cond
         * */
        //Table res = new Table(to_table);  // need to set schema and data for table res
        Table res;

        try{
            db.newEmptyTable(to_table);
            db.getTable(to_table).isDerivative();  // mark this table as derivative
            res = db.getTable(to_table);
        }catch(Exception e){
            System.out.println("Couldn't create destination table.");
            return null;
        }

        List<Integer> ind_selected;
        Map<String, Integer> from_schema = from_table.getSchema();

        if(Parser.is_arith_string(cond)) {
            // one condition
            ind_selected = evaluate_arith(cond, from_schema, from_table);
            //for(Integer i: ind_selected)
            //System.out.println("ARITH RESULT: " + i);
        }
        else{
            // two conditions
            String[] parts = Parser.bool_match(cond);
            //System.out.println("BEFORE TRIMMING: " + parts[0]);
            String cond1 = parts[0].replaceAll("^\\(+", "");  // get rid of leading open parens
            cond1 = cond1.replaceAll("\\)+$", "");            // get rid of trailing close parens
            //System.out.println("AFTER TRIMMING: " + cond1);

            List<Integer> left = evaluate_arith(cond1, from_schema, from_table);
            //TODO delete test lines
            if(left.size() == 0)
                System.out.println("\nDidn't find any matching record.");
            //for(Integer i: left)
            //System.out.println("res: " + i);

            List<Integer> right = evaluate_arith(Parser.trim_cond(parts[2]), from_schema, from_table);
            //TODO delete test lines
            if(right.size() == 0)
                System.out.println("\nDidn't find any matching record.");
            //for(Integer i: right)
            //System.out.println("res: " + i);

            switch (parts[1]){
                case "and":
                    ind_selected = intersect_lists(left, right);
                    break;
                case "or":
                    ind_selected = merge_lists(left, right);
                    break;
                default:
                    System.out.println("Error occurred valuating boolean.");
                    return res;
            }
        }

        if(ind_selected.size() == 0)
            System.out.println("\nDidn't find any matching record.");


        // TODO set schema for new table
        String[] newcols = new String[from_schema.size()];
        int i = 0;
        for(String col: from_schema.keySet()){
            newcols[i++] = col;
        }
        db.setSchema(newcols, to_table);
        System.out.println("FINE AFTER SETTING SCHEMA");
        // TODO populate the new table
        if(!db.copySubset(from_table.name, to_table, ind_selected))
            System.out.println("Error when populating the resulting table.");
        // TODO print out new table
        System.out.println("FINE AFTER POPULATING NEW TABLE");

        System.out.println("Size:" + res.getTableSize());
        res.printData();
        return res;
    }

    public static List<Integer> intersect_lists(List<Integer> left, List<Integer> right){
        /**
         * In case of AND, just take the intersection of two lists without duplicate
         *
         * @param left:
         * @param right:
         * */
        List<Integer> res = new ArrayList<>();

        // TODO delete
        //System.out.println("\nevaluating AND");
        for(Integer i: left){
            if(right.contains(i)) {
                res.add(i);
                //System.out.println("added: " + i);
            }
            /*
            if(!right.contains(i))
                left.remove(i);*/
        }

            /*
            for(Integer i: res)
                System.out.println("Intersection product: " + i);
            */
        return res;
    }

    public static List<Integer> merge_lists(List<Integer> left, List<Integer> right){
        /**
         * In case of OR, just merge two lists without duplicate
         *
         * @param left:
         * @param right:
         * */
        // TODO delete
        //System.out.println("evaluating OR");
        // remove duplicate
        left.removeAll(right);
        // merge two lists
        left.addAll(right);
        // sort
        Collections.sort(left);

            /*
            for(Integer i: left)
                System.out.println("Merging product: " + i);
            */
        return left;
    }

    // UPDATED
    public static List<Integer> evaluate_arith(String s, Map<String, Integer> schema, Table table) throws IOException {
        /**
         * @return the indices of the entries selected
         * */
        List<Integer> res = new ArrayList<>();
        String[] qs = Parser.arith_match(s);
        String[] col;
        String constraint;
        boolean reverse;

        // TODO delete
        //System.out.println("\nEvaluating arithmetic expression without index.");
        //System.out.println(qs[0]);
        //System.out.println(qs[1]);
        //System.out.println(qs[2]);
        //for(String k: schema.keySet())
        //    System.out.println(k);
        if (schema.keySet().contains(qs[0])) {
            col = table.getData()[schema.get(qs[0])];   // column is on the left of the operator
            constraint = qs[2];
            reverse = false;
        }
        else {
            col = table.getData()[schema.get(qs[2])];   // column is on the right of the operator
            constraint = qs[0];
            reverse = true;
        }

        // if previously have built a hash index on this column:
        if(table.hash_indices != null && table.hash_indices.get(qs[0].trim()) != null){
            System.out.println("Column " + qs[0] + " has been hash indexed.");
            return select_from_hash(qs[0], constraint, qs[1], reverse, table);
        }

        // if previously have built a btree index on this column:
        if(table.btree_indices != null && table.btree_indices.get(qs[0].trim()) != null){
            System.out.println("Column " + qs[0] + " has been btree indexed.");
            return select_from_btree(qs[0], constraint, qs[1], reverse, table);
        }

        // decide what operation it is
        //System.out.println("OPERATOR: "+ qs[1]);
        switch(qs[1]){
            case ">":
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (!reverse && Integer.valueOf(col[i]) > Integer.valueOf(constraint))
                            res.add(i);
                        else if(reverse && Integer.valueOf(col[i]) < Integer.valueOf(constraint))
                            res.add(i);
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case "<":
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (!reverse && Integer.valueOf(col[i]) < Integer.valueOf(constraint))
                            res.add(i);
                        else if (reverse && Integer.valueOf(col[i]) > Integer.valueOf(constraint))
                            res.add(i);
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case ">=":
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (!reverse && Integer.valueOf(col[i]) >= Integer.valueOf(constraint))
                            res.add(i);
                        else if(reverse && Integer.valueOf(col[i]) <= Integer.valueOf(constraint))
                            res.add(i);
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case "<=":
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (!reverse && Integer.valueOf(col[i]) <= Integer.valueOf(constraint))
                            res.add(i);
                        else if(reverse && Integer.valueOf(col[i]) >= Integer.valueOf(constraint))
                            res.add(i);
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case "=":
                //System.out.println("equallll");
                try {
                    for (int i = 0; i < col.length; i++) {
                        //System.out.println(col.get(i)  + " " + String.valueOf(col.get(i)));
                        if (col[i].equals(constraint))
                            res.add(i);
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case "!=":
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (!col[i].equals(constraint))
                            res.add(i);
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            default:
                System.out.println("Unknown operator.");
                break;
        }
        return res;
    }

    private static List<Integer> select_from_hash(String col, String constraint, String op,  Boolean reverse, Table table){
        HashMap<String, List<Integer>> index;
        try {
            index = table.hash_indices.get(col);
        }catch(NullPointerException n){
            System.out.println("Hash index don't exist.");
            return null;
        }

        List<Integer> res = new ArrayList<>();

        switch(op){
            case "=":
                return index.get(constraint);
            case "!=":
                for(String k: index.keySet()){
                    if(!k.equals(constraint))
                        res.addAll(index.get(k));
                }
                return res;
            case ">":
                for(String k: index.keySet()){
                    if(reverse && Integer.valueOf(k) < Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                    else if(!reverse && Integer.valueOf(k) > Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                }
                return res;
            case "<":
                for(String k: index.keySet()){
                    if(reverse && Integer.valueOf(k) > Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                    else if(!reverse && Integer.valueOf(k) < Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                }
                return res;
            case ">=":
                for(String k: index.keySet()){
                    if(reverse && Integer.valueOf(k) <= Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                    else if(!reverse && Integer.valueOf(k) >= Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                }
                return res;
            case "<=":
                for(String k: index.keySet()){
                    if(reverse && Integer.valueOf(k) >= Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                    else if(!reverse && Integer.valueOf(k) <= Integer.valueOf(constraint))
                        res.addAll(index.get(k));
                }
                return res;
            default:
                System.out.println("Unrecognized operator.");
                return res;
        }

    }

    private static List<Integer> select_from_btree
            (String col, String constraint, String op,  Boolean reverse, Table table) throws IOException {
        BTree<BtreeKey, List<Integer>> index;
        try {
            index = table.btree_indices.get(col);
        }catch(NullPointerException n){
            System.out.println("Btree index does't exist.");
            return null;
        }

        List<Integer> res = new ArrayList<>();
        BTTestIteratorImpl<BtreeKey, List<Integer>> iter = new BTTestIteratorImpl<>();
        List<BtreeKey> keySet = index.getKeySet(iter);
        BtreeKey constr = new BtreeKey(constraint);

        switch(op){
            case "=":
                return index.search(new BtreeKey(constraint));
            case "!=":
                for(BtreeKey k: keySet){
                    if(!k.equals(constraint))
                        res.addAll(index.search(k));
                }
                return res;
            case ">":
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) < 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) > 0)
                        res.addAll(index.search(k));
                }
                return res;
            case "<":
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) > 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) < 0)
                        res.addAll(index.search(k));
                }
                return res;
            case ">=":
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) <= 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) >= 0)
                        res.addAll(index.search(k));
                }
                return res;
            case "<=":
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) >= 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) <= 0)
                        res.addAll(index.search(k));
                }
                return res;
            default:
                System.out.println("Unrecognized operator.");
                return res;
        }

    }
}
