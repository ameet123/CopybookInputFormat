package com.anthem.cobol.model

import com.anthem.cobol.utility.ConfigSerDeser

case class CobolConverterConf(copyBook: String, namenode: String, ebcdicIn: String, asciiOut: String,
                              serConf: ConfigSerDeser)
