package algebra;

import interpreter.ast.ConditionExpression;;
import interpreter.lexer.Lexer;
import interpreter.parser.RecursiveDescentParser;
import db.*;
import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

public class ConditionEvaluator {
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
     * */
    public static final Pattern OR_p = Pattern.compile("\\s*\\((.+)\\)(\\s*or\\s*)\\((.+)\\)\\s*");
    public static final Pattern AND_p = Pattern.compile("\\s*\\((.+)\\)(\\s*and\\s*)\\((.+)\\)\\s*");
    public static final Pattern ARITH_P = Pattern.compile("([^><!=]+)([><!=]+)([^><!=]+)");


    public static Table evaluate(String name, String cond, Table table) {
        /**
         * used with "select" query, called from algebra.Select.
         * @param name: name of the resulting table
         * @param cond: string containing the query conditions
         * @param db: database to get the target table from
         * @param table: name of the target table to realize the query conditions
         *
         * @return: a new table with @name in the @db that satisfies query conditions @cond
         * */
        Table res = new Table(name);

        Lexer lexer = new Lexer(new ByteArrayInputStream(cond.getBytes()));
        RecursiveDescentParser parser = new RecursiveDescentParser(lexer);
        ConditionExpression ast = parser.build();

        // TODO print out new table
        System.out.println(String.format("AST: %s", ast));
        System.out.println(String.format("RES: %s", ast.interpret()));

        return res;
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
            System.out.print("Error occurred while processing boolean expressions");
        }
        return res;
    }

    public static String[] arith_match(String s) {
        /**
         * matching the bottom level arithmetic expression out of the string
         *
         * not fault proof. should check that the string doesn't contain boolean operators before calling this function
         * */
        String[] res = new String[3];
        Matcher arith = ARITH_P.matcher(s);
        if(arith.matches()) {
            res[0] = arith.group(1).trim();
            res[1] = arith.group(2).trim();
            res[2] = arith.group(3).trim();
        }else{
            System.out.print("Error occurred while processing arithmetic expressions");
        }
        return res;
    }
}