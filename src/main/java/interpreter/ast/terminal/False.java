package interpreter.ast.terminal;

import interpreter.ast.Terminal;

public class False extends Terminal {
    public False() {
        super(false);
    }
    public boolean interpret() {
        return value;
    }
}
