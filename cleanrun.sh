#! /usr/bin/bash
rm RM.*
javac -cp .:jars/* Spider.java
java -cp .:jars/* Spider "http://www.cse.ust/hk" "1"

