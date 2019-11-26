## HOMEWORK 3 -- Question 1

### Instructions For Graders

* I'm using __Java__.

* My code has been tested with success on __crunchy5.cims.nyu.edu__.

* B-trees and Hash database are two separate programs.

* __Setup__: please put the newline-delimited file containing testing operations as __commands.txt__ under __/input__ folder.

* __Compile__: 
    
    * B-trees: ```javac index.Btree.java```
    * Hash: ```javac index.Hash.java```

* __Run__: 

    * B-trees: 
        ```java index.Btree  "../../../input/myindex"  "../../../input/commands.txt"```
    * Hash: 
        ```java index.Hash  "../../../input/myindex"  "../../../input/commands.txt"```

* __Output__: 
    Stored under the __/output__ folder as four files: 
    * ```btree_timing.txt``` -- timings (individual and total) for btree experiment
    * ```hash_timing.txt``` -- timings (individual and total) for hash experiment
    * ```btree_resultTable``` -- query output and __final table__ for btree experiment
    * ```hash_resultTable``` -- query output and __final table__ for hash experiment


### Folder Structure

* **/input** input path for data file and command file
 
* **/output** output path for timing experiments

* **/src/main/java** 
    contains: 
    * B-trees implementation, under __/btree__
    * Hash implementation, under __/hash__
    * Utility functions, under __/util__
        * credit to https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/
    * Helper class, that enables the storage of <Key, Value> pairs in a Java array, under __/pair__


### Data Structure Implementation

##### BTree source:

* https://www.codeproject.com/Articles/1158559/B-Tree-Another-Implementation-By-Java

##### Hash source:

* https://github.com/phishman3579/java-algorithms-implementation/blob/master/src/com/jwetherell/algorithms/data_structures/HashMap.java


### Compilation

##### index.Btree class
```javac index.Btree.java```


##### index.Hash class
```javac index.Hash.java```


### Execution 

##### index.Btree class
```java index.Btree  "../../../input/myindex"  "../../../input/commands.txt"```


##### index.Hash class
```java index.Hash  "../../../input/myindex"  "../../../input/commands.txt"```



### Technical Specifications

##### Data & Index Storage

* Data is stored as a dynamically sized array (arraylist) of __<Key, Value>__ pairs, where __<Index>__ is treated as the natural order of the array.

* Hash and B-tree structures accept a __<Key>__ and produce an __<Index>__ into the data array.


##### Insert

* Implemented as __upsert__, meaning that if there's already a record with key value k, then that record's value is updated to v.

* __syntax__: ```insert(<key>, <value>)```

* __output__: 
    * When inserting a record whose key DOES NOT already exist, the database output: 
        ```
        VALUE of KEY:<key> updated from <old_value> to <new_value>
        Current number of items in <B-tree/HashMap> DB is: <number of entries>
        ```
    
    * When inserting a record whose key already exist, the database output: 
        ```
        KEY:<key> VALUE:<value> inserted.
        Current number of items in <B-tree/HashMap> DB is: <number of entries>
        ```

##### Delete

* __syntax__: ```delete(<key>)```

* When a key is deleted, it is deleted from the B-tree/hash structure. But in order to __avoid shifting__ the 
underlying data array, the Pair associated with the key stays in the array while its value is marked as __null__.

* __output__: 
    * When deleting a record that exists in the database: 
        ```
         KEY: <key> deleted!
         Current number of items in <B-tree/HashMap> DB is: <number of entries>
        ```
    * When deleting a record that DOES NOT exists in the database: 
        ``` 
        The KEY:<key> doesn't exist in the database.
        ```
        
##### Search

* __syntax__: ```search(<key>)```

* __output__: 
    * When searching for a key that exists in the database: 
        ```
         VALUE of the given KEY:<key> is: <value>
        ```
    * When deleting a record that DOES NOT exists in the database: 
        ``` 
        The KEY:<key> doesn't exist in the database.
        ```