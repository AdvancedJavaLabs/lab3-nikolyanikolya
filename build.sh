#!/usr/bin/env bash

hadoop fs -rm -r -f /user/nikolay/output
hadoop fs -rm -r -f /user/nikolay/intermediate

echo "HADOOP_CLASSPATH = $HADOOP_CLASSPATH"
javac -cp ${HADOOP_CLASSPATH} -d . src/main/java/lab3/WordCount.java src/main/java/lab3/Statistic.java
jar -cvf WordCount.jar lab3/WordCount*.class lab3/Statistic.class
hadoop jar WordCount.jar lab3.WordCount /user/nikolay/input /user/nikolay/intermediate /user/nikolay/output
