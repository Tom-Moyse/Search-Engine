#! /usr/bin/bash
rm -f RM.*
rm -f spider_result.txt
find . -name "*.class" -type f -delete
javac -cp .:jars/* searchengine/Spider.java
javac -cp .:jars/* searchengine/Search.java
