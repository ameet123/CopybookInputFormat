package com.anthem.cobol.utility

import com.anthem.cobol.CobolMultiParser
import com.anthem.cobol.model.CobolConverterConf
import org.apache.log4j.Logger

class ProcessArguments extends Serializable {
  @transient lazy val LOGGER: Logger = Logger.getLogger(getClass.getName)
  private val TOTAL_ARGS: Int = 4

  def exec(args: Array[String]): CobolConverterConf = {
    if (args.length != TOTAL_ARGS) {
      LOGGER.error("ERR: passed:" + args.length + " Required:" + TOTAL_ARGS)
      LOGGER.error(s"Usage : spark-submit --class  ${CobolMultiParser.getClass.getName} <copyBookFile> <ebcdic data " +
        s"HDFS dir> <ascii output HDFS dir>")
      System.exit(1)
    }
    val copyBook: String = args(0)
    val namenode: String = args(1)
    val ebcdicInput: String = args(2)
    val asciiOut: String = args(3)
    CobolConverterConf(copyBook, namenode, ebcdicInput, asciiOut, Util.getHadoopConf)
  }
}
