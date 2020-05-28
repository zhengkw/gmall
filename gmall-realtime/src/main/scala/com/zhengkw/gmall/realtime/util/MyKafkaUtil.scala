package com.zhengkw.gmall.realtime.util

import com.zhengkw.common.util.PorpertiesUtil
import com.zhengkw.scala.util.PropertyUtil
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka.KafkaUtils


/**
 * @ClassName:MyKafkaUtil
 * @author: zhengkw
 * @description:
 * @date: 20/05/28下午 8:53
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object MyKafkaUtil {

  var param = Map[String, String](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> PropertyUtil.getProperty("config.properties", "kafka.servers"),
    ConsumerConfig.GROUP_ID_CONFIG -> PorpertiesUtil.getProperty("kafka.group.id", "config.properties")
    /*    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> PropertyUtil.getProperty("config.properties", "kafka.servers"),
        ConsumerConfig.GROUP_ID_CONFIG -> PropertyUtil.getProperty("config.properties", "kafka.group.id")*/
  )

  /**
   * @descrption:
   * @param ssc   KafkaUtils.createDirectStream必要参数
   * @param topic 消费的主题
   * @return: org.apache.spark.streaming.dstream.DStream<java.lang.String>
   * @date: 20/05/28 下午 10:00
   * @author: zhengkw
   */
  def getKafkaStream(ssc: StreamingContext, topic: String) = {
    val ds = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc,
      param,
      Set(topic))
      .map(_._2)
    ds
  }
}
