import com.cloudera.sa.copybook.mapreduce.CopybookInputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.{SparkConf, SparkContext}

val conf = new SparkConf().setMaster("local[2]").setAppName("CobolSparker").set("fs.defaultFS", "hdfs://nameservice1")
val sc = new SparkContext(conf)

val copybookInputPath = "/user/af55267/cobol_mapred/cpy/facp"
val dataFileInputPath = "/user/af55267/cobol_mapred/facp"
val outputPath = "/user/af55267/cobol_mapred/out/facp"

val config = new Configuration
config.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"))
config.addResource(new Path("/etc/hadoop/conf/mapred-site.xml"))
config.addResource(new Path("/etc/hadoop/conf/yarn-site.xml"))
config.addResource(new Path("/etc/hadoop/conf/core-site.xml"))

CopybookInputFormat.setCopybookHdfsPath(config, copybookInputPath)

val fileRDD = sc.newAPIHadoopFile(dataFileInputPath, classOf[CopybookInputFormat], classOf[LongWritable],
  classOf[Text], config)

val delimitedRDD = fileRDD.map { a =>
  val cells = a._2.toString.split(new Character(0x01.toChar).toString)
  val strBuilder = new StringBuilder
  for (cell <- cells) {
    strBuilder.append(cell.replaceAll("[^\\x00-\\x7F]", "") + "|")
  }
  strBuilder.toString
}
delimitedRDD.saveAsTextFile(outputPath)