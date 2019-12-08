package parser;

import util.Cond;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final Pattern OR_p = Pattern.compile("(.+)(or)(.+)");
    private static final Pattern AND_p = Pattern.compile("(.+)(and)(.+)");
    private static final Pattern ARITH_P = Pattern.compile("([^><!=]+)([><!=]+)([^><!=]+)");
    private static final Pattern consOpCons_p = Pattern.compile("(\\d+\\.*\\d*\\s*)([+|\\-|*|/])(\\s*\\d+\\.*\\d*)"); // constant [+|-|*|/] constant
    private static final Pattern colOpCons_p = Pattern.compile("([a-zA-Z]+[^+\\-*/]*)([+|\\-|*|/])(\\s*\\d+\\.*\\d*)");  // column [+|-|*|/] constant
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
        }else if(and.matches()){
            res[0] = and.group(1).trim();
            res[1] = and.group(2).trim();
            res[2] = and.group(3).trim();
        }else{
            System.out.println("Error occurred while processing boolean expressions");
        }
        return res;
    }

    public static Cond[] arith_match(String s) {
        /**
         * s syntax: <operand> <op> <operand>
         *
         * matching the bottom level arithmetic expression out of the string
         *
         * not fault proof. should check that the string doesn't contain boolean operators before calling this function
         * */
        Cond[] conds = new Cond[3];
        String[] res = new String[3];
        Matcher arith = ARITH_P.matcher(s);
        if(arith.matches()) {
            res[0] = arith.group(1).trim();
            conds[0] = transformArithOp(res[0], true);

            res[1] = arith.group(2).trim();
            conds[1] = new Cond(res[1], true, false);

            res[2] = arith.group(3).trim();
            conds[2] = transformArithOp(res[2], false);
        }else{
            System.out.println("Error occurred while processing arithmetic expressions");
        }
        return conds;
    }


    private static Cond transformArithOp(String operand, boolean leftOfOp){
        /**
         * used privately in arith_match
         *
         * if leftOfOp, this is CONSTANT. else COLUMN
         * */

        // if consOpCons, combine
        Matcher cons_m = consOpCons_p.matcher(trim_cond(operand)); // CONSTANT ARITHOP CONSTANT
        Matcher col_m = colOpCons_p.matcher(trim_cond(operand));

        if(cons_m.matches()){

            Double cons1 = Double.valueOf(cons_m.group(1).trim());
            String op = cons_m.group(2).trim();
            Double cons2 = Double.valueOf(cons_m.group(3).trim());

            switch(op.trim()){
                case("+"):
                    return new Cond(String.valueOf(cons1+cons2));
                case("-"):
                    return new Cond(String.valueOf(cons1 - cons2));
                case("*"):
                    return new Cond(String.valueOf(cons1 * cons2));
                case("/"):
                    try {
                        return new Cond(String.valueOf(cons1 / cons2));
                    }catch(ArithmeticException e){
                        System.out.println("Divide by zero exception.");
                        return null;
                    }
                default:
                    System.out.println("Syntax error in operand.");
                    return null;
            }
        }else if(col_m.matches()){
            String col = col_m.group(1).trim();
            String op = col_m.group(2).trim();
            Double cons2 = Double.valueOf(col_m.group(3).trim());
            return new Cond(col, op, cons2);
        }else{
            if(leftOfOp)
                return new Cond(operand, false, false);
            else
                return new Cond(operand, false, true);
        }
    }

    public enum ArithOp{
        PLUS, MINUS, MULT, DIV, EQUAL, LESS, LESS_OR_EQUAL, MORE, MORE_OR_EQUAL, NOT_EQUAL;
    }

    public enum Type{
        CONSTANT, COLUMNOP, OP, COLUMN;
    }


    public static String[] decomposeOperandFromCond(Cond c){
        assert c.type == Type.CONSTANT || c.type == Type.COLUMN || c.type == Type.COLUMNOP;

        if(c.type == Type.CONSTANT)
            // the rhs of the arithop will be registered as CONSTANT
            return c.constant.split("\\.");
        else
        return c.col.split("\\.");
    }
}