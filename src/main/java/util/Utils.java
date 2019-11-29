package util;

import db.*;
import parser.Parser;

import java.util.*;

public class Utils{

    public static double calculateAverage(ArrayList col) {
        try {
            double sum = calculateSum(col);
            if (col != null && col.size() != 0)
                return sum / col.size();
            return sum;
        }catch(Exception e){
            System.out.println("Exception when calculating avg.");
            return 0.0;
        }
    }

    public static double calculateSum(ArrayList<Integer> col) {
        try{
            double sum = 0;
            if(col != null && col.size() != 0) {
                for(Integer mark : col) {
                    //System.out.print(mark + " ");
                    sum += mark;
                }
            }
            return sum;
        }catch(Exception e){
            System.out.println("Exception when calculating sum.");
            return 0.0;
        }
    }

    public static Table getFromTable(String btwParens, Database db){
        String[] inside = btwParens.split(",");
        Table fromTable = db.getTable(Parser.trim_cond(inside[0].trim()));
        if(fromTable == null)
            System.out.println("The table " + inside[0].trim() + " doesn't exist in the database.");

        return fromTable;
    }

    public static String getNthArg(String btwParens, int N){
        /**
         * get the @param Nth argument from between parens.
         * N is 1-based.
         * */
        String[] inside = btwParens.split(",");
        try {
            return Parser.trim_cond(inside[N - 1].trim());
        }catch (Exception e){
            System.out.println("N=" + N + " is out of bounds.");
            return null;
        }
    }

    public static ArrayList getCol(String btwParens, Database db){
        String[] inside = btwParens.split(",");
        Table fromTable = getFromTable(btwParens, db);

        try {
            return fromTable.getData().get(fromTable.getSchema().get(inside[1].trim()));
        }catch(Exception e){
            System.out.println("Error while extracting the column from table.");
            return null;
        }
    }

    public static Map<String, Integer> sortMapByValue(Map<String, Integer> unsortMap) {
        /**
         * Utility function to sort the schema by its value (indes)
         * */
        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/

        return sortedMap;
    }
}
