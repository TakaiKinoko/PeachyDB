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

    public void setSchema(Map<String, Integer> schema){
        this.schema = Utils.sortMapByValue(schema);

    }

    public void insertData(ArrayList l){
        data.add(l);
    }

    public ArrayList<ArrayList> getData(){
        return data;
    }

    public int getTableSize(){
        return data.size();
    }

    public void printTable(){
        int limit = 20 < getTableSize()? 20 : getTableSize();

        try {
            for (String k : schema.keySet())
                System.out.print(k + "\t");
            System.out.println();
        }catch(Exception e){
            System.out.println("Schema wasn't set up.");
            return;
        }

        ArrayList tmp;
        for(int i = 0; i < limit/2; i++){
            tmp = data.get(i);
            for(int j = 0; j < tmp.size(); j++)
                System.out.print(tmp.get(j) + "\t");
            System.out.println();
        }
        System.out.println("...");
        for(int i = limit/2; i < limit; i++){
            tmp = data.get(i);
            for(int j = 0; j < tmp.size(); j++)
                System.out.print(tmp.get(j) + "\t");
            System.out.println();
        }
        System.out.println("Total: " + getTableSize() + " entries in the table");
    }
}
