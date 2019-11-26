package algebra;

import db.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Select {
    Database db;

    public Select(Database db){
        this.db = db;
    }

    public void select(String s){
        /**
         * @param "select" query
         * @return a table containing the result of this query
         *
         * Calls ConditionEvaluator.evaluate() to parse the conditions string
         * */
        Pattern name_p = Pattern.compile("([a-zA-Z]+\\d*)(\\s*):=");
        Pattern cond_p = Pattern.compile("\\((.*?)\\)");
        Matcher cond_matcher = cond_p.matcher(s);
        Matcher name_matcher = name_p.matcher(s);

        try {
            String toTable = ""; // toTable: new table to store the query result in
            String cond = "";// cond: query conditions
            Table fromTable = null, res; // fromTable: name of the table to realize the query in; res: the resulting table of the query

            if (cond_matcher.find()) {
                String[] inside = cond_matcher.group(1).split(",");
                fromTable = db.getTable(inside[0].trim());
                cond = inside[1].trim();
            }
            if (name_matcher.find()) {
                toTable = name_matcher.group(1);
                //TODO: delete the test println
                System.out.println(toTable);
            }

            // update database
            //TODO CALL COND INTERPRETER
            if(cond.equals("") && toTable.equals("") && fromTable != null) {
                res = ConditionEvaluator.evaluate(toTable, cond, fromTable);
                // add resulting table to the database
                db.addTable(res);
            }

        }catch(Exception e){
            System.out.println();
            return;
        }
    }
}
