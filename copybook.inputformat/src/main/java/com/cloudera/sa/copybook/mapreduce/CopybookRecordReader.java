package com.cloudera.sa.copybook.mapreduce;

import com.cloudera.sa.copybook.Const;
import net.sf.JRecord.Common.Constants;
import net.sf.JRecord.Details.AbstractLine;
import net.sf.JRecord.External.CobolCopybookLoader;
import net.sf.JRecord.External.CopybookLoader;
import net.sf.JRecord.External.Def.ExternalField;
import net.sf.JRecord.External.ExternalRecord;
import net.sf.JRecord.IO.AbstractLineReader;
import net.sf.JRecord.IO.LineIOProvider;
import net.sf.JRecord.Numeric.Convert;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.BufferedInputStream;
import java.io.IOException;

public class CopybookRecordReader extends RecordReader<LongWritable, Text> {

    private int recordByteLength;
    private long start;
    private long pos;
    private long end;

    private LongWritable key = null;
    private Text value = null;

    private AbstractLineReader ret;
    private ExternalRecord externalRecord;
    private static String fieldDelimiter = Character.toString((char) 0x01);

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
            throws IOException {

        String cblPath = context.getConfiguration().get(
                Const.COPYBOOK_INPUTFORMAT_CBL_HDFS_PATH_CONF);

        FileSystem fs = FileSystem.get(context.getConfiguration());

        BufferedInputStream inputStream = new BufferedInputStream(fs.open(new Path(
                cblPath)));

        CobolCopybookLoader copybookInt = new CobolCopybookLoader();
        try {
            externalRecord = copybookInt
                    .loadCopyBook(inputStream, "RR", CopybookLoader.SPLIT_NONE, 0,
                            "cp037", Convert.FMT_MAINFRAME, 0, null);

            int fileStructure = Constants.IO_FIXED_LENGTH;

            for (ExternalField field : externalRecord.getRecordFields()) {
                recordByteLength += field.getLen();
            }

            // jump to the point in the split that the first whole record of split
            // starts at
            FileSplit fileSplit = (FileSplit) split;

            start = fileSplit.getStart();
            end = start + fileSplit.getLength();

            BufferedInputStream fileIn = new BufferedInputStream(fs.open(fileSplit.getPath()));

            if (start != 0) {
                pos = start - (start % recordByteLength) + recordByteLength;

                fileIn.skip(pos);
            }

            // ameet: 3/12/18 This signature has changed to not provide LineIOProvider
            ret = LineIOProvider.getInstance().getLineReader(fileStructure);

            ret.open(fileIn, externalRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        if (pos > end) {
            return false;
        }

        if (key == null) {
            key = new LongWritable();
        }
        if (value == null) {
            value = new Text();
        }

        AbstractLine line = ret.read();

        if (line == null) {
            return false;
        }

        pos += recordByteLength;

        key.set(pos);

        StringBuilder strBuilder = new StringBuilder();

        boolean isFirst = true;
        int i = 0;
        for (ExternalField ignored : externalRecord.getRecordFields()) {
            if (isFirst) {
                isFirst = false;
            } else {
                strBuilder.append(fieldDelimiter);
            }
            strBuilder.append(line.getFieldValue(0, i++));
        }

        value.set(strBuilder.toString());
        key.set(pos);

        return true;
    }

    @Override
    public LongWritable getCurrentKey() {
        return key;
    }

    @Override
    public Text getCurrentValue() {

        return value;
    }

    @Override
    public float getProgress() {
        if (start == end) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (pos - start) / (float) (end - start));
        }
    }

    @Override
    public void close() throws IOException {
        ret.close();
    }

}
