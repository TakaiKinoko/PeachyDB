package aggregation;

import db.*;
import parser.*;
import util.*;

public class Aggregate {
    private Database db;

    public Aggregate(Database db){
        this.db = db;
    }

    // UPDATED
    public void avg(String s) {
        try {
            String toVar = Parser.get_toTable(s);
            String btwParens = Parser.get_conditions(s);
            String[] col;
            Double res;

            col = Utils.getCol(btwParens, db);
            //db.getTable(fromTable).getData().get(col);
            res = Utils.calculateAverage(col);

            Variable var = new Variable(toVar, res);
            db.newVariable(var);

            System.out.printf("%s: %.4f\n", toVar, (double)var.getValue());

        } catch (Exception e) {
            System.out.println("Exception ");
        }
    }

    // UPDATED
    public void sum(String s) {
        try {
            String toVar = Parser.get_toTable(s);
            String btwParens = Parser.get_conditions(s);
            Table fromTable;
            String[] col;
            Double res;

            col = Utils.getCol(btwParens, db);
            //db.getTable(fromTable).getData().get(col);
            res = Utils.calculateSum(col);

            Variable var = new Variable(toVar, res);
            db.newVariable(var);
            //System.out.printf("%s: %.4f\n", toVar, res);
            System.out.printf("%s: %.4f\n", toVar, (Double)var.getValue());

        } catch (Exception e) {
            System.out.println("Exception.");
        }
    }

    // UPDATED
    public void count(String s) {
        try {
            String toVar = Parser.get_toTable(s);
            String btwParens = Parser.get_conditions(s);
            String[] col;
            int res;

            col = Utils.getCol(btwParens, db);
            res = col.length;

            Variable var = new Variable(toVar, res);
            db.newVariable(var);
            //System.out.printf("%s: %.4f\n", toVar, res);
            System.out.printf("%s: %d\n", toVar, (Integer)var.getValue());
        } catch (Exception e) {
            System.out.println("Exception.");
        }
    }

}
