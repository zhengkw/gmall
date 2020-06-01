package com.zhengkw.gmall.realtime.app

import com.zhengkw.common.Constant
import com.zhengkw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @ClassName:OrderApp
 * @author: zhengkw
 * @description:
 * @date: 20/06/01上午 8:59
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object OrderApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("OrderApp ")
    val ssc = new StreamingContext(conf, Seconds(3))
   //获取kafka数据
   val kafkaStream = MyKafkaUtil.getKafkaStream(ssc, Constant.ORDER_INFO_TOPIC)
    kafkaStream.print(100)
    ssc.start()
    ssc.awaitTermination()
  }
}
