package aggregation;

import db.*;
import parser.Parser;
import util.Utils;
import java.util.LinkedList;
import java.util.Deque;

public class Moving {
    private Database db;

    private enum Op{
        AVG, SUM;
    }

    public Moving(Database db){
        this.db = db;
    }

    public void movsum(String s){
        /**
         * query syntax:
         * <toTable> := movsum(<fromTable>, <col>, <window_len>)
         * */
        try {
            apply(s, Op.SUM);
        }catch(Exception e){
            System.out.println("Error.");
        }
    }

    public void movavg(String s){
        try {
            apply(s, Op.AVG);
        }catch(Exception e){
            System.out.println("Error.");
        }
    }

    // UPDATED
    private void apply(String s, Op op){
        /**
         *
         * */
        String btwParens, toTable;
        Table fromTable;
        String[] col;
        int window;

        try {
            btwParens = Parser.get_conditions(s);
           // System.out.println("Between parens: " + btwParens);
            toTable = Parser.get_toTable(s);
            //System.out.println("To table: " + toTable);
            fromTable = Utils.getFromTable(btwParens, db);
            col = Utils.getCol(btwParens, db);
            //System.out.println("from table: " + fromTable.name);
            //System.out.println("WINDOW: " + Utils.getNthArg(s, 3));
            window = Integer.valueOf(Utils.getNthArg(s, 3));
            //System.out.println("WINDOW: " + window);
        }catch (Exception e){
            System.out.println("Error encountered parsing the query.");
            return;
        }

        assert window >= 1;  // to prevent divide by 0 error when computing average

        try{
            // TODO newTable
            db.newEmptyTable(toTable);

            System.out.println("StaticTable created.");
            // TODO schema
            String[] cols = new String[fromTable.getSchema().size()+1];
            String[] projects = new String[cols.length];
            int i = 0;
            for(String c: fromTable.getSchema().keySet()){
                projects[i] = c;
                cols[i++] = c;
            }
            cols[i] = "mov_sum";
            System.out.println("Schema prepared.");
            int newColNum = i+1;

            // TODO data
            db.getTable(toTable).initializeDataMatrix(new String[newColNum][fromTable.getTableSize()]);
            String[][] todata = db.getData(toTable);
            String[][] fromdata = fromTable.getData();
            for(int j = 0; j < newColNum - 1; j++){
                todata[j] = fromdata[j];
            }

            System.out.println("from table size: " + fromdata.length + fromdata[0].length);
            System.out.println("to table size: " + todata.length + todata[0].length);

            db.setSchema(cols, toTable);
            System.out.println("Schema set up.");

            int movsum_col = db.getTable(toTable).getSchema().get("mov_sum");
            String[] movsum = db.getTable(toTable).getData()[movsum_col]; // mov_sum column

            for(String n: db.getTable(toTable).getSchema().keySet()){
                System.out.println("result table col: "+ n);
            }
            // TODO populate table
            // TODO -- WRONG?? NEW COLUMN HASN'T BEEN ADDED YET??
            //db.projectTable(fromTable.name, toTable, projects);


            // TODO compute moving sum
            Deque<Integer> Q = new LinkedList<Integer>();

            for(int j = 0; j < fromTable.getTableSize(); j++){
                if(Q.size() >= window)
                    Q.removeFirst();
                Q.addLast(Integer.valueOf(col[j]));
                Integer sum = sumDeque(Q);
                if(op == Op.SUM)
                    movsum[j] = String.valueOf(sum);
                else if(op == Op.AVG)
                    movsum[j] = String.valueOf(sum / window);
            }

            // TODO print table
            db.getTable(toTable).printData();

        } catch (Exception e) {
            System.out.println("Error while processing table.");
        }
    }

    private Integer sumDeque(Deque<Integer> Q){
        Integer sum = 0;
        for(Integer i: Q)
            sum += i;
        return sum;
    }

}
