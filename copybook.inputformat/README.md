#### Upgrade to newer JRecord

This update aims to upgrade JRecord and cb2xml to newer versions.
This was necessary due to following issue,

```bash
18/03/08 11:16:40 WARN scheduler.TaskSetManager: Lost task 446.0 in stage 0.0 (TID 513, dwbdtest1r1w4.wellpoint.com, executor 15): java.lang.RuntimeException: java.lang.NullPointerException
        at com.cloudera.sa.copybook.mapreduce.CopybookRecordReader.initialize(CopybookRecordReader.java:88)
        at org.apache.spark.rdd.NewHadoopRDD$$anon$1.liftedTree1$1(NewHadoopRDD.scala:182)
        at org.apache.spark.rdd.NewHadoopRDD$$anon$1.<init>(NewHadoopRDD.scala:179)
        at org.apache.spark.rdd.NewHadoopRDD.compute(NewHadoopRDD.scala:134)
        at org.apache.spark.rdd.NewHadoopRDD.compute(NewHadoopRDD.scala:69)
        at org.apache.spark.rdd.RDD.computeOrReadCheckpoint(RDD.scala:323)
        at org.apache.spark.rdd.RDD.iterator(RDD.scala:287)
        at org.apache.spark.rdd.MapPartitionsRDD.compute(MapPartitionsRDD.scala:38)
        at org.apache.spark.rdd.RDD.computeOrReadCheckpoint(RDD.scala:323)
        at org.apache.spark.rdd.RDD.iterator(RDD.scala:287)
        at org.apache.spark.rdd.MapPartitionsRDD.compute(MapPartitionsRDD.scala:38)
        at org.apache.spark.rdd.RDD.computeOrReadCheckpoint(RDD.scala:323)
        at org.apache.spark.rdd.RDD.iterator(RDD.scala:287)
        at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:87)
        at org.apache.spark.scheduler.Task.run(Task.scala:108)
        at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:338)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.NullPointerException
        at net.sf.JRecord.External.CobolCopybookLoader.loadCopyBook(CobolCopybookLoader.java:142)
        at com.cloudera.sa.copybook.mapreduce.CopybookRecordReader.initialize(CopybookRecordReader.java:56)
        ... 18 more
```

#### Versions
`JRecord` => 0.81.4

`cb2xml`  => 0.95.

This is consistent with the JRecord which bundles the above cb2xml version with it.

#### Setup,

As mentioned in the outer README, the jar files for JRecord and cb2xml need to be added manually to the `
.m2/repository` on user's machine.

#### Compilation

```bash
mvn clean package
```

#### Change

the main change is in `CopybookRecordReader.java` to line,
```java
ret = LineIOProvider.getInstance().getLineReader(
          fileStructure,
          LineIOProvider.getInstance().getLineProvider(fileStructure));
```
This changes to following due to changes in the API for newer JRecord.
```java
 ret = LineIOProvider.getInstance().getLineReader(fileStructure);
```