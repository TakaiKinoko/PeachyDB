#!/usr/bin/env bash

echo --------------------------- compiling ---------------------------------------------
mvn compile
mvn package

echo -----------------------------------------------------------------------------------
echo ---------------------- feeding queries to the database ----------------------------
echo ------------- output directed to output/fh643_AllOperations -----------------------
echo -----------------------------------------------------------------------------------

java -cp target/peachyDB-1.0.jar Entry  < input/input_pipe > output/fh643_AllOperations




