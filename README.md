# peachyDB: A miniature relational database with order

## Compile and Run with Maven

#### download repo
```$ git clone https://github.com/TakaiKinoko/PeachyDB.git```

#### compile
```
$ cd PeachyDB
$ mvn compile 
```

#### build jar
```$ mvn package```

#### run 
```$ java -cp target/peachyDB-1.0.jar Entry```

## Features:

### In Order

### Pretty-Printer

## Specifications:

#### Table naming convention: 

* has to start with an alphabetic letter
* regular expression:
    ```([a-zA-Z]+(.)*)```
#### Derivative tables
* __definition__: tables that are built as a subset of another existing table
* Internally, this table share the same schema of the parent table. But when it is printed out, it's column 
names have the format of ```<table_name>_<column_name>```

## Queries Supported: 

#### I/O
##### read from file 
    * syntax: ```<table_name> := inputfromfile(<filepath>)```
    * note: 
        1. a ```<filepath>``` must be assigned to a ```<table_name>```
        1. ```<table_name>``` format: ```[a-zA-Z]+\d*```
        1. ```<filepath>``` should be the relative path from __Entry.java__ to the file __without__ parenthesis
        1. reading in a new file means initializing a new table. 
        1. the order of the file is preserved when read into the underlying data-structure of each table (an array-table).
    * example: ```inputfromfile(../../../input/sales1.txt)```
        
##### outputtofile
    
#### algebra
##### ```select```
    * syntax: 
    * entries selected will be deep copy from the source table
##### ```project```
    * syntax: 
    * columns selected will be __shallow copy__ (pointer) of the source table 
##### ```join```
##### ```groupby``` 
        internally: use treemap
        
##### ```concat```

##### ```join```

#### aggregate

##### ```count```
##### ```sum```
##### ```avg```
##### ```countgroup```
        based on groupby. treemap. sorted by the first groupby condition

##### ```sumgroup```
    based on groupby. treemap. sorted by the first groupby condition
    
##### ```avggroup```
    based on groupby. treemap. sorted by the first groupby condition

#### moving aggregates
    * - movavg
    * - movsum

#### others

##### sort 

Based off groupby

* customized comparable class GroupKey, 
* Java TreeMap

##### concat

#### utility
* show tables: 
    * syntax: ```showtables()``` 
* show schemas:
    * syntax: ```showschema()```

#### Total Lines of code
``` find . -name '*.java' | xargs wc -l```
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