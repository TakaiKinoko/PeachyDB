package interpreter.ast;

public abstract class NonTerminal implements ConditionExpression {
    protected ConditionExpression left, right;
    public void setLeft(ConditionExpression left) {
        this.left = left;
    }
    public void setRight(ConditionExpression right) {
        this.right = right;
    }
}
