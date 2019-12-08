package algebra;

import db.Database;
import db.Table;
import parser.Parser;
import util.Utils;

public class Concat {
    /**
     *  The two tables being concatenated must have the same schema
     * */
    public static void concat(Database db, String s) {
        /**
         * @param db: database that contains the tables to be concatenated
         * @param s: query string
         * format of s: <to_table> := concat(<table1>, <table2>)
         * */
        String btwParens, toTable, t1, t2;

        try {
            btwParens = Parser.get_conditions(s);
            toTable = Parser.get_toTable(s);
            t1 = Utils.getNthArg(btwParens, 1);
            t2 = Utils.getNthArg(btwParens, 2);

            db.concatTables(t1, t2, toTable);
            db.getTable(toTable).printData();
            return;
        }catch(Exception e){
            System.out.println("Exception while parsing query string.");
            return;
        }
    }
}
