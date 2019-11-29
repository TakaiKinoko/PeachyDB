package algebra;

import db.*;
import parser.*;
import java.util.Arrays;

public class Project {
    Database db;

    public Project(Database db){
        this.db = db;
    }

    public void project(String s){
        /**
         * @param "select" query
         * @return a table containing the result of this query
         *
         * Calls ConditionEvaluator.evaluate() to parse the conditions string
         * */

        try {
            String toTable = Parser.get_toTable(s); // toTable: new table to store the query result in
            String withinParens = Parser.get_conditions(s); // query string between the parens

            System.out.println("Project to new table: "+ toTable);
            String fromTable = ""; // fromTable: name of the table to realize the query in; res: the resulting table of the query

            if (withinParens != null && withinParens.length() != 0) {
                String[] inside = withinParens.split(",");
                System.out.println("Project from table: "+ inside[0].trim());
                fromTable = inside[0].trim();
                if(db.getTable(fromTable) == null)
                    System.out.println("The table " + inside[0].trim() + " doesn't exist in the database.");
                String[] cols = new String[inside.length-1];
                for(int i = 0; i < cols.length; i++) {
                    cols[i] = inside[i + 1].trim();
                    System.out.println(inside[i].trim());
                }

                // create new table
                db.newTable(toTable);
                // set up schema
                db.getTable(toTable).setSchema(cols);
                // copy columns
                db.projectTable(fromTable, toTable, cols);

                db.getTable(toTable).printData();
            }
        }catch(Exception e){
            System.out.println("Exception happened while analyzing query string.");
            return;
        }
    }
}
