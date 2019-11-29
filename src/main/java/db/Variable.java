package db;

public class Variable {
    public String name;
    private Object value;

    public Variable(String name, Object value){
        this.name = name;
        this.value = value;
    }

    public Object getValue(){
        return value;
    }
}
