# PeachyDB: a miniature relational database 

## TABLE OF CONTENTS

1. [LIST OF QUERIES](README.md#QUERIES)
2. [SETUP](README.md##SETUP)
    1. [compile and run with maven](README.md#compile-and-run-with-maven)
3. [DOCUMENTATION](README.md##DOCUMENTATION)
    1. [table naming convention](README.md#table-naming-convention)
    2. [I/O queries](README.md#I/O)
    3. [algebraic queries](README.md#algebraic)
    4. [aggregate queries](README.md#aggregate)
    5. [moving aggregate queries](README.md#moving-aggregates)
4. [FEATURES](README.md##FEATURES)
5. [STATISTICS](README.md##STATISTICS)

## QUERIES

1. [showtables](README.md#)
1. [showschema](README.md#)
1. [quit](README.md#)
1. [inputfromfile](README.md#read-from-file)
1. [outputtofile](README.md#write-table-to-file)
1. [select](README.md#select)
1. [project](README.md#project)
1. [join](README.md#join)
1. [concat](README.md#concat)
1. [sort](README.md#sort)
1. [count](README.md#count)
1. [sum](README.md#sum)
1. [avg](README.md#avg)
1. [countgroup](README.md#countgroup)
1. [sumgroup](README.md#sumgroup)
1. [avggroup](README.md#avggroup)
1. [movavg](README.md#moving-average)
1. [movsum](README.md#moving-sum)

## SETUP

### compile and run with maven

* download repo
```
$ git clone https://github.com/TakaiKinoko/PeachyDB.git
```

* compile
```
$ cd PeachyDB
$ mvn compile 
```

* build jar
```
$ mvn package
```

* run 
```
$ java -cp target/peachyDB-1.0.jar Entry
```

* exit the database

    type ```quit``` when the database is running.


## DOCUMENTATION

### table naming convention

* has to start with an alphabetic letter
* syntax using regular expression:
    ```([a-zA-Z]+(.)*)```

* derivative tables:
    
    *  __definition__: tables that are built 'on top of' another (more than one) existing table

    * to differentiate the derivative table columns from its parent(s), it's column names have the format of ```<table_name>_<column_name>```

    * queries on the derivative tables should make sure that the columns are addressed according to the rule above

### I/O

#### read from file 

* syntax: ```<table_name> := inputfromfile(<filepath>)```

* note: 
    1. a ```<filepath>``` must be assigned to a ```<table_name>```
    1. ```<table_name>``` format: ```[a-zA-Z]+\d*```
    1. ```<filepath>``` should be the relative path from __Entry.java__ to the file __without__ parenthesis
    1. reading in a new file means initializing a new table. 
    1. the order of the file is preserved when read into the underlying data-structure of each table (an array-table).
* example: ```inputfromfile(../../../input/sales1.txt)```
        
#### write table to file



### algebraic

#### select

* syntax: 
* entries selected will be deep copy from the source table

#### project

* syntax: 
* columns selected will be __shallow copy__ (pointer) of the source table 

#### join


#### groupby 

internally: use treemap
        
#### concat

#### join

#### sort 

### aggregate

#### count 

#### sum 

#### avg 

#### countgroup 

based on groupby. treemap. sorted by the first groupby condition

#### sumgroup 

based on groupby. treemap. sorted by the first groupby condition
    
#### avggroup 

based on groupby. treemap. sorted by the first groupby condition

### moving aggregates

#### moving average

#### moving sum

Based off groupby

* customized comparable class GroupKey, 
* Java TreeMap


### utility

#### quit 

* syntax:  ```quit``` or   ```Quit```

#### show tables

* syntax: ```showtables()``` 

* sample output: 
```
+-----------+----------+
| Table     | Size     |
+-----------+----------+
| R2        | 900      |
| T4        | 391      |
| Q1        | 28       |
| R3        | 1        |
| Q2        | 28       |
| R4        | 50       |
| Q3        | 1        |
| R5        | 178      |
| Q4        | 1        |
| R6        | 5        |
| Q5        | 29       |
| R         | 1000     |
| S         | 100000   |
| T         | 3642     |
| T2prime   | 391      |
| T1        | 391      |
| T2        | 391      |
| R1        | 900      |
| T3        | 391      |
+-----------+----------+
```

#### show schemas

* syntax: ```showschema()```

* sample output:  # TODO USE PICTURE

```$xslt
+-----------+-----------------------------------------------------------------------------------------------------------------------------------------------------+
| Table     | Schema                                                                                                                                              |
+-----------+-----------------------------------------------------------------------------------------------------------------------------------------------------+
| R2        | R2_saleid | R2_qty | R2_pricerange |                                                                                                                |
| T4        | R1_saleid | R1_itemid | R1_customerid | R1_storeid | R1_time | R1_qty | R1_pricerange | S_saleid | S_I | S_C | S_S | S_T | S_Q | S_P | mov_sum |    |
| Q1        | Q1_saleid | Q1_itemid | Q1_customerid | Q1_storeid | Q1_time | Q1_qty | Q1_pricerange |                                                             |
| R3        | avg_qty |                                                                                                                                           |
| Q2        | Q2_saleid | Q2_itemid | Q2_customerid | Q2_storeid | Q2_time | Q2_qty | Q2_pricerange |                                                             |
| R4        | groupby_qty | sum_time |                                                                                                                            |
| Q3        | Q3_saleid | Q3_itemid | Q3_customerid | Q3_storeid | Q3_time | Q3_qty | Q3_pricerange |                                                             |
| R5        | groupby_time | groupby_pricerange | sum_qty |                                                                                                       |
| Q4        | Q4_saleid | Q4_itemid | Q4_customerid | Q4_storeid | Q4_time | Q4_qty | Q4_pricerange |                                                             |
| R6        | groupby_pricerange | avg_qty |                                                                                                                      |
| Q5        | saleid | itemid | customerid | storeid | time | qty | pricerange |                                                                                  |
| R         | saleid | itemid | customerid | storeid | time | qty | pricerange |                                                                                  |
| S         | saleid | I | C | S | T | Q | P |                                                                                                                    |
| T         | R_saleid | R_itemid | R_customerid | R_storeid | R_time | R_qty | R_pricerange | S_saleid | S_I | S_C | S_S | S_T | S_Q | S_P |                     |
| T2prime   | R1_saleid | R1_itemid | R1_customerid | R1_storeid | R1_time | R1_qty | R1_pricerange | S_saleid | S_I | S_C | S_S | S_T | S_Q | S_P |              |
| T1        | R1_saleid | R1_itemid | R1_customerid | R1_storeid | R1_time | R1_qty | R1_pricerange | S_saleid | S_I | S_C | S_S | S_T | S_Q | S_P |              |
| T2        | R1_saleid | R1_itemid | R1_customerid | R1_storeid | R1_time | R1_qty | R1_pricerange | S_saleid | S_I | S_C | S_S | S_T | S_Q | S_P |              |
| R1        | R1_saleid | R1_itemid | R1_customerid | R1_storeid | R1_time | R1_qty | R1_pricerange |                                                             |
| T3        | R1_saleid | R1_itemid | R1_customerid | R1_storeid | R1_time | R1_qty | R1_pricerange | S_saleid | S_I | S_C | S_S | S_T | S_Q | S_P | mov_sum |    |
+-----------+-----------------------------------------------------------------------------------------------------------------------------------------------------+
```
## FEATURES

#### In Order

#### Pretty-Printer
    
## STATISTICS
* line counts using: ```$ find . -name '*.java' | xargs wc -l```
```
     196 ./src/main/java/aggregation/GroupAgg.java
     139 ./src/main/java/aggregation/Moving.java
      79 ./src/main/java/aggregation/Aggregate.java
      90 ./src/main/java/util/Sort.java
      96 ./src/main/java/util/PrettyPrinter.java
     114 ./src/main/java/util/GroupKey.java
     130 ./src/main/java/util/Utils.java
      41 ./src/main/java/util/SortGroupKeyMap.java
     181 ./src/main/java/io/IO.java
     260 ./src/main/java/io/QueryParser.java
     150 ./src/main/java/parser/Parser.java
      18 ./src/main/java/btree/BTKeyValue.java
    1020 ./src/main/java/btree/BTree.java
      11 ./src/main/java/btree/BTIteratorIF.java
      26 ./src/main/java/btree/BTException.java
      61 ./src/main/java/btree/BTNode.java
      42 ./src/main/java/btree/SimpleFileWriter.java
      82 ./src/main/java/db/DynamicTable.java
     225 ./src/main/java/db/Table.java
     401 ./src/main/java/db/Database.java
      15 ./src/main/java/db/Variable.java
      39 ./src/main/java/index/BTTestIteratorImpl.java
      86 ./src/main/java/index/Btree.java
      66 ./src/main/java/index/Hash.java
      58 ./src/main/java/index/BtreeKey.java
      28 ./src/main/java/Entry.java
     288 ./src/main/java/algebra/Join.java
     444 ./src/main/java/algebra/Select.java
      91 ./src/main/java/algebra/CartesianArray.java
     416 ./src/main/java/algebra/JoinOld.java
      55 ./src/main/java/algebra/Project.java
      34 ./src/main/java/algebra/Concat.java
     173 ./src/main/java/algebra/Cartesian.java
      12 ./src/main/java/pair/IPair.java
      44 ./src/main/java/pair/Pair.java
    5211 total
```

#### Join
Heap space.