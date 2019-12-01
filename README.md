# peachyDB: A miniature relational database with order

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
    * - sort
    * - concat

#### utility
* show tables: 
    * syntax: ```showtables()``` 
* show schemas:
    * syntax: ```showschema()```

#### Total Lines of code
``` find . -name '*.java' | xargs wc -l```
```
     173 ./java/aggregation/GroupAgg.java
     118 ./java/aggregation/Moving.java
      78 ./java/aggregation/Aggregate.java
      26 ./java/util/PrettyPrinter.java
      82 ./java/util/GroupKey.java
     105 ./java/util/Utils.java
      41 ./java/util/SortGroupKeyMap.java
     132 ./java/io/IO.java
     209 ./java/io/QueryParser.java
     157 ./java/parser/Parser.java
     195 ./java/db/Table.java
     320 ./java/db/Database.java
      15 ./java/db/Variable.java
      24 ./java/Entry.java
     559 ./java/algebra/Join.java
     298 ./java/algebra/Select.java
      55 ./java/algebra/Project.java
      34 ./java/algebra/Concat.java
      12 ./java/pair/IPair.java
      42 ./java/pair/Pair.java
    2675 total
```

#### Join
Heap space.