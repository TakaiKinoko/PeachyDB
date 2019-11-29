package parser;

import db.Database;
import db.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    /**
     * TODO: add to README
     *
     * Characteristics of the expressions:
     * recursive on the boolean (and|or) level (meaning that there can be strings like: (a and (b or (d and e))))
     * but flat on once go inside the arithmetic level (meaning it can only be e.g. (_x == _y))
     * extra note is that on each side of the arithmetic operator, the format is: (Column | Constant) [+|-|*] (Column | Constant)
     *
     * Parsing strategy
     * First: recursively descend into arithmetic level
     * Then: parse arithmetic expression
     *
     *
     *     TODO delete below
     *     public static final Pattern COMPOUND_ARITH_P = Pattern.compile("(.+)([==|!=|>=|<=])(.+)");
     *     public static final Pattern SINGULAR_ARITH_P = Pattern.compile("(.+)([<|>])(.+)");
     *     public static final Pattern EQUAL_P = Pattern.compile("(.+)(==)(.+)");
     *     public static final Pattern NOTEQUAL_P = Pattern.compile("(.+)(!=)(.+)");
     *     public static final Pattern LESS_P = Pattern.compile("(.+)(<)(.+)");
     *     public static final Pattern LE_P = Pattern.compile("(.+)(<=)(.+)");
     *     public static final Pattern MORE_P = Pattern.compile("(.+)(>)(.+)");
     *     public static final Pattern ME_P = Pattern.compile("(.+)(>=)(.+)");
     *
     *     public static final Pattern OR_p = Pattern.compile("\\s*([(|)]*)(\\s*or\\s*)([(|)]*)\\s*");
     *     public static final Pattern AND_p = Pattern.compile("\\s*([(|)]*)(\\s*and\\s*)([(|)]*)\\s*");
     *     public static final Pattern OR_p = Pattern.compile("\\s*\\((.+)\\)(\\s*or\\s*)\\((.+)\\)\\s*");
     *     public static final Pattern AND_p = Pattern.compile("\\s*\\((.+)\\)(\\s*and\\s*)\\((.+)\\)\\s*");
     *
     * */

    private static final Pattern OR_p = Pattern.compile("(.+)(or)(.+)");
    private static final Pattern AND_p = Pattern.compile("(.+)(and)(.+)");
    private static final Pattern ARITH_P = Pattern.compile("([^><!=]+)([><!=]+)([^><!=]+)");
    private static final Pattern name_p = Pattern.compile("([a-zA-Z]+(.)*)(\\s*):=");  // table name has to start with an alphabetic letter
    private static final Pattern cond_p = Pattern.compile("\\((.*?)\\)$");

    public static String get_toTable(String s){
        /**
         * @param s: query String in the form of  "<toTable> := (condition)"
         * @return <toTable>
         *
         * */
        Matcher name_matcher = name_p.matcher(s);
        String toTable = ""; // toTable: new table to store the query result in

        try {
            if (name_matcher.find()) {
                return name_matcher.group(1).trim();
            }else{
                System.out.println("Couldn't extract query destination.");
                return "";
            }
        }catch(Exception e){
            System.out.println("Exception happened while analyzing query string.");
        }
        return toTable;
    }

    public static String get_conditions(String s) {
        /**
         * @param s: query String in the form of  "<toTable> := <command>(<condition>)"
         * @return <condition>
         *
         * */
        Matcher cond_matcher = cond_p.matcher(s);
        String cond = "";// cond: query conditions

        if (cond_matcher.find()) {
            cond = cond_matcher.group(1).trim();
            return cond;
        }else {
            System.out.println("Couldn't extract query conditions.");
            return cond;
        }
    }

    public static String trim_cond(String s){
        return s.replaceAll("^\\(+", "").replaceAll("\\)+$", "");
    }

    public static boolean is_arith_string(String s){
        /**
         * check if the string contains only arithmetic expression and NOT boolean
         * */
        Matcher or = OR_p.matcher(s), and = AND_p.matcher(s);
        if(or.matches() || and.matches())
            return false;
        return true;
    }

    public static String[] bool_match(String s) {
        /**
         * matching the top level boolean expression out of the string
         *
         * not fault proof. should check if the string contains boolean operator before calling this function
         * */
        String[] res = new String[3];
        Matcher or = OR_p.matcher(s);
        Matcher and = AND_p.matcher(s);
        if(or.matches()){
            res[0] = or.group(1).trim();
            res[1] = or.group(2).trim();
            res[2] = or.group(3).trim();
            //System.out.println("OR: " + res[0] + "|" + res[1] + "|" + res[2]);
        }else if(and.matches()){
            res[0] = and.group(1).trim();
            res[1] = and.group(2).trim();
            res[2] = and.group(3).trim();
            //System.out.println("AND: " + res[0] + "|" + res[1] + "|" + res[2]);
        }else{
            System.out.println("Error occurred while processing boolean expressions");
        }
        return res;
    }

    public static String[] arith_match(String s) {
        /**
         * s syntax: <operand> <op> <operand>
         *
         * matching the bottom level arithmetic expression out of the string
         *
         * not fault proof. should check that the string doesn't contain boolean operators before calling this function
         * */
        //System.out.println("DEBUG: "+ s);
        String[] res = new String[3];
        Matcher arith = ARITH_P.matcher(s);
        if(arith.matches()) {
            res[0] = arith.group(1).trim();
            res[1] = arith.group(2).trim();
            res[2] = arith.group(3).trim();
        }else{
            System.out.println("Error occurred while processing arithmetic expressions");
        }
        return res;
    }

    public static String[] decomposeOperand(String s){
        /**
         * s syntax: <table_name>.<col_name>
         * */
        s = trim_cond(s);
        return s.split("\\.");
    }

}