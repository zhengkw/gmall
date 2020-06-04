package com.zhengkw.gmall.realtime.util

import com.zhengkw.gmall.realtime.bean.AlertInfo
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{Bulk, Index}
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
 * @ClassName:ESUtil
 * @author: zhengkw
 * @description:
 * @date: 20/06/04下午 11:46
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object ESUtil {
  private val factory = new JestClientFactory
  val esurl = "http://hadoop102:9200"
  private val config: HttpClientConfig = new HttpClientConfig.Builder(esurl)
    .maxTotalConnection(100) // 允许的最多客户端的个数
    .connTimeout(10000) // 连接es的超时时间
    .readTimeout(10000) // 读取数据的超时时间
    .multiThreaded(true) //是否多线程
    .build()
  factory.setHttpClientConfig(config)

  /**
   * @descrption: 插入数据
   * @param index
   * @param source 数据
   * @param id     doc-id
   * @return: void
   * @date: 20/06/05 上午 12:03
   * @author: zhengkw
   */
  def insertSingle(index: String, source: Object, id: String = null) {
    val client: JestClient = factory.getObject
    val action = new Index.Builder(source)
      .index(index)
      .`type`("_doc")
      .id(id) // 如果是传递的null, 则相当于没有传
      .build()
    client.execute(action)
    client.shutdownClient() // 把客户端还给工厂
  }

  /**
   * @descrption: 批量插入
   * @param index
   * @param sources  采用迭代器是因为可协变
   * @return: void
   * @date: 20/06/05 上午 12:04
   * @author: zhengkw
   */
  def insertBulk(index: String, sources: Iterator[Object]) = {
    val client: JestClient = factory.getObject
    val builder = new Bulk.Builder()
      .defaultIndex(index)
      .defaultType("_doc")
    // 在一个Bulk.Builder中add进去多个Action, 可以一次性交给es完成插入
    // Object   (id, object)
    sources.foreach {
      case (id: String, data) =>
        val action = new Index.Builder(data)
          .id(id)
          .build()
        builder.addAction(action)
      case data =>
        val action = new Index.Builder(data)
          .build()
        builder.addAction(action)
    }

    client.execute(builder.build())
    client.shutdownClient()
  }
  implicit class RichES(rdd: RDD[AlertInfo]) {
    def saveToES(index: String): Unit = {
      rdd.foreachPartition((it: Iterator[AlertInfo]) => {
        // 同一设备，每分钟只记录一次预警。-> 靠es来保证. (spark-streaming不处理)
        // 如果id相同, 后面的会覆盖前面的!!!  mid_ + 分钟数
        val sources = it
          .map(info => (info.mid + "_" + info.ts / 1000 / 60, info))
        ESUtil.insertBulk(index, sources)
      })
    }
  }

  def main(args: Array[String]): Unit = {
    //main方法用于测试（非常规写法！）

    /* val source = User("fengjie", 50)
     insertSingle("user1128", source)*/

    val it = Iterator(("30", User("a", 10)), ("30", User("b", 20)))
    //        val it = Iterator(User("a", 10), User("b", 20))
    insertBulk("user1128", sources = it)
  }
}
