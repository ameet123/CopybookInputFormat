#!/usr/bin/env bash

# For SSL DEBUG add -Djavax.net.debug=all to driver and executor java options.
SPARK_SUBMIT=/usr/bin/spark2-submit
APP_JAR=../target/scala-2.10/cobol_spark_2.10-0.1.jar
JARS=../lib/copybookInputFormat.jar
VM_OPTIONS="-Dlog4j.properties=./log4j.properties"
UPLOAD="./cb2xml.properties#cb2xml.properties,./lexer.dat#lexer.dat"

usage(){
  echo -e "Usage: $0 \n\t-s <mode:local/yarn>\n\t-c <copybook file path in HDFS>\n\t-n <namenode>\n\t-i <ebcdic dir in HDFS>\n\t-o <ascii out dir in HDFS>\n\t-d <whether to purge output dir>\n\t-k <keytab>\n\t-p <principal>"
  exit 2
}
null_check(){
  arg=$1
  if [ "$1" == "" ]
  then
    echo "Invalid option"
    usage
  fi
}
keytab_check(){
  file=$1
  if [ ! -s $file ]
  then
    echo "Invalid keytab:$file"
    exit 4
  fi
}

if [ "$#" -lt 6 ]
then
  usage
fi
while getopts ":s:c:n:i:o:k:p:d" x; do
    case "${x}" in
        s)
            SPARK_SVR=${OPTARG}
            (("$SPARK_SVR" == "local" || "$SPARK_SVR" == "yarn")) || usage
            ;;
        c)
            COPYBOOK=${OPTARG}
            null_check ${COPYBOOK}
            ;;
        n)
            NAMENODE=${OPTARG}
            null_check ${NAMENODE}
            ;;
        i)
            EBCDIC_DIR=${OPTARG}
            null_check $EBCDIC_DIR
            ;;
        o)
            ASCII_OUT=${OPTARG}
            null_check ${ASCII_OUT}
            ;;
        k)
            KEYTAB=${OPTARG}
            keytab_check ${KEYTAB}
            ;;
        p)
            PRINCIPAL=${OPTARG}
            null_check PRINCIPAL
            ;;
        d)
            PURGE="Y"
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

echo -e "Running EBCDIC Conversion:\n{\n\tMode=${SPARK_SVR}\n\tPrincipal=${PRINCIPAL}\n\tKeytab=$KEYTAB\n\tCopybook=${COPYBOOK}\n\tIN=${EBCDIC_DIR}\n\tOUT=${ASCII_OUT}\n}"

if [ "$PURGE" == "Y" ]
then
  echo "Deleting ascii output dir: $ASCII_OUT"
  hdfs dfs -rm -r $ASCII_OUT
fi

if [ "${SPARK_SVR}" == "local" ]
then
    echo ">> Running in LOCAL Mode..."

    $SPARK_SUBMIT \
    --master ${SPARK_SVR} \
    --keytab ${KEYTAB} \
    --principal ${PRINCIPAL} \
    --conf "spark.driver.port=39200" \
    --executor-cores 4 --num-executors 4 \
    --num-executors 2 \
    --driver-memory 1g --executor-memory 1g \
    --jars ${JARS} \
    --driver-java-options "${VM_OPTIONS}" \
    --conf "spark.executor.extraJavaOptions=${VM_OPTIONS}" \
    --conf spark.network.timeout=10000000 \
    --conf spark.ui.port=24100 \
    --class com.anthem.cobol.CobolMultiParser \
    ${APP_JAR} ${COPYBOOK} ${NAMENODE} ${EBCDIC_DIR} ${ASCII_OUT}
else
    echo ">> Running in Yarn Mode..."

    $SPARK_SUBMIT \
    --master ${SPARK_SVR} \
    --keytab ${KEYTAB} \
    --files ${UPLOAD} \
    --principal ${PRINCIPAL} \
    --conf "spark.driver.port=39200" \
    --executor-cores 4 --num-executors 4 \
    --num-executors 2 \
    --driver-memory 1g --executor-memory 1g \
    --jars ${JARS} \
    --driver-java-options "${VM_OPTIONS}" \
    --conf "spark.executor.extraJavaOptions=${VM_OPTIONS}" \
    --conf spark.network.timeout=10000000 \
    --conf spark.ui.port=24100 \
    --class com.anthem.cobol.CobolMultiParser \
    ${APP_JAR} ${COPYBOOK} ${NAMENODE} ${EBCDIC_DIR} ${ASCII_OUT}
fi