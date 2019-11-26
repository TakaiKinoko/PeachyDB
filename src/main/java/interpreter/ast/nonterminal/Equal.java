package interpreter.ast.nonterminal;

import interpreter.ast.NonTerminal;

public class Equal extends NonTerminal {
    public boolean interpret() {
        return left.interpret() && right.interpret();
    }
    public String toString() {
        return String.format("(%s = %s)", left, right);
    }
}