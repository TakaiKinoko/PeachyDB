package util;

public class Parens {
    public static int evaluate(String expression) {
        // here -1 means 'not found yet'
        int plusPos = -1; // position of last '+' outside of parentheses
        int timesPos = -1; // position of last '*' outside of parentheses

        // scan expression, looking for operators outside of parentheses
        int level = 0; // current nesting depth
        for (int i=0; i<expression.length(); i++) {
            char ch = expression.charAt(i);

            // look for operator
            if (ch=='+' && level==0) plusPos = i;
            if (ch=='*' && level==0) timesPos = i;

            // count level of parentheses
            if (ch=='(') level++;
            if (ch==')') level--;
        }

        // recurse on lowest-precedence operator

        if (plusPos != -1) {
            // break down, e.g. "3*4+5*6" => "3*4" and "5*6"
            String exprLeft = expression.substring(0, plusPos);
            String exprRight = expression.substring(plusPos+1);
            int valueLeft = evaluate(exprLeft);
            int valueRight = evaluate(exprRight);
            return valueLeft + valueRight;
        }
        else if (timesPos != -1) {
            // break down, e.g. "(3+4)*(5+6)" => "(3+4)" and "(5+6)"
            String exprLeft = expression.substring(0, timesPos);
            String exprRight = expression.substring(timesPos+1);

            int valueLeft = evaluate(exprLeft);
            int valueRight = evaluate(exprRight);
            return valueLeft * valueRight;

        }
        else if (expression.charAt(0)=='(') {
            // everything was in a matched pair of parentheses
            // break down, e.g. "(3*4)" => "3*4"
            return evaluate(expression.substring(1, expression.length()-1));
        }
        else {
            // base case: just a number. convert expression to int.
            // don't make any recursive calls
            return Integer.parseInt(expression);
        }
    }
}
