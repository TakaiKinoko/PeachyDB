package interpreter.ast.nonterminal;

import interpreter.ast.ConditionExpression;
import interpreter.ast.NonTerminal;

public class Not extends NonTerminal {
    public void setChild(ConditionExpression child) {
        setLeft(child);
    }

    public void setRight(ConditionExpression right) {
        throw new UnsupportedOperationException();
    }

    public boolean interpret() {
        return !left.interpret();
    }

    public String toString() {
        return String.format("!%s", left);
    }
}