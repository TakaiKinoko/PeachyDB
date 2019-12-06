package io;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import algebra.*;
import aggregation.*;
import index.Btree;
import index.Hash;
import util.PrettyPrinter;
import util.Sort;

public class QueryParser {
    /**
     * This class parses queries, calls the queried functions which operate on the associated table(s)
     * COMMANDS SUPPORTED:
     * ===============
     * I/O
     * ===============
     * - inputfromfile
     * - outputtofile
     * ===============
     * algebra
     * ===============
     * - select
     * - project
     * - join
     * - groupby
     * ===============
     * Aggregate
     * ===============
     * - count
     * - sum
     * - avg
     * - countgroup
     * - sumgroup
     * - avggroup
     * ===============
     * moving aggregates
     * ===============
     * - movavg
     * - movsum
     * ===============
     * others
     * ===============
     * - sort
     * - concat
     */
    private Matcher[] matchers;
    private Pattern[] patterns;
    private int command_num;
    private IO io;

    private long START;

    public QueryParser(IO io){
        /**
         * @param io: the io (subsequently database) to direct queries to.
         * */
        this.io = io;
        patterns = new Pattern[24];
        // I/O
        int i = 0;
        patterns[i++] = Pattern.compile("inputfromfile"); // 0
        patterns[i++] = Pattern.compile("outputtofile"); // 1
        // DATA
        patterns[i++] = Pattern.compile("search");   // 2
        patterns[i++] = Pattern.compile("insert");   // 3
        patterns[i++] = Pattern.compile("delete");   // 4
        // ALGEBRA
        patterns[i++] = Pattern.compile("select");   // 5
        patterns[i++] = Pattern.compile("project");  // 6
        patterns[i++] = Pattern.compile("join");     // 7
        patterns[i++] = Pattern.compile("groupby");  // 8
        // AGGREGATE
        patterns[i++] = Pattern.compile("count(\\(|\\s)");    // 9
        patterns[i++] = Pattern.compile("(=|\\s)sum(\\(|\\s)");      // 10
        patterns[i++] = Pattern.compile("(=|\\s)avg(\\(|\\s)");      // 11
        patterns[i++] = Pattern.compile("countgroup");  // 12
        patterns[i++] = Pattern.compile("sumgroup");    // 13
        patterns[i++] = Pattern.compile("avggroup");    // 14
        // MOVING AGGREGATE
        patterns[i++] = Pattern.compile("movsum");      // 15
        patterns[i++] = Pattern.compile("movavg");      // 16
        // OTHER
        patterns[i++] = Pattern.compile("sort");        // 17
        patterns[i++] = Pattern.compile("concat");      // 18
        patterns[i++] = Pattern.compile("showtables");  // 19
        patterns[i++] = Pattern.compile("showschema");  // 20
        // INDEX
        patterns[i++] = Pattern.compile("^(\\s*)([H|h]ash)");  // 21
        patterns[i++] = Pattern.compile("^(\\s*)([B|b]tree)");  // 22
        patterns[i++] = Pattern.compile("^(\\s*)([Q|q]uit)"); //23

        command_num = i;  // number of commands the program takes

        matchers = new Matcher[i];  // matchers correspond to each of the patterns
    }

    public void decodeQuery(String s) throws IOException  {
        /**
         * @param s: query string to be decoded
         * will direct the string consequently to the right query function
         * */
        // set up matchers
        for(int i = 0; i < command_num; i++)
            matchers[i] = patterns[i].matcher(s);

        System.out.println("\n" + s);
        if(matchers[0].find()){                     // inputfromfile
            startTimer();
            io.inputfromfile(s);
            endTimer();
        }else if(matchers[1].find()){               // outputtofile
            startTimer();
            io.outputtofile(s);
            endTimer();
        }else if(matchers[2].find()){               // search (not implemented)
        }else if(matchers[3].find()){               // insert (not implemented)
        }else if(matchers[4].find()){               // delete (not implemented)
        }else if(matchers[5].find()){               // select
            startTimer();
            Select selector = new Select(io.db);
            selector.select(s);
            endTimer();
        }else if(matchers[6].find()){               // project
            startTimer();
            Project projector = new Project(io.db);
            projector.project(s);
            endTimer();
        }else if(matchers[7].find()){               // join
            startTimer();
            Join joiner = new Join(io.db, s);
            joiner.join();
            endTimer();
        }else if(matchers[8].find()){               // groupby
        }else if(matchers[9].find()){               // count
            startTimer();
            Aggregate agg = new Aggregate(io.db);
            agg.count(s);
            endTimer();
        }else if(matchers[10].find()){              // sum
            startTimer();
            Aggregate agg = new Aggregate(io.db);
            agg.sum(s);
            endTimer();
        }else if(matchers[11].find()){              // avg
            startTimer();
            Aggregate agg = new Aggregate(io.db);
            agg.avg(s);
            endTimer();
        }else if(matchers[12].find()){              // countgroup
            startTimer();
            GroupAgg ga = new GroupAgg(io.db);
            ga.countgroup(s);
            endTimer();
        }else if(matchers[13].find()){              // sumgroup
            startTimer();
            GroupAgg ga = new GroupAgg(io.db);
            ga.sumgroup(s);
            endTimer();
        }else if(matchers[14].find()){              // avggroup
            startTimer();
            GroupAgg ga = new GroupAgg(io.db);
            ga.avggroup(s);
            endTimer();
        }else if(matchers[15].find()){              // movsum
            startTimer();
            Moving ma = new Moving(io.db);
            ma.movsum(s);
            endTimer();
        }else if(matchers[16].find()){              // movavg
            startTimer();
            Moving ma = new Moving(io.db);
            ma.movavg(s);
            endTimer();
        }else if(matchers[17].find()){              // sort
            startTimer();
            Sort S = new Sort(io.db);
            S.sort(s);
            endTimer();
        }else if(matchers[18].find()) {             // concat
            startTimer();
            Concat.concat(io.db, s);
            endTimer();
        }else if(matchers[19].find()) {             // showtables
            io.db.showtables();
        }else if(matchers[20].find()) {             //showshema
            io.db.showSchema();
        }else if(matchers[21].find()) {             // hash
            Hash ha = new Hash(io.db, s);
        }else if(matchers[22].find()) {             // btree
            Btree bt = new Btree(io.db, s);
        }else if(matchers[23].find()){              //quit
            PrettyPrinter.printGoodbye();
            // Terminate JVM
            System.exit(0);

        }else{                                      // error
            System.out.println("There's syntax error in the query.");
        }
    }

    private void startTimer(){
        /**
         * record when the queried function is being called
         * */
        this.START = System.currentTimeMillis();
    }

    private void endTimer(){
        /**
         * record when the queried function is finished and print out its time cost to stdout
         * */
        System.out.printf("Time cost: %.4f seconds\n\n", ((double)System.currentTimeMillis() - (double)START)/1000);
    }
}
