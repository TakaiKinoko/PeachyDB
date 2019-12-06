package algebra;

import btree.BTree;
import index.BTTestIteratorImpl;
import index.BtreeKey;
import parser.*;
import db.*;
import util.*;

import java.io.IOException;
import java.util.*;

public class Select {
    Database db;

    public Select(Database db){
        this.db = db;
    }

    public void select(String s) throws IOException{
        /**
         * @param "select" query
         * @return a table containing the result of this query
         *
         * Calls ConditionEvaluator.evaluate() to parse the conditions string
         * TODO haven't dealt with the arithops within each condition!!
         * TODO equal sign is =, not ==
         *
         * Syntax:
         * COND := ((column|constant) [+|-|*|/ constant]? (< | <= | > | >= | = | !=) (column|constant) [+|-|*|/ constant]?)
         * CONDITIONS := COND ((and|or) COND)?
         * QUERY := (to_table) (:=) (select)(\((from_table, CONDITIONS)\))
         *
         * */
        Table res;

        //try {
            String toTable = Parser.get_toTable(s); // toTable: new table to store the query result in
            String withinParens = Parser.get_conditions(s); // query string between the parens
            String cond = "";// cond: query conditions
            Table fromTable = null; // fromTable: name of the table to realize the query in; res: the resulting table of the query

            if (withinParens != null && withinParens.length() != 0) {
                String[] inside = withinParens.split(",");
                //System.out.println("Select from table: "+ inside[0].trim());
                fromTable = db.getTable(inside[0].trim());
                if(fromTable == null)
                    System.out.println("The table " + inside[0].trim() + " doesn't exist in the database.");
                cond = inside[1].trim();
                //System.out.println("Select from table: "+ fromTable.name);
                //System.out.println("Conditions: "+ cond);
            }


            // update database
            if(!cond.equals("") && !toTable.equals("") && fromTable != null) {
                res = evaluateSelect(toTable.trim(), cond, fromTable, db);
                //System.out.println("FINE AFTER EVALUATESELECT");
                // add resulting table to the database
                //db.addTable(res);
            }

        //}catch(Exception e){
            //System.out.println("Exception happened while analyzing query string.");
        //}
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
            //=================================== one condition ========================================================
            ind_selected = evaluate_arith(cond, from_schema, from_table);
            //for(Integer i: ind_selected)
            //System.out.println("ARITH RESULT: " + i);
        }
        else{
            //=================================== two conditions =======================================================
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
        //System.out.println("FINE AFTER SETTING SCHEMA");
        // TODO populate the new table
        if(!db.copySubset(from_table.name, to_table, ind_selected))
            System.out.println("Error when populating the resulting table.");
        // TODO print out new table
        //System.out.println("FINE AFTER POPULATING NEW TABLE");

        //System.out.println("Size:" + res.getTableSize());
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

    private static Double getColValue(Cond column, String[] col, int i){
        Double val;
        switch(column.op){
            case PLUS:
                val = Double.valueOf(col[i]) + Double.valueOf(column.constant);
                break;
            case MINUS:
                val = Double.valueOf(col[i]) - Double.valueOf(column.constant);
                break;
            case MULT:
                val = Double.valueOf(col[i]) * Double.valueOf(column.constant);
                break;
            case DIV:
                val = Double.valueOf(col[i]) / Double.valueOf(column.constant);
                break;
            default:
                val = null;
        }
        return val;
    }
    // UPDATED
    public static List<Integer> evaluate_arith(String s, Map<String, Integer> schema, Table table) throws IOException {
        /**
         * @return the indices of the entries selected
         * */
        List<Integer> res = new ArrayList<>();
        //String[] qs = Parser.arith_match(s);
        Cond[] conds = Parser.arith_match(s);
        Cond column;
        Cond constraint;
        String[] col;
        //String constraint;
        boolean reverse;

        // TODO delete
        //System.out.println("\nEvaluating arithmetic expression without index.");
        //System.out.println(qs[0]);
        //System.out.println(qs[1]);
        //System.out.println(qs[2]);
        //for(String k: schema.keySet())
        //    System.out.println(k);
        //if (schema.keySet().contains(qs[0])) {
        if(conds[0].type == Parser.Type.COLUMN || conds[0].type == Parser.Type.COLUMNOP){
            column = conds[0];
            //col = table.getData()[schema.get(qs[0])];   // column is on the left of the operator
            col = table.getData()[schema.get(column.col)];
            //constraint = qs[2];
            constraint = conds[2];

            //System.out.println("CONSTRAINT: " + constraint.toString());
            reverse = false;
        }
        else {
            column = conds[2];
            col = table.getData()[schema.get(column.col)];
            //col = table.getData()[schema.get(qs[2])];   // column is on the right of the operator
            //constraint = qs[0];
            constraint = conds[0];
            //System.out.println("CONSTRAINT: " + constraint.toString());
            reverse = true;
        }

        //TODO GET THESE INDICES TO WORK

        // if previously have built a hash index on this column:
        //if(table.hash_indices != null && table.hash_indices.get(qs[0].trim()) != null){
        if(table.hash_indices != null && table.hash_indices.get(column.col) != null){
            System.out.println("Column " + column.col + " has been hash indexed.");
            //return select_from_hash(col, constraint, qs[1], reverse, table);
            return select_from_hash(column, constraint, conds[1], reverse, table);
        }

        // if previously have built a btree index on this column:
        //if(table.btree_indices != null && table.btree_indices.get(qs[0].trim()) != null){
        if(table.btree_indices != null && table.btree_indices.get(column.col) != null){
            System.out.println("Column " + column.col + " has been btree indexed.");
            //return select_from_btree(qs[0], constraint, qs[1], reverse, table);
            return select_from_btree(column, constraint, conds[1], reverse, table);
        }

        switch(conds[1].op){
            //case ">":
            case MORE:
                try {
                    for (int i = 0; i < col.length; i++) {
                        if(column.type == Parser.Type.COLUMN){
                            if (!reverse && Double.valueOf(col[i]) > Double.valueOf(constraint.constant))
                                res.add(i);
                            else if(reverse && Double.valueOf(col[i]) <  Double.valueOf(constraint.constant))
                                res.add(i);
                        }else if(column.type == Parser.Type.COLUMNOP){
                            Double val = getColValue(column, col, i);
                            if (!reverse && val >  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && val <  Double.valueOf(constraint.constant))
                                res.add(i);
                        }
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case LESS:
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (column.type == Parser.Type.COLUMN) {
                            if (!reverse && Double.valueOf(col[i]) <  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && Double.valueOf(col[i]) >  Double.valueOf(constraint.constant))
                                res.add(i);
                        } else if (column.type == Parser.Type.COLUMNOP) {
                            Double val = getColValue(column, col, i);
                            if (!reverse && val <  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && val >  Double.valueOf(constraint.constant))
                                res.add(i);
                        }
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case MORE_OR_EQUAL:
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (column.type == Parser.Type.COLUMN) {
                            if (!reverse && Double.valueOf(col[i]) >=  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && Double.valueOf(col[i]) <=  Double.valueOf(constraint.constant))
                                res.add(i);
                        } else if (column.type == Parser.Type.COLUMNOP) {
                            Double val = getColValue(column, col, i);
                            if (!reverse && val >=  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && val <=  Double.valueOf(constraint.constant))
                                res.add(i);
                        }
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case LESS_OR_EQUAL:
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (column.type == Parser.Type.COLUMN) {
                            if (!reverse && Double.valueOf(col[i]) <=  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && Double.valueOf(col[i]) >=  Double.valueOf(constraint.constant))
                                res.add(i);
                        } else if (column.type == Parser.Type.COLUMNOP) {
                            Double val = getColValue(column, col, i);
                            if (!reverse && val <=  Double.valueOf(constraint.constant))
                                res.add(i);
                            else if (reverse && val >=  Double.valueOf(constraint.constant))
                                res.add(i);
                        }
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case EQUAL:
                //System.out.println("equallll");
                boolean numerical = Utils.isNumeric(col[0]);
                try {  // TODO
                    for (int i = 0; i < col.length; i++) {
                        if (column.type == Parser.Type.COLUMN) {
                            //System.out.println(col[i] + " v.s. " + constraint.constant);
                            // String column:
                            if (!numerical && col[i].equals(constraint.constant))
                                res.add(i);
                            // Numerical column:
                            else if (numerical && Double.valueOf(col[i]) - Double.valueOf(constraint.constant) == 0)
                                res.add(i);
                        } else if (column.type == Parser.Type.COLUMNOP) {
                            Double val = getColValue(column, col, i);
                            //System.out.println(val + " v.s. " + constraint.constant);
                            if (!numerical && String.valueOf(val).equals(constraint.constant))
                                res.add(i);
                            else if(numerical && val - Double.valueOf(constraint.constant) == 0)
                                res.add(i);
                        }
                    }
                }catch(Exception e){
                    System.out.println("Operand error.");
                }
                break;
            case NOT_EQUAL:
                numerical = Utils.isNumeric(col[0]);
                try {
                    for (int i = 0; i < col.length; i++) {
                        if (column.type == Parser.Type.COLUMN) {
                            if (!numerical && !col[i].equals(constraint.constant))
                                res.add(i);
                            else if (numerical && Double.valueOf(col[i]) - Double.valueOf(constraint.constant) != 0)
                                res.add(i);
                        } else if (column.type == Parser.Type.COLUMNOP) {
                            Double val = getColValue(column, col, i);
                            if (!numerical && !String.valueOf(val).equals(constraint.constant))
                                res.add(i);
                            else if(numerical && val - Double.valueOf(constraint.constant) != 0)
                                res.add(i);
                        }
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

    //private static List<Integer> select_from_hash(String col, String constraint, String op,  Boolean reverse, Table table){
    private static List<Integer> select_from_hash(Cond col, Cond constraint, Cond op,  Boolean reverse, Table table){
        System.out.println("Using hash index"); // TODO delete
        HashMap<String, List<Integer>> index;
        try {
            index = table.hash_indices.get(col.col);
        }catch(NullPointerException n){
            System.out.println("Hash index don't exist.");
            return null;
        }

        List<Integer> res = new ArrayList<>();

        switch(op.op){
            case EQUAL:
                if(col.type == Parser.Type.COLUMN)
                    // NO ARITHMETIC OP HIDDEN INSIDE
                    return index.get(constraint.constant);
                else if(col.type == Parser.Type.COLUMNOP){
                   return getIndices(col, constraint, index);
                }
            case NOT_EQUAL:
                if(col.type == Parser.Type.COLUMN){
                    for(String k: index.keySet()) {
                        if (!k.equals(constraint.constant))
                            res.addAll(index.get(k));
                    }
                }else if(col.type == Parser.Type.COLUMNOP){
                    for(String k: index.keySet()) {
                        if (!adjustVal(col, k).equals(constraint.constant))
                            res.addAll(index.get(k));
                    }
                }
                return res;
            case MORE:
                for(String k: index.keySet()){
                    if(col.type == Parser.Type.COLUMN) {
                        if (reverse && Double.valueOf(k) < Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(k) > Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }else if(col.type == Parser.Type.COLUMNOP){
                        if (reverse && Double.valueOf(adjustVal(col, k)) < Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(adjustVal(col, k)) > Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }
                }
                return res;
            case LESS:
                for(String k: index.keySet()){
                    if(col.type == Parser.Type.COLUMN) {
                        if (reverse && Double.valueOf(k) > Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(k) < Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }else if(col.type == Parser.Type.COLUMNOP){
                        if (reverse && Double.valueOf(adjustVal(col, k)) > Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(adjustVal(col, k)) < Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }
                }
                return res;
            case MORE_OR_EQUAL:
                for(String k: index.keySet()){
                    if(col.type == Parser.Type.COLUMN) {
                        if (reverse && Double.valueOf(k) <= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(k) >= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }else if(col.type == Parser.Type.COLUMNOP){
                        if (reverse && Double.valueOf(adjustVal(col, k)) <= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(adjustVal(col, k)) >= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }
                }
                return res;
            case LESS_OR_EQUAL:
                for(String k: index.keySet()){
                    if(col.type == Parser.Type.COLUMN) {
                        if (reverse && Double.valueOf(k) >= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(k) <= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }else if(col.type == Parser.Type.COLUMNOP){
                        if (reverse && Double.valueOf(adjustVal(col, k)) >= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                        else if (!reverse && Double.valueOf(adjustVal(col, k)) <= Double.valueOf(constraint.constant))
                            res.addAll(index.get(k));
                    }
                }
                return res;
            default:
                System.out.println("Unrecognized operator.");
                return res;
        }

    }

    private static List<Integer> getIndices(Cond col, Cond constraint, HashMap<String, List<Integer>> index){
        switch(col.op){
            case PLUS:
                return index.get(String.valueOf(Double.valueOf(constraint.constant) - Double.valueOf(col.constant)));
            case MINUS:
                return index.get(String.valueOf(Double.valueOf(constraint.constant) + Double.valueOf(col.constant)));
            case MULT:
                if(Double.valueOf(col.constant).equals(0.0)) // avoid divide by zero exception
                    return index.get(String.valueOf(0));
                return index.get(String.valueOf(Double.valueOf(constraint.constant) / Double.valueOf(col.constant)));
            case DIV:
                return index.get(String.valueOf(Double.valueOf(constraint.constant) * Double.valueOf(col.constant)));
            default:
                return null;
        }
    }

    private static String adjustVal(Cond col, String val){
        switch(col.op){
            case PLUS:
                return String.valueOf(Double.valueOf(val) + Double.valueOf(col.constant));
            case MINUS:
                return String.valueOf(Double.valueOf(val) - Double.valueOf(col.constant));
            case MULT:
                return String.valueOf(Double.valueOf(val) * Double.valueOf(col.constant));
            case DIV:
                return String.valueOf(Double.valueOf(val) / Double.valueOf(col.constant));
            default:
                return "";
        }
    }

    //private static List<Integer> select_from_btree
    //        (String col, String constraint, String op,  Boolean reverse, Table table) throws IOException {
    private static List<Integer> select_from_btree
            (Cond col, Cond constraint, Cond op,  Boolean reverse, Table table) throws IOException {
        BTree<BtreeKey, List<Integer>> index;
        try {
            index = table.btree_indices.get(col.col);
        }catch(NullPointerException n){
            System.out.println("Btree index does't exist.");
            return null;
        }

        System.out.println("Using BTree index"); //TODO selecting from BTREE
        List<Integer> res = new ArrayList<>();
        BTTestIteratorImpl<BtreeKey, List<Integer>> iter = new BTTestIteratorImpl<>();
        List<BtreeKey> keySet = index.getKeySet(iter);
        //BtreeKey constr = new BtreeKey(constraint);
        //BtreeKey constr = adjustConstraint(col, constraint);
        BtreeKey constr;
        if(col.type == Parser.Type.COLUMN)
            constr = new BtreeKey(constraint.constant);
        else
            constr = new BtreeKey(adjustConstraint(col, constraint));

        switch(op.op){
            case EQUAL:
                //System.out.println("EQUAL");
                List<Integer> match = index.search(constr);
                if(match == null)
                    return res;
                res.addAll(match);
                return res;
            case NOT_EQUAL:
                for(BtreeKey k: keySet){
                    if(!k.equals(constr))
                        res.addAll(index.search(k));
                }
                return res;
            case MORE:
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) < 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) > 0)
                        res.addAll(index.search(k));
                }
                return res;
            case LESS:
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) > 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) < 0)
                        res.addAll(index.search(k));
                }
                return res;
            case MORE_OR_EQUAL:
                for(BtreeKey k: keySet){
                    if(reverse && k.compareTo(constr) <= 0)
                        res.addAll(index.search(k));
                    else if(!reverse && k.compareTo(constr) >= 0)
                        res.addAll(index.search(k));
                }
                return res;
            case LESS_OR_EQUAL:
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

    private static String adjustConstraint(Cond COLUMN, Cond constraint){
        String orig_constr = constraint.constant;
        String new_constr = "";
        String constant = COLUMN.constant;

        switch(COLUMN.op){
            case PLUS:
                new_constr = String.valueOf(Double.valueOf(orig_constr) - Double.valueOf(constant));
                break;
            case MINUS:
                new_constr =String.valueOf(Double.valueOf(orig_constr) + Double.valueOf(constant));
                break;
            case MULT:
                if(Double.valueOf(constant).equals(0.0))
                    new_constr = String.valueOf(0);
                else
                    new_constr = String.valueOf(Double.valueOf(orig_constr) / Double.valueOf(constant));
                break;
            case DIV:
                new_constr = String.valueOf(Double.valueOf(orig_constr) * Double.valueOf(constant));
                break;
            default:
                break;
        }

        //System.out.println("OLD CONSTRAINT " + orig_constr);
        //System.out.println("NEW CONSTRAINT " + new_constr);
        return new_constr;
    }
}
