To build spider in linux:
javac -cp .:jars/* Spider.java

To run spider with set start link and max number of indexed pages:
java -cp .:jars/* Spider "START LINK HERE" "NUM PAGES HERE"
So for described config: java -cp .:jars/* Spider "http://www.cse.ust.hk" "30"


To build test program:
javac -cp .:jars/* Test.java

To run test program:
java -cp .:jars/* Test