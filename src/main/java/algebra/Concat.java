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
        //TODO what about duplicate??
        String btwParens, toTable, t1, t2;

        try {
            btwParens = Parser.get_conditions(s);
            // System.out.println("Between parens: " + btwParens);
            toTable = Parser.get_toTable(s);
            System.out.println("To table: " + toTable);
            t1 = Utils.getNthArg(btwParens, 1);
            t2 = Utils.getNthArg(btwParens, 2);
            System.out.println("Table 1: " + t1);
            System.out.println("Table 2: " + t2);

            db.concatTables(t1, t2, toTable);
            db.getTable(toTable).printData();
            return;
        }catch(Exception e){
            System.out.println("Exception while parsing query string.");
            return;
        }
    }
}
