package com.anthem.cobol.utility

import java.io.{BufferedWriter, File, FileWriter}

import net.sf.JRecord.External.{CobolCopybookLoader, CopybookLoader, ExternalRecord}

object HiveSchemaGenerator {
  private val SPLIT_NONE: Int = 0

  def exec(copyBook: String, outputFile: String): Unit = {
    val copybookInt: CopybookLoader = new CobolCopybookLoader
    val externalRecord: ExternalRecord = copybookInt.loadCopyBook(copyBook, SPLIT_NONE, 0, "cp037", 0, 0, null)
    val writer: BufferedWriter = new BufferedWriter(new FileWriter(new File(outputFile)))
    var isFirst = true
    var lastColumn = ""
    var repeatCounter = 1
    var recordLength = 0
    val hiveType = "STRING"

    for (field <- externalRecord.getRecordFields) {
      if (isFirst) isFirst = false
      else {
        writer.append(",")
        writer.newLine()
      }
      recordLength += field.getLen
      var columnName = field.getCobolName
      columnName = columnName.replace('-', '_')
      if (lastColumn == columnName) columnName = columnName + {
        repeatCounter += 1
        repeatCounter - 1
      }
      else {
        repeatCounter = 1
        lastColumn = columnName
      }
      writer.append("  " + columnName + " " + hiveType)
    }
    writer.close()
  }
}