package aggregation;

import db.*;
import parser.*;
import util.*;

public class Aggregate {
    private Database db;

    public Aggregate(Database db){
        /**
         * Constructor
         * @param db: database to associate the operations on
         * */
        this.db = db;
    }

    public void avg(String s) {
        /**
         * compute the average of a column
         * @param s: querq string
         *
         * legal format of s: <to_table> := avg(<from_table>, <column_name>)
         * */
        makeAggTable(s, "avg");
    }

    public void sum(String s) {
        /**
         * sum of a column of a table
         * @param s: querq string
         *
         * legal format of s: <to_table> := sum(<from_table>, <column_name>)
         * */
        makeAggTable(s, "sum");
    }

    public void count(String s) {
        /**
         * count the number of entries in a column
         * @param s: querq string
         *
         * legal format of s: <to_table> := count(<from_table>, <column_name>)
         * */
        makeAggTable(s, "count");
    }

    private void makeAggTable(String s, String op){
        /**
         * private method that perform required operations(avg/sum/count) on the table and it's column extracted from
         * query string
         * @param s: raw query string
         * @param op: operator
         * */
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
}
