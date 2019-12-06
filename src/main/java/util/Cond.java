package util;

import parser.Parser;

public class Cond{
    /**
     * Store condition in the form of <column> <arithop [+|-|*|/]> <contant>
     * if type is CONSTANT, meaning only constant is not empty.
     * if type is COLUMN, meaning there's a column and an op
     * if type is OP, meaning this cond is just an operator
     * */
    public Parser.Type type;   // if CONSTANT, then String and ArithOp are empty
    public String col;
    public Parser.ArithOp op;
    public String constant;

    public Cond(String constant){
        // constant can also be a string (in equality and inequality comparison)
        this.type = Parser.Type.CONSTANT;
        this.constant = constant;
    }

    public Cond(String str, boolean isOp, boolean isConstant){
        /**
         * constructor for op and JUST column
         * */
        if(isOp){
            //System.out.println("making op: " + str);
            this.type = Parser.Type.OP;
            switch (str.trim()){
                case("<"):
                    this.op = Parser.ArithOp.LESS;
                    break;
                case(">"):
                    this.op = Parser.ArithOp.MORE;
                    break;
                case("<="):
                    this.op = Parser.ArithOp.LESS_OR_EQUAL;
                    break;
                case(">="):
                    this.op = Parser.ArithOp.MORE_OR_EQUAL;
                    break;
                case("="):
                    this.op = Parser.ArithOp.EQUAL;
                    break;
                case("!="):
                    this.op = Parser.ArithOp.NOT_EQUAL;
                    break;
                default:
                    break;
            }
        }else if (isConstant){
            this.type = Parser.Type.CONSTANT;
            this.constant = str.trim();
        }else{
            this.type = Parser.Type.COLUMN;
            this.col = str.trim();
        }

    }

    public Cond(String col, String op, Double constant){
        /**
         * constructor for column condition
         *
         * only when it's a numerical column, it is legal to do arithmetic operation on. but still store as String
         * */
        this.type = Parser.Type.COLUMNOP;
        this.col = col.trim();
        this.constant = String.valueOf(constant);
        switch(op.trim()){
            case "+":
                this.op = Parser.ArithOp.PLUS;
                break;
            case "-":
                this.op = Parser.ArithOp.MINUS;
                break;
            case "*":
                this.op = Parser.ArithOp.MULT;
                break;
            case "/":
                this.op = Parser.ArithOp.DIV;
                break;
        }
    }

    public String toString(){
        switch(type){
            case CONSTANT:
                return "--CONSTANT: " + constant + "\n";
            case COLUMN:
                return "--COLUMN: " + col + "\n";
            case COLUMNOP:
                String op;
                switch(this.op){
                    case MORE:
                        op = ">";
                        break;
                    case LESS:
                        op = "<";
                        break;
                    case MORE_OR_EQUAL:
                        op = ">=";
                        break;
                    case LESS_OR_EQUAL:
                        op = "<=";
                        break;
                    case EQUAL:
                        op = "=";
                        break;
                    case NOT_EQUAL:
                        op = "!=";
                        break;
                    case PLUS:
                        op = "+";
                        break;
                    case MINUS:
                        op = "-";
                        break;
                    case MULT:
                        op = "*";
                        break;
                    case DIV:
                        op = "/";
                        break;
                    default:
                        op = " ";
                }
                return "--COLUMN: " + col + " OP: " + op + " CONSTANT: " + constant + "\n";
            case OP:
                switch(this.op){
                    case MORE:
                        op = ">";
                        break;
                    case LESS:
                        op = "<";
                        break;
                    case MORE_OR_EQUAL:
                        op = ">=";
                        break;
                    case LESS_OR_EQUAL:
                        op = "<=";
                        break;
                    case EQUAL:
                        op = "=";
                        break;
                    case NOT_EQUAL:
                        op = "!=";
                        break;
                    default:
                        op = " ";
                }
                return "--OP: " + op + "\n";
            default:
                return "";
        }
    }
}

