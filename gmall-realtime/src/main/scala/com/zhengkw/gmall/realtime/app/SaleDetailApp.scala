package com.zhengkw.gmall.realtime.app

import java.util.Properties

import com.alibaba.fastjson.JSON
import com.zhengkw.common.Constant
import com.zhengkw.gmall.realtime.bean.{OrderDetail, OrderInfo}
import com.zhengkw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @ClassName:SaleDetailApp
 * @author: zhengkw
 * @description:
 * @date: 20/06/06上午 12:55
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object SaleDetailApp {
  //mysql配置信息
  val url = "jdbc:mysql://hadoop102:3306/gmall1128"
  val props: Properties = new Properties()
  props.setProperty("user", "root")
  props.setProperty("password", "sa")

  /**
   * @descrption: 通过工具类获取流数据
   *              需要将获取的数据进行封装，kafka里写入的数据
   *              为json字符串
   * @param ssc
   * @return: scala.Tuple2<org.apache.spark.streaming.dstream.DStream<java.lang.String>,org.apache.spark.streaming.dstream.DStream<java.lang.String>>
   * @date: 20/06/06 上午 1:04
   * @author: zhengkw
   */
  def getOrderInfoAndOrderDetailStreams(ssc: StreamingContext) = {
    //从kafka消费mysql通过canal写入的数据
    val orderInfo = MyKafkaUtil.getKafkaStream(ssc, Constant.ORDER_INFO_TOPIC)
      .map(json => JSON.parseObject(json, classOf[OrderInfo]))
    val orderDetail = MyKafkaUtil.getKafkaStream(ssc, Constant.ORDER_DETAIL_TOPIC)
      .map(json => JSON.parseObject(json, classOf[OrderDetail]))
    (orderInfo, orderDetail)
  }
/**
* @descrption: 全外连接
 * @param orderInfoStream
 * @param orderDetailStream
* @return: join后的流
* @date: 20/06/06 上午 1:18
* @author: zhengkw
*/
  def fullJoin(orderInfoStream: DStream[OrderInfo], orderDetailStream: DStream[OrderDetail]) = {

  }

  def main(args: Array[String]): Unit = {
    // 1. 读数据
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SaleDetailApp")
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(3))
    //获取双流数据
    val (orderInfoStream, orderDetailStream) = getOrderInfoAndOrderDetailStreams(ssc)
    fullJoin(orderInfoStream, orderDetailStream)
    ssc.start()
    ssc.awaitTermination()
  }
}
