package db;

import util.Utils;

import java.util.ArrayList;
import java.util.Map;

public class DynamicTable {
    private ArrayList<ArrayList> data;
    private Map<String, Integer> schema;
    public String name;

    public DynamicTable(String name){
        data = new ArrayList<>();
        this.name = name;
    }

    public void makeStatic(Database db){
        int cols_num = schema.size();
        int lines = data.get(0).size();
        db.newTable(this.name, cols_num, lines);
        db.dynamicTables.remove(this.name);  // remove it as a temporary table from database

        Table target = db.getTable(name);
        String[][] td = target.getData();
        target.updateSchema(this.schema);

        for(int i = 0; i < schema.size(); i++){
            ArrayList l = data.get(i);
            for(int j = 0; j < l.size(); j++)
                td[i][j] = String.valueOf(l.get(j));
        }

        //target.printData();
    }

    public void setSchema(Map<String, Integer> schema){
        this.schema = Utils.sortMapByValue(schema);
        for(int i = 0; i< schema.size(); i++)
            data.add(new ArrayList());
    }

    public void insertData(ArrayList l){
        for(int i = 0; i < schema.size(); i++)
            data.get(i).add(l.get(i));
    }

    public ArrayList<ArrayList> getData(){
        return data;
    }

    public int getTableSize(){
        return data.get(0).size();
    }
}
