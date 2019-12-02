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
        makeAggTable(s, "avg");
    }

    // UPDATED
    public void sum(String s) {
        makeAggTable(s, "sum");
    }

    private void makeAggTable(String s, String op){
        try {
            String toTable = Parser.get_toTable(s);
            db.newEmptyTable(toTable);
            Table target = db.getTable(toTable);

            String btwParens = Parser.get_conditions(s);
            String[] col =  Utils.getCol(btwParens, db);

            target.setSchema(new String[]{op + "_" + Utils.getNthArg(btwParens, 2)});

            String[][] data = new String[1][1];
            switch(op){
                case "sum":
                    data[0][0] = String.valueOf(Utils.calculateSum(col));
                    break;
                case "count":
                    data[0][0] = String.valueOf(col.length);
                    break;
                case "avg":
                    data[0][0] = String.valueOf(Utils.calculateAverage(col));
                    break;
            }

            target.updateData(data);
            target.printData();

        } catch (Exception e) {
            System.out.println("Exception.");
        }
    }

    // UPDATED
    public void count(String s) {
        makeAggTable(s, "count");
    }

}
