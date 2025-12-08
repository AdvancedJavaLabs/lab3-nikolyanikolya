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
  val hadoop_version = "3.4.2"

  implementation("org.apache.hadoop:hadoop-common:${hadoop_version}")
  implementation("org.apache.hadoop:hadoop-mapreduce-client-core:${hadoop_version}")
  implementation("org.apache.hadoop:hadoop-hdfs:${hadoop_version}")
  implementation("org.apache.hadoop:hadoop-mapreduce-client-common:${hadoop_version}")
  implementation("org.apache.hadoop:hadoop-mapreduce-client-jobclient:${hadoop_version}")
}

tasks.jar {
  archiveFileName.set("app.jar")
  manifest {
    attributes["Main-Class"] = "WordCount"
  }
}