package com.zhengkw.gmall.realtime.app

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.JSON
import com.zhengkw.common.Constant
import com.zhengkw.gmall.realtime.bean.StartupLog
import com.zhengkw.gmall.realtime.util.{MyKafkaUtil, MyRedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

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
    //一批3秒
    val ssc = new StreamingContext(conf, Seconds(3))
    //调用util里的kafka工具消费数据
    val ds = MyKafkaUtil.getKafkaStream(ssc, Constant.STARTUP_TOPIC)
    //控制台上打印
    //ds.print(1000)

    //将数据封装到样例类
    val startupLogStream =
      ds.map(log =>
        JSON.parseObject(log, classOf[StartupLog]))
    //从redis读到所有今天启动过的设备(读redis的数据)
    val startupLog = startupLogStream.transform(
      // 对rdd进行整体去重. 不能按照分区进行去重
      // 连接redis, 读取数据. 其实是在driver中,获客户端, 然后获取所有已经启动的设备
      rdd => {
        //获取redis客户端
        val client = MyRedisUtil.getClient
        //获取存在redis中的mids数据
        val mids = client.smembers(Constant.STARTUP_TOPIC + ":" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        //归还redis客户端到连接池
        client.close()
        //把已经启动的过滤掉. 只留下来没有启动过的
        // 提高集合使用效率, 把集合做广播变量
        val midsBC = ssc.sparkContext.broadcast(mids)
        //过滤
        /* rdd.filter(!_.mid.contains(midsBC.value))
           .map(log => (log.mid, log))
           .groupByKey()
           //.mapValues(_.toList.sortBy(_.ts))
           .map({
             case (mid, it) => it.toList.sortBy(_.ts).head
           })*/
        rdd
          .filter(startupLog => !midsBC.value.contains(startupLog.mid))
          // 原因是因为, 如果一个mid第一批次启动的时候, 有多次启动行为的过滤
          .map(log => (log.mid, log))
          .groupByKey
          .map {
            //                    case (mid, logIt) =>  logIt.toList.sortBy(_.ts).head
            case (mid, logIt) => logIt.toList.minBy(_.ts) // 排序取最小
          }
      })
    // 把新启动的设备id写入到redis
    startupLog.foreachRDD(rdd => {
      val client = MyRedisUtil.getClient
      // 写法2: 每个分区向外写
      rdd.foreachPartition(startuplog => {
        startuplog.foreach(log =>
          client.sadd(Constant.STARTUP_TOPIC + ":" + log.logDate, log.mid))

      })
    })
    /* startupLog.foreachRDD(rdd => {
       // 写法1: 把rdd中, 所有的mid拉取到驱动端, 一次性写入
       // 写法2: 每个分区向外写
       rdd.foreachPartition(startupLogs => {
         val client: Jedis = MyRedisUtil.getClient

         startupLogs.foreach(log => {
           client.sadd(Constant.STARTUP_TOPIC + ":" + log.logDate, log.mid)
         })
         client.close()
       })
     })*/
    ssc.start()
    ssc.awaitTermination()
  }
}
