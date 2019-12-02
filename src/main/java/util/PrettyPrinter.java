package util;

import db.Table;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

public class PrettyPrinter {

    public static String getBox(String content, int len, String boundary){
        return boundary + " " + content + new String(new char[len - content.length() - boundary.length()]).replace("\0", " ");
    }

    public static String getBorder(int len, String boundary, String filler){
        return boundary + getBorderPart(len, filler);
    }

    private static String getBorderPart(int len, String filler){
        // generate border without boundary
        return new String(new char[len]).replace("\0", filler);
    }

    public static void prettyPrintTable(BufferedWriter bw, Table tb) throws IOException {
        int padding = 4;
        int col_num;
        int lines;
        String[][] data;
        try{
            col_num = tb.getSchema().size();
            lines = tb.getTableSize();
            data = tb.getData();
        }catch(NullPointerException E){
            System.out.println("Table doesn't exist.");
            return;
        }

        int[] maxLen = new int[col_num];

        for(String s: tb.getSchema().keySet()) {
            maxLen[tb.getSchema().get(s)] = s.length() + padding;
        }

        Random r = new Random();
        // random sample 10 lines
        for(int i = 0; i < 10; i++){
            int ind = r.nextInt(lines);
            for(String s: tb.getSchema().keySet()) {
                int col = tb.getSchema().get(s);
                String str = tb.getData()[col][ind];
                maxLen[col] = maxLen[col] < (str.length() + padding) ? (str.length() + padding): maxLen[col];
            }
        }

        StringBuilder boundary = new StringBuilder();
        StringBuilder header = new StringBuilder();
        for(String s: tb.getSchema().keySet()) {
            // schema is sorted, so this is fine...
            boundary.append(PrettyPrinter.getBorder(maxLen[tb.getSchema().get(s)], "+", "-"));
            header.append(PrettyPrinter.getBox(s, maxLen[tb.getSchema().get(s)], "|"));
        }
        header.append("|");
        boundary.append("+");

        bw.write(boundary.toString() + "\n");
        bw.write(header.toString() + "\n");
        bw.write(boundary.toString() + "\n");

        //System.out.println(boundary.toString());
        //System.out.println(header.toString());
        //System.out.println(boundary.toString());

        for(int i = 0; i < lines; i++){
            StringBuilder entry = new StringBuilder();
            for(int j = 0; j < col_num; j++)
                entry.append(PrettyPrinter.getBox(data[j][i], maxLen[j], "|"));
            entry.append("|");
            bw.write(entry.toString() + "\n");
            bw.write(boundary.toString() + "\n");
        }
        bw.write("\nNumber of entries: " + lines + "\n");
        //bw.write(boundary.toString() + "\n");
        //bw.close();
    }



    public static void printWelcome(){
        System.out.println("+-----------------------------------------+");
        System.out.println("|           Welcome to PeachyDB           |");
        System.out.println("|                                         |");
        System.out.println("|           @author: Fang Han             |");
        System.out.println("+-----------------------------------------+\n");
    }
}

