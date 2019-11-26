package io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import algebra.*;

public class QueryParser {

    /** COMMANDS SUPPORTED:
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
     * aggregate
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
    /*
    Pattern input_p, output_p;
    Pattern insert_p, deletion_p, search_p;
    Pattern select_p, project_p, join_p, groupby_p;
    Pattern count_p, sum_p, avg_p, countgroup_p, sumgroup_p, avggroup_p;
    Pattern movavg_p, movsum_p;
    Pattern sort_p, concat_p;
    */
    Matcher[] matchers;
    Pattern[] patterns;
    int command_num;
    IO io; //

    public QueryParser(IO io){
        /**
        // I/O
        input_p = Pattern.compile("^inputfromfile"); // 0
        output_p = Pattern.compile("^outputtofile"); // 1
        // DATA
        search_p = Pattern.compile("^search");  // 2
        insert_p = Pattern.compile("^insert");  // 3
        deletion_p = Pattern.compile("^delete");// 4
        // ALGEBRA
        select_p = Pattern.compile("^select");  // 5
        project_p = Pattern.compile("^project");// 6
        join_p = Pattern.compile("^join");      // 7
        groupby_p = Pattern.compile("^groupby");// 8
        // AGGREGATE
        count_p = Pattern.compile("^count");    // 9
        sum_p = Pattern.compile("^sum");        // 10
        avg_p = Pattern.compile("^avg");        // 11
        countgroup_p = Pattern.compile("^countgroup"); // 12
        sumgroup_p = Pattern.compile("^sumgroup");     // 13
        avggroup_p = Pattern.compile("^avggroup");     // 14
        // MOVING AGGREGATE
        movsum_p = Pattern.compile("^movsum");        // 15
        movavg_p = Pattern.compile("^movavg");         // 16
        // OTHER
        sort_p = Pattern.compile("^sort");             // 17
        concat_p = Pattern.compile("^concat");         // 18
        */
        this.io = io;
        patterns = new Pattern[20];
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
        patterns[i++] = Pattern.compile("count");    // 9
        patterns[i++] = Pattern.compile("sum");      // 10
        patterns[i++] = Pattern.compile("avg");      // 11
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

        command_num = i;  // number of commands the program takes

        matchers = new Matcher[i];  // matchers correspond to each of the patterns
    }

    public void decodeQuery(String s) {
        /**
         * @param s: query string to be decoded
         * will direct the string consequently to the right query function
         * */
        // set up matchers
        for(int i = 0; i < command_num; i++)
            matchers[i] = patterns[i].matcher(s);

        if(matchers[0].find()){
            //TODO delete test print
            System.out.println("Command: inputfromfile");
            io.inputfromfile(s);
        }else if(matchers[1].find()){
            // outputtofile
        }else if(matchers[2].find()){
            // search
        }else if(matchers[3].find()){
            // insert
        }else if(matchers[4].find()){
            // delete
        }else if(matchers[5].find()){
            Select selector = new Select(io.db);
            selector.select(s);
            // select
        }else if(matchers[6].find()){
            // project
        }else if(matchers[7].find()){
            // join
        }else if(matchers[8].find()){
            // groupby
        }else if(matchers[9].find()){
            // count
        }else if(matchers[10].find()){
            // sum
        }else if(matchers[11].find()){
            // avg
        }else if(matchers[12].find()){
            // countgroup
        }else if(matchers[13].find()){
            // sumgroup
        }else if(matchers[14].find()){
            // avggroup
        }else if(matchers[15].find()){
            // movsum
        }else if(matchers[16].find()){
            // movavg
        }else if(matchers[17].find()){
            // sort
        }else if(matchers[18].find()) {
            // concat
        }else if(matchers[19].find()) {
            // showtables
            io.db.showtables();
        }else{
            // error
            System.out.println("There's syntax error in the query.");
        }
    }
}
