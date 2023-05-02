#! /usr/bin/bash
rm -f searchengine/files/RM.*
rm -f searchengine/files/spider_result.txt
rm -f searchengine/*.class
javac -cp .:jars/* searchengine/Spider.java
javac -cp .:jars/* searchengine/tests/TestSearch.java
