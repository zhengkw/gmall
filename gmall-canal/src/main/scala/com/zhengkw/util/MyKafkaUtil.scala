package com.zhengkw.util

import java.util.Properties

import com.zhengkw.common.util.PorpertiesUtil
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
 * @ClassName:MykafkaUtil
 * @author: zhengkw
 * @description: kafka工具类
 * @date: 20/05/31下午 1:25
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object MyKafkaUtil {
  private val props = new Properties()
  //读取配置文件内容并设置属性值！
  props.setProperty("bootstrap.servers", PorpertiesUtil.getProperty("bootstrap.servers", "kafka.properties"))
  props.setProperty("key.serializer", PorpertiesUtil.getProperty("key.serializer", "kafka.properties"))
  props.setProperty("value.serializer", PorpertiesUtil.getProperty("value.serializer", "kafka.properties"))

  private val producer = new KafkaProducer[String, String](props)

  def send(topic: String, content: String) = {
    producer.send(new ProducerRecord[String, String](topic, content))
  }
}
