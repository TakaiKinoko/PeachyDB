package util;

import db.Table;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

public class PrettyPrinter {

    public static void printWelcome(){
        System.out.println("+-----------------------------------------+");
        System.out.println("|           Welcome to PeachyDB           |");
        System.out.println("|                                         |");
        System.out.println("|       @author: Fang Han Cabrera         |");
        System.out.println("+-----------------------------------------+\n");
    }

    public static void printGoodbye(){
        System.out.println("+-----------------------------------------+");
        System.out.println("|                                         |");
        System.out.println("|           Bye from Peachy ^.^           |");
        System.out.println("|                                         |");
        System.out.println("|       @author: Fang Han Cabrera         |");
        System.out.println("|                                         |");
        System.out.println("|            @NYU Fall 2019               |");
        System.out.println("|                                         |");
        System.out.println("|    Get in touch: fang@buymecoffee.co    |");
        System.out.println("|                                         |");
        System.out.println("+-----------------------------------------+\n");
    }

    public static void enterCommandMsg(){
        System.out.println("+-----------------------------------------+");
        System.out.println("|             type query                  |");
        System.out.println("|                 or                      |");
        System.out.println("|        type \"quit\" to exit              |");
        System.out.println("+-----------------------------------------+\n");
    }

    public static void prettyPrintTableToFile(BufferedWriter bw, Table tb, boolean crop, boolean borders) throws IOException {
        int[] maxLen = getColLengths(tb);

        bw.write(prettyPrintTableHeader(tb, maxLen) + "\n");
        //System.out.println(boundary.toString());
        //System.out.println(header.toString());
        //System.out.println(boundary.toString());

        bw.write(prettyPrintAllEntries(tb, maxLen, crop, borders));
    }

    public static void prettyPrintTableToStdOut(Table tb, boolean crop){
        int[] maxLen = getColLengths(tb);
        System.out.println(prettyPrintTableHeader(tb, maxLen));
        System.out.println(prettyPrintAllEntries(tb, maxLen, crop, false)); // no border underneath each entry when printing to stdout
    }

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

    public static int[] getColLengths(Table tb){
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
            return null;
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
                String str = data[col][ind];
                maxLen[col] = maxLen[col] < (str.length() + padding) ? (str.length() + padding): maxLen[col];
            }
        }
        return maxLen;
    }

    private static String prettyPrintBorder(Table tb, int[] maxLen) {
        StringBuilder boundary = new StringBuilder();
        for(String s: tb.getSchema().keySet()) {
            // schema is sorted, so this is fine...
            boundary.append(PrettyPrinter.getBorder(maxLen[tb.getSchema().get(s)], "+", "-"));
        }
        boundary.append("+");
        return boundary.toString();
    }

    private static String prettyPrintTableHeader(Table tb, int[] maxLen){
        StringBuilder header = new StringBuilder();
        for(String s: tb.getSchema().keySet()) {
            // schema is sorted, so this is fine...
            header.append(PrettyPrinter.getBox(s, maxLen[tb.getSchema().get(s)], "|"));
        }
        header.append("|");

        String boundary = prettyPrintBorder(tb, maxLen);

        return boundary + "\n" + header.toString() + "\n" + boundary;
    }

    private static String prettyPrintAllEntries(Table tb, int[] maxLen, boolean crop, boolean borders){
        String[][] data;
        try{
            data = tb.getData();
        }catch(NullPointerException E){
            System.out.println("Table doesn't exist.");
            return null;
        }

        int col_num = data.length;
        int lines = data[0].length;
        //System.out.println("Data size: " + col_num + " x " + lines);

        String border = prettyPrintBorder(tb, maxLen);

        StringBuilder entry = new StringBuilder();
        if(!crop) {
            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < col_num; j++)
                    entry.append(getBox(data[j][i], maxLen[j], "|"));
                entry.append("|" + "\n");
                if(borders)
                    entry.append(border + "\n");
            }
        }else if(crop && lines < 20){
            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < col_num; j++)
                    entry.append(getBox(data[j][i], maxLen[j], "|"));
                entry.append("|" + "\n");
            }
            entry.append(border + "\n");
        }else{
            int limit = 20 < lines? 20: lines;
            for (int i = 0; i < limit/2; i++) {
                for (int j = 0; j < col_num; j++)
                    entry.append(getBox(data[j][i], maxLen[j], "|"));
                entry.append("|" + "\n");
            }
            entry.append(getOmission(tb, maxLen));
            entry.append("  \n");
            for (int i = lines - limit/2; i < lines; i++) {
                for (int j = 0; j < col_num; j++)
                    entry.append(getBox(data[j][i], maxLen[j], "|"));
                entry.append("|" + "\n");
            }
            entry.append(border + "\n");
        }


        // append border and line count
        entry.append("Number of entries: " + lines + "\n");

        return entry.toString();
    }

    private static String getOmission(Table tb, int[] maxLen){
        StringBuilder sb = new StringBuilder();
        for(String s: tb.getSchema().keySet()) {
            // schema is sorted, so this is fine...
            sb.append(PrettyPrinter.getBox("...", maxLen[tb.getSchema().get(s)], " "));
        }
        return "\n" + sb.toString() + "\n";
    }

}

