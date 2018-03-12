package com.anthem.cobol.utility

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

object Util {

  def getHadoopConf: ConfigSerDeser = {
    val config = new Configuration
    config.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"))
    config.addResource(new Path("/etc/hadoop/conf/mapred-site.xml"))
    config.addResource(new Path("/etc/hadoop/conf/yarn-site.xml"))
    config.addResource(new Path("/etc/hadoop/conf/core-site.xml"))
    new ConfigSerDeser(config)
  }
}
