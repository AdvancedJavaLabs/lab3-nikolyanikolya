# Установка

```bash
curl -O https://downloads.apache.org/hadoop/common/hadoop-3.4.0/hadoop-3.4.0.tar.gz
```

# Настройка для запуска hadoop

```bash
sudo systemsetup -setremotelogin on
```

Проверить, что можно залогиниться по ssh без пароля

```bash
ssh localhost
```
Если требует пароль, то сгенерить ssh ключ. Важно!!! Выберите такое название для файла с ssh ключом, чтобы не перезаписать существующие

```bash
ssh-keygen -t rsa -N "" -f ~/.ssh/<ssh_key_file>
cat ~/.ssh/<ssh_key_file>.pub >> ~/.ssh/authorized_keys
```

## Проинициализировать `~/.zshrc` следующим образом
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export HADOOP_HOME="$HOME/hadoop/hadoop-3.4.0"
export HADOOP_JAR=$HADOOP_HOME/share/hadoop/common/hadoop-common-3.4.0.jar
export HADOOP_INSTALL="$HADOOP_HOME"
export HADOOP_MAPRED_HOME="$HADOOP_HOME"
export HADOOP_COMMON_HOME="$HADOOP_HOME"
export HADOOP_HDFS_HOME="$HADOOP_HOME"
export HADOOP_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export YARN_HOME="$HADOOP_HOME"
export HADOOP_COMMON_LIB_NATIVE_DIR="$HADOOP_HOME/lib/native"
export PATH="$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH"
export PATH="$HADOOP_JAR:$PATH"
```

где `$HOME` - место, куда вы установили hadoop

Далее 
```bash 
source ~/.zshrc
```

Далее конфигурируем hadoop xml (${HADOOP_HOME}/etc/hadoop)

## mapred-site.xml

```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>

    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>localhost:10020</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>localhost:19888</value>
    </property>

    <property>
        <name>yarn.app.mapreduce.am.env</name>
        <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
    </property>
    <property>
        <name>mapreduce.map.env</name>
        <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
    </property>
    <property>
        <name>mapreduce.reduce.env</name>
        <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
    </property>
</configuration>
```

## hdfs-site.xml
```xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>file:///Users/nikolay/hadoopdata/hdfs/namenode</value>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>file:///Users/nikolay/hadoopdata/hdfs/datanode</value>
    </property>
</configuration>
```

## core-site.xml

```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
</configuration>
```

## yarn-site.xml

```xml
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <property>
        <name>yarn.nodemanager.env-whitelist</name>
        <value>
            JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME
        </value>
    </property>
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>localhost</value>
    </property>
    <property>
        <name>yarn.resourcemanager.address</name>
        <value>localhost:8032</value>
    </property>
    <property>
        <name>yarn.nodemanager.admin-env</name>
        <value>JAVA_HOME=${JAVA_HOME}</value>
    </property>
</configuration>
```

## yarn-env.sh

```bash
export JAVA_HOME=${JAVA_HOME}
export PATH=$JAVA_HOME/bin:$PATH
```

## hadoop-env.sh

```bash
export JAVA_HOME=${JAVA_HOME}
```

## Запуск Hadoop

```bash
hadoop namenode -format
start-all.sh
```

После этой команды по адресу localhost:9870 должна быть доступна вьюшка hdfs

# Запуск java приложения
```bash
javac -cp ${HADOOP_CLASSPATH} -d . src/main/java/lab3/WordCount.java
jar -cvf WordCount.jar lab3/WordCount*.class
hadoop jar WordCount.jar lab3.WordCount /user/nikolay/input /user/nikolay/output
```