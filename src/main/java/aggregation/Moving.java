package aggregation;

import db.*;
import parser.Parser;
import util.Utils;
import java.util.ArrayList;
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

    private void apply(String s, Op op){
        /**
         *
         * */
        String btwParens, toTable;
        Table fromTable;
        ArrayList col;
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
            db.newTable(toTable);

            // TODO schema
            String[] cols = new String[fromTable.getSchema().size()+1];
            String[] projects = new String[cols.length - 1];
            int i = 0;
            for(String c: fromTable.getSchema().keySet()){
                projects[i] = c;
                cols[i++] = c;
            }
            cols[i] = "mov_sum";

            db.setSchema(cols, toTable);
            int movsum_col = db.getTable(toTable).getSchema().get("mov_sum");
            ArrayList movsum = db.getTable(toTable).getData().get(movsum_col); // mov_sum column

            // TODO populate table
            db.projectTable(fromTable.name, toTable, projects);

            // TODO compute moving sum
            Deque<Integer> Q = new LinkedList<Integer>();

            for(int j = 0; j < fromTable.getData().get(0).size(); j++){
                if(Q.size() >= window)
                    Q.removeFirst();
                Q.addLast((Integer)(col.get(j)));
                Integer sum = sumDeque(Q);
                if(op == Op.SUM)
                    movsum.add(sum);
                else if(op == Op.AVG)
                    movsum.add(sum / window);
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
