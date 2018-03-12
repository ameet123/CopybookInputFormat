package com.anthem.cobol.utility

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

class ConfigSerDeser(var conf: Configuration) extends Serializable {

  def this() {
    this(new Configuration())
  }

  def get(): Configuration = conf

  private def writeObject(out: java.io.ObjectOutputStream): Unit = {
    conf.write(out)
  }

  private def readObject(in: java.io.ObjectInputStream): Unit = {
    conf = new Configuration()
    conf.readFields(in)
  }

  private def readObjectNoData(): Unit = {
    conf = new Configuration()
  }
}