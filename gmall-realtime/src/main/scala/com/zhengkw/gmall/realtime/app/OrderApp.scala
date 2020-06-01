package com.zhengkw.gmall.realtime.app

import com.alibaba.fastjson.JSON
import com.zhengkw.common.Constant
import com.zhengkw.gmall.realtime.bean.OrderInfo
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
  val tableName = "GMALL_ORDER_INFO1128"
  val colSeq = Seq("ID", "PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT", "CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY", "USER_ID", "IMG_URL", "TOTAL_AMOUNT", "EXPIRE_TIME", "DELIVERY_ADDRESS", "CREATE_TIME", "OPERATE_TIME", "TRACKING_NO", "PARENT_ORDER_ID", "OUT_TRADE_NO", "TRADE_BODY", "CREATE_DATE", "CREATE_HOUR")
  val zkUrl = Some("hadoop102,hadoop103,hadoop104:2181")

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("OrderApp ")
    val ssc = new StreamingContext(conf, Seconds(3))
    //获取kafka数据
    val sourceStream = MyKafkaUtil.getKafkaStream(ssc, Constant.ORDER_INFO_TOPIC)
    //    sourceStream.print(100)
    // json的两种动作: 解析:  json字符串=>java对象   序列化: java对象  =>json字符串
    val orderInfoStream = sourceStream.map(s => JSON.parseObject(s, classOf[OrderInfo]))
    import org.apache.phoenix.spark._
    orderInfoStream.foreachRDD(rdd => {
      /*
   1. 如果是使用分布式数据集的保存方法, 就不用考虑分区
   2. 如果是自己单独去连接外部存储, 则需要按分区来写.
   saveToPhoenix 里面有mapPatitions
    */

      rdd.saveToPhoenix(tableName, colSeq, zkUrl = zkUrl)
    })
    //测试是否拿到kafka数据
    orderInfoStream.print

    ssc.start()
    ssc.awaitTermination()
  }
}
