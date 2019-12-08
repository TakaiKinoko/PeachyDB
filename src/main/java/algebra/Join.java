package algebra;

import db.*;
import parser.Parser;
import util.*;

import java.util.*;

public class Join {

    /**
     * Algorithm
     * =================================================================================================================
     * Table A       Table B
     * size m         size n
     *
     * Outer loop A, inner loop B
     * Send join condition(s) directly into the table (use switch case for the operators), so that no extra memory is
     * squandered except for the final table that's resulted this way.
     *
     * Memory O(mn)
     * Time O(mn)
     *
     * Only short under 50ms mini-bursts of garbage collection, no long > 5s long pause as the old algorithm
     *
     * =================================================================================================================
     * The old algorithm in JoinOld.java when there are two join conditions:
     *
     * 4 GroupBys using TreeMap, memory O(m) & O(n)
     * get cartesian product memory O(mn)
     *
     *
     * */
    private Database db;
    private Table[] fromTables;
    private DynamicTable target;
    private String conditions;

    private enum Op{
        NOT_EQUAL, MORE_THAN, LESS_THAN, LESS_OR_EQUAL, MORE_OR_EQUAL, EQUAL, AND, OR;
    }

    public Join(Database db, String s){
        /**
         * constructor
         * @param s: query string
         * @param db: database to perform the join operation on
         * */
        this.db = db;
        try {
            String toTable = Parser.get_toTable(s);
            //================================
            // toTable: new table to store the query result in
            // ================================
            db.newDynamicTable(toTable);
            this.target = db.getDynamicTable(toTable);

            // ================================
            //  fromTable: queried tables
            // ================================
            String withinParens = Parser.get_conditions(s); // query string between the parens
            String t1 = Utils.getNthArg(withinParens, 1);
            String t2 = Utils.getNthArg(withinParens, 2);
            fromTables = new Table[2];
            fromTables[0] = db.getTable(t1);
            fromTables[1] = db.getTable(t2);
            conditions = Utils.getNthArg(withinParens, 3);
        }catch(Exception e){
            System.out.println("Exception while parsing join conditions.");
        }
    }

    public void join(){
        /**
         * main entry for the join operation
         * */
        Conditions COND = new Conditions(conditions, fromTables);
        COND.evaluate(target);

        target.makeStatic(db);
        db.getTable(target.name).printData(); // print static table
    }

    class Conditions {
        /**
         * private utility class that gets passed in to each pair of data from the tables and perform boolean operation
         * */
        Table table1;
        Table table2;

        Map<Table, Integer> col_map1; // map tables to the selected column in the first join condition
        Op arith1;
        Boolean reverse1;             // indicate if the tables to the left and right of the operand is in reverse order as table1 and 2

        Map<Table, Parser.ArithOp> arith_map1;  // in case the syntax is [table1].[column1] [+|-|*|\] [table2].[column2]
        Map<Table, String> constant_map1;

        Op bool;                      // null if there's only one join condition

        Map<Table, Integer> col_map2; // null if there's only one condition
        Op arith2;   // null if only one condition
        Boolean reverse2;

        Map<Table, Parser.ArithOp> arith_map2;
        Map<Table, String> constant_map2;


        boolean twoConds;

        Conditions(String conditions, Table[] fromTables){
            /**
             * constructor 1 for when there's only one condition
             * */
            // ensure the tables and condition left/right operands are aligned
            this.table1 = fromTables[0];
            this.table2 = fromTables[1];
            if(Parser.is_arith_string(conditions)) {
                //===================================
                // ONLY JOIN CONDITION
                //===================================
                Cond[] parts = Parser.arith_match(conditions);
                this.arith_map1 = new HashMap<>();
                this.constant_map1 = new HashMap<>();
                this.col_map1 = parseCond(parts, arith_map1, constant_map1);
                this.arith1 = getArithOperator(parts, reverse1);
                this.twoConds = false;

            }else{
                //===================================
                // TWO JOIN CONDITIONS
                //===================================
                String[] conds = Parser.bool_match(conditions);
                this.bool = getBoolOp(conds); // get boolean op
                String cond1 = Parser.trim_cond(conds[0]); // get first condition
                String cond2 = Parser.trim_cond(conds[2]); // get second condition
                Cond[] parts1 = Parser.arith_match(cond1);
                Cond[] parts2 = Parser.arith_match(cond2);
                this.arith_map1 = new HashMap<>();
                this.constant_map1 = new HashMap<>();
                this.arith_map2 = new HashMap<>();
                this.constant_map2 = new HashMap<>();
                this.col_map1 = parseCond(parts1, arith_map1, constant_map1);
                this.col_map2 = parseCond(parts2, arith_map2, constant_map2);
                this.arith1 = getArithOperator(parts1, reverse1);
                this.arith2 = getArithOperator(parts2, reverse2);
                this.twoConds = true;
            }

        }

        Map<Table, Integer> parseCond(Cond[] parts, Map<Table, Parser.ArithOp> arith_map, Map<Table, String> constant_map){
            assert parts.length == 3;

            String[] leftOperand = Parser.decomposeOperandFromCond(parts[0]);
            String[] rightOperand = Parser.decomposeOperandFromCond(parts[2]);
            String t1name = leftOperand[0];// table name of the left operand
            String t2name = rightOperand[0]; // table name of the right operand
            Cond left = parts[0];
            Cond right = parts[2];

            if(reverse1 == null){
                reverse1 = !t1name.equals(table1.name);
            }else{
                reverse2 = !t1name.equals(table1.name);
            }

            Table t1 = db.getTable(t1name);
            Table t2 = db.getTable(t2name);
            String col1 = leftOperand[1]; // column name of the left operand
            String col2 = rightOperand[1]; // column name of the right operand

            Map<Table, Integer> colMap = new HashMap<>();
            colMap.put(t1, t1.getSchema().get(col1));
            colMap.put(t2, t2.getSchema().get(col2));

            // ADDED
            if(left.type == Parser.Type.COLUMNOP) {
                arith_map.put(t1, left.op);
                constant_map.put(t1, left.constant);
            }
            if(right.type == Parser.Type.COLUMNOP) {
                arith_map.put(t2, right.op);
                constant_map.put(t2, right.constant);
            }

            return colMap;
        }

        Op getArithOperator(Cond[] parts, Boolean reverse){
            /**
             * passing in reverse flag will make sure that the order tables in the conditions is aligned with the resulting table
             * */
            assert parts.length == 3 && reverse != null;

            Parser.ArithOp operator = parts[1].op;

            switch(operator){
                case EQUAL:
                    return Op.EQUAL;
                case NOT_EQUAL:
                    return Op.NOT_EQUAL;
                case LESS:
                    return reverse? Op.MORE_THAN : Op.LESS_THAN;
                case LESS_OR_EQUAL:
                    return reverse? Op.MORE_OR_EQUAL : Op.LESS_OR_EQUAL;
                case MORE:
                    return reverse? Op.LESS_THAN : Op.MORE_THAN;
                case MORE_OR_EQUAL:
                    return reverse? Op.MORE_OR_EQUAL : Op.MORE_OR_EQUAL;
                default:
                    System.out.println("Unrecognized arithmetic operator.");
                    return null;
            }
        }

        Op getBoolOp(String[] conds){
            /**
             * @param conds: should be of the format: {<condition1>, <boolean_operator>, <condition2>}
             * */
            assert conds.length == 3;

            switch(conds[1].trim()){
                case "and":
                    return Op.AND;
                case "or":
                    return Op.OR;
                default:
                    System.out.println("Unknown boolean operator. Check syntax.");
                    return null;
            }
        }

        void evaluate(DynamicTable target){
            /**
             * this function evaluates the join conditions on each combination of entries from these two tables and
             * store the resulting table in target
             * */

            // ================== set schema ==================
            Map<String, Integer> newSchema = new HashMap<>();
            int cnt = 0;
            for (String col : table1.getSchema().keySet()) {
                newSchema.put(table1.name + "_" + col, cnt++);
            }
            int colnum_1 = cnt;
            for (String col : table2.getSchema().keySet()) {

                newSchema.put(table2.name + "_" + col, cnt++);
            }
            int colnum_2 = cnt - colnum_1;
            target.setSchema(newSchema);

            // ================== data entry =================
            String[][] data1 = table1.getData();
            String[][] data2 = table2.getData();

            for(int i = 0; i < table1.getTableSize(); i++){
                for(int j = 0; j < table2.getTableSize(); j++) {
                    if(!twoConds && condEval(i, j, col_map1, arith1, arith_map1, constant_map1)) {
                        addData(data1, data2, colnum_1, colnum_2, i, j, target);
                    }else if(twoConds){
                        boolean left = condEval(i, j, col_map1, arith1, arith_map1, constant_map1);
                        boolean right = condEval(i, j, col_map2, arith2, arith_map2, constant_map2);
                        switch(bool){
                            case AND:
                                if(left && right)
                                    addData(data1, data2, colnum_1, colnum_2, i, j, target);
                                break;
                            case OR:
                                if(left || right)
                                    addData(data1, data2, colnum_1, colnum_2, i, j, target);
                        }

                    }
                }
            }

        }

        void addData(String[][] data1, String[][] data2, int colnum_1, int colnum_2, int i, int j, DynamicTable target){
            try {
                ArrayList tmp;
                tmp = new ArrayList();
                for (int col1 = 0; col1 < colnum_1; col1++) {
                    tmp.add(data1[col1][i]);
                }
                for (int col2 = 0; col2 < colnum_2; col2++) {
                    tmp.add(data2[col2][j]);
                }
                target.insertData(tmp);
            }catch(Exception e){
                System.out.println("Couldn't insert data.");
            }
        }

        boolean condEval(int i, int j, Map<Table, Integer> col_map, Op arith, Map<Table, Parser.ArithOp> arith_map, Map<Table, String> constant_map){
            /**
             * @param i: index of table1 entry
             * @param j: index of table2 entry
             * */
            int col1 = col_map.get(table1);
            int col2 = col_map.get(table2);
            String[][] data1 = table1.getData();
            String[][] data2 = table2.getData();
            String orig1 = data1[col1][i];
            String orig2 = data2[col2][j];

            String val1 = arith_map.get(table1) == null? orig1: adjustValue(arith_map, constant_map, orig1, table1);

            String val2 = arith_map.get(table2) == null? orig2: adjustValue(arith_map, constant_map, orig2, table2);

            boolean numerical = Utils.isNumeric(val1);
            switch(arith){
                case EQUAL:
                    if(numerical)
                        return Double.valueOf(val1) - Double.valueOf(val2) == 0.0;
                    return val1.equals(val2);
                case NOT_EQUAL:
                    if(numerical)
                        return Double.valueOf(val1) - Double.valueOf(val2) != 0.0;
                    return !val1.equals(val2);
                case LESS_THAN:
                    return Double.valueOf(val1) < Double.valueOf(val2);
                case LESS_OR_EQUAL:
                    return Double.valueOf(val1) <= Double.valueOf(val2);
                case MORE_THAN:
                    return Double.valueOf(val1) > Double.valueOf(val2);
                case MORE_OR_EQUAL:
                    return Double.valueOf(val1) >= Double.valueOf(val2);
                default:
                    return false;
            }
        }

        String adjustValue(Map<Table, Parser.ArithOp> arith_map, Map<Table, String> constant_map, String val, Table table){

            String constant = constant_map.get(table);
            switch(arith_map.get(table)){
                case PLUS:
                    return String.valueOf(Double.valueOf(val) + Double.valueOf(constant));
                case MINUS:
                    return String.valueOf(Double.valueOf(val) - Double.valueOf(constant));
                case MULT:
                    return String.valueOf(Double.valueOf(val) * Double.valueOf(constant));
                case DIV:
                    return String.valueOf(Double.valueOf(val) / Double.valueOf(constant));
                default:
                    return val;

            }
        }

    }
}
