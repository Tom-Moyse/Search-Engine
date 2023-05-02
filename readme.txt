To build spider in linux:
javac -cp .:jars/* Spider.java

To run spider with set start link and max number of indexed pages:
java -cp .:jars/* Spider "START LINK HERE" "NUM PAGES HERE"

So for described config: 
java -cp .:jars/* searchengine/Spider "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm" "50"

To continue from a previous crawl simply run above without start link:  
java -cp .:jars/* Spider "400"


To build test program 1:
javac -cp .:jars/* Test.java

To run test program 1:
java -cp .:jars/* Test

To build test program 2:
javac -cp .:jars/* searchengine/tests/TestSearch.java

To run test program 2:
java -cp .:jars/* searchengine/tests/TestSearch