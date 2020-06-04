package com.zhengkw.gmall.realtime.util

import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.Index

/**
 * @ClassName:ESUtil
 * @author: zhengkw
 * @description:
 * @date: 20/06/03下午 2:30
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object ESDemo {
  def main(args: Array[String]): Unit = {
    val factory = new JestClientFactory
    val serverUrl = "http://hadoop102:9200"
    val conf = new HttpClientConfig.Builder(serverUrl)
      .maxTotalConnection(100) // 允许的最多客户端的个数
      .connTimeout(10000) // 连接es的超时时间
      .readTimeout(10000) // 读取数据的超时时间
      .multiThreaded(true) //是否多线程
      .build()
    //3.配置conf
    factory.setHttpClientConfig(conf)

    //2.获得客户端对象
    val client = factory.getObject

    /*val source =
      """
        |{
        |   "name":"KJ",
        |   "age":12
        |}
        |""".stripMargin*/

    //fuc2
    val source = User("java", 13)
    //向es写数据
    val action = new Index.Builder(source)
      .index("javademo")
      .`type`("test")
      .id("2")
      .build()
    client.execute(action)

  }
}

case class User(name: String, age: Int)