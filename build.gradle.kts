plugins {
  java
  application
}

group = "com.example"
version = "1.0"

application {
  mainClass.set("com.example.wordcount.WordCount")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.apache.hadoop:hadoop-client:3.3.6")
  implementation("org.apache.hadoop:hadoop-common:3.3.6")
  implementation("org.apache.hadoop:hadoop-hdfs:3.3.6")
  implementation("org.apache.hadoop:hadoop-mapreduce-client-core:3.3.6")
  implementation("org.apache.hadoop:hadoop-mapreduce-client-common:3.3.6")
  implementation("org.apache.hadoop:hadoop-mapreduce-client-jobclient:3.3.6")
  implementation("com.opencsv:opencsv:5.9")
}