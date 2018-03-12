### Spark Project for EBCDIC Load

This converts the Java project for spark into `Scala`
It also adds an invocation `bash` script.

#### Assumptions:
+ HDFS is kerberized
+ HDFS configuration files are under /etc/hadoop/conf. If this is different, then modify `Util.scala`
+ Install `sbt` 

#### Packaging the jar

```sbtshell
cd cobol_spark
sbt clean compile package
```

#### Sundry Items

`HiveSchemaGenerator` is not used, however can be easily added to `CobolMultiParser` to print the column and 
corresponding data type information to a file.
To add schema generation, make following changes,

+ Modify the runner to accept the location of copybook from local file system in addition to all other options.
+ Modify ProcessArguments to add the local copybook to `CobolConverterConf`
+ add schema genreation as,
```scala
HiveSchemaGenerator.exec("local copybook file", "output file")
```