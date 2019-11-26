package interpreter.ast.terminal;

import interpreter.ast.Terminal;

public class True extends Terminal {
    public True() {
        super(true);
    }
    public boolean interpret() {
        return value;
    }
}
