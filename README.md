# peachyDB: A miniature relational database with order

## Features:

### In Order

### Pretty-Printer

## Queries Supported: 

#### I/O
* read from file 
    * syntax: ```<table_name> := inputfromfile(<filepath>)```
    * note: 
        1. a ```<filepath>``` must be assigned to a ```<table_name>```
        1. ```<table_name>``` format: ```[a-zA-Z]+\d*```
        1. ```<filepath>``` should be the relative path from __Entry.java__ to the file __without__ parenthesis
        1. reading in a new file means initializing a new table. 
        1. the order of the file is preserved when read into the underlying data-structure of each table (an array-table).
    * example: ```inputfromfile(../../../input/sales1.txt)```
        
* outputtofile
    
#### algebra
    * - select
    * - project
    * - join
    * - groupby

#### aggregate
    * - count
    * - sum
    * - avg
    * - countgroup
    * - sumgroup
    * - avggroup

#### moving aggregates
    * - movavg
    * - movsum

#### others
    * - sort
    * - concat

#### utility
* show tables: 
    * syntax: ```showTables()``` 
