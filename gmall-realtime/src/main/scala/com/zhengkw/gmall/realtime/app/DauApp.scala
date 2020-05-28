package com.zhengkw.gmall.realtime.app

import com.zhengkw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @ClassName:DauApp
 * @author: zhengkw
 * @description: sparkstreaming消费kafka数据
 * @date: 20/05/28下午 8:53
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object DauApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("DauApp")
    val ssc = new StreamingContext(conf, Seconds(3))
    //调用util里的kafka工具消费数据
 
    ssc.start()
    ssc.awaitTermination()
  }
}
