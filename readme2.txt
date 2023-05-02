In /mnt/c/Users/Tom/Documents/Uni/comp4321/project folder
./cleandir.sh
java -cp .:jars/* searchengine/Spider "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm" "300"

In ~/apache-tomcat/apache-tomcat-9.0.74 folder
cp -r /mnt/c/Users/Tom/Documents/Uni/comp4321/project/searchengine/ webapps/searchengine/WEB-INF/classes/
cp /mnt/c/Users/Tom/Documents/Uni/comp4321/project/searchpage.jsp webapps/searchengine/
cp /mnt/c/Users/Tom/Documents/Uni/comp4321/project/RM.* webapps/searchengine/
bin/shutdown.sh
bin/startup.sh