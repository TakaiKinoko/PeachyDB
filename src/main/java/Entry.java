import db.*;
import io.*;
import util.*;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
public class Entry {

    public static void main(String[] args) throws Exception{

        //================================================ Experiment with primitive type
        long start, end;

        for(int i = 0; i < 5; i++) {
            start = System.nanoTime();
            int[] pair = new int[2];
            pair[0] = 1;
            pair[1] = 2;
            end = System.nanoTime();
            System.out.println(end - start);
        }
        System.out.println("\n");
        //================================================ Experiment with customized object type
        for(int i = 0; i < 5; i++) {
            start = System.nanoTime();
            Pair<Integer> pair = new Pair<>(1, 2);
            end = System.nanoTime();
            System.out.println(end - start);
        }
        System.out.println();

        //================================================ Possibility of counting lines in a file before reading === so to assign array as underlying data type
        for(int i = 0; i < 5; i++){
            start = System.currentTimeMillis();
            BufferedReader reader = new BufferedReader(new FileReader("input/sales2.txt"));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();
            end = System.currentTimeMillis();
            System.out.println(end - start);
        }




        // create a new database
        Database db = new Database();

        // set up scanner, parser and IO for parsing commandline input
        IO io = new IO(db);
        QueryParser parser = new QueryParser(io);
        Scanner s = new Scanner(System.in);
        PrettyPrinter.printWelcome();

        /**
         * EXPERIMENT RESULT
         * using ArrayList<ArrayList> costs wayyyy less memory & a bit less time than allocating a 2D array that's too big
         * printing a large table can prove costly too.
         * I think I should use ArrayList<ArrayList> to implement the table, and only print out the first and last few lines
         * at the end together with the line count.
         *
         * */
/*
        io.inputfromfile("R := inputfromfile(input/sales1.txt)");
        io.inputfromfile("S := inputfromfile(input/sales2.txt)");

        Table small = db.getTable("R");
        Table big = db.getTable("S");
        start = System.currentTimeMillis();

        String[][] sData = small.getData();
        String[][] bData = big.getData();

        DynamicTable dt;
        if(db.newDynamicTable("result"))
            dt = db.getDynamicTable("result");
        else return;

        //ArrayList<String[]> data = new ArrayList<>();
        //ArrayList<ArrayList> data = new ArrayList<>();
        //String[][] data = new String[14][small.getTableSize() * big.getTableSize()];
        int cnt = 0;
        for(int i = 0; i < small.getTableSize(); i++){
            for(int j = 0; j < big.getTableSize(); j++){
                if(sData[6][i].equals(bData[6][j]) && Integer.valueOf(sData[5][i]) >= Integer.valueOf(bData[5][j])){
                    //System.out.println((cnt) + " " + sData[1][i] + " " + sData[1][i] + " " + sData[2][i] + " " + bData[1][j]);
                    ArrayList l = new ArrayList();
                    /*
                    String[] l = new String[14];
                    for(int n = 0; n < 7; n++){
                        l[n] = sData[n][i];
                        l[n+7] = bData[n][j];
                    }*/
                /*    for(int n = 0; n < 7; n++){
                        l.add(sData[n][i]);
                    }
                    for(int n = 0; n < 7; n++){
                        l.add(bData[n][j]);
                    }

                    dt.insertData(l);
                    //data.add(l);
                    //System.out.println(l);

                    /*
                    for(int n = 0; n < 7; n++){
                        data[n][cnt] = sData[n][i];
                        data[n+7][cnt] = bData[n][j];
                    }*/
                  //cnt++;
              //  }
           // }
       // }

        /*
        for(int i = 0; i < 10; i++){
            System.out.println(dt.getData().get(i).toString());
        }
        System.out.println("...");
        */
        /*
        dt.printTable();

        end = System.currentTimeMillis();
        System.out.println("data nominal size: " + small.getTableSize() * big.getTableSize() +
                "\ndata actual size: " + dt.getTableSize() + "\ndouble loop costs: " + (end - start)/1000 + " s.");
*/

        while(s.hasNext()) {
            String q = s.nextLine();
            parser.decodeQuery(q);
        }

    }

    // TODO delete the test class
    public static class Pair<V> {
        V val1;
        V val2;

        public Pair(V ind1, V ind2){
            this.val1 = ind1;
            this.val2 = ind2;
        }
    }
}
