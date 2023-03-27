#! /usr/bin/bash
rm -f RM.*
rm -f spider_result.txt
rm -f *.class
javac -cp .:jars/* Spider.java
javac -cp .:jars/* Test.java
