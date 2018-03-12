package com.anthem.cobol

import com.anthem.cobol.model.CobolConverterConf
import com.anthem.cobol.utility.{HiveSchemaGenerator, ProcessArguments}
import com.cloudera.sa.copybook.mapreduce.CopybookInputFormat
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkContext}

object CobolMultiParser extends Serializable {
  @transient lazy val LOGGER: Logger = Logger.getLogger("CobolSparker")
  private val APP_NAME: String = "CobolSparker"

  def main(args: Array[String]): Unit = {
    val cobolConf: CobolConverterConf = new ProcessArguments().exec(args)
    LOGGER.info(">>Processed all arguments successfully.")
    val conf = new SparkConf().setAppName(APP_NAME).set("fs.defaultFS", cobolConf.namenode)
    val sc = new SparkContext(conf)

    LOGGER.info(">>Setting copybook path")
    CopybookInputFormat.setCopybookHdfsPath(cobolConf.serConf.get(), cobolConf.copyBook)

    val fileRDD = sc.newAPIHadoopFile(cobolConf.ebcdicIn, classOf[CopybookInputFormat], classOf[LongWritable],
      classOf[Text], cobolConf.serConf.get())

    val delimitedRDD = fileRDD.map { a =>
      val cells = a._2.toString.split(new Character(0x01.toChar).toString)
      val strBuilder = new StringBuilder
      for (cell <- cells) {
        strBuilder.append(cell.replaceAll("[^\\x20-\\x7E]", "") + "|")
      }
      strBuilder.toString
    }
    delimitedRDD.saveAsTextFile(cobolConf.asciiOut)
  }
}