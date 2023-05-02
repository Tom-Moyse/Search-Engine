#! /usr/bin/bash
rm -f RM.*
rm -f searchengine/files/spider_result.txt
find . -name "*.bak" -type f -delete
javac -cp .:jars/* searchengine/Spider.java
javac -cp .:jars/* searchengine/Search.java
