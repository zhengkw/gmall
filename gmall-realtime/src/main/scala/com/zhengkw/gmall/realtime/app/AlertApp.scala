package com.zhengkw.gmall.realtime.app

import java.util

import com.alibaba.fastjson.JSON
import com.zhengkw.common.Constant
import com.zhengkw.gmall.realtime.bean.{AlertInfo, EventLog}
import com.zhengkw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

/**
 * @ClassName:AlertApp
 * @author: zhengkw
 * @description:
 * 需求：
 * 同一设备，5分钟内三次及以上用不同账号登录并领取优惠劵, 每6s统计一次，
 * 并且在登录到领劵过程中没有浏览商品。同时达到以上要求则产生一条预警日志。
 * 同一设备，每分钟只记录一次预警。
 *
 * -----
 * 同一设备  -> 按照mid分组
 * 5分钟内 每6s统计一次  -> window  窗口的长度: 5分钟, 窗口的步长: 6s
 * 三次及以上用不同账号登录 -> 统计登录的账号的数
 * 并领取优惠劵 -> 把其他的行为过滤掉, 浏览商品的行为得留下来
 *
 * 同时达到以上要求则产生一条预警日志 -> 日志信息写到es中
 * 同一设备，每分钟只记录一次预警。-> 靠es来保证. (spark-streaming不处理)
 * @date: 20/06/02上午 1:01
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object AlertApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("AlertApp")
    val ssc = new StreamingContext(conf, Seconds(3))
    // 1. 消费事件日志
    val sourceStream = MyKafkaUtil
      .getKafkaStream(ssc, Constant.EVENT_TOPIC)
      // 1.1给数据加窗口
      .window(Minutes(5), Seconds(6))

    //2.封装数据
    val eventLogStream = sourceStream.map(log =>
      //将kafka读到的json字符串转换成样例类对象
      JSON.parseObject(log, classOf[EventLog]))
    /*
    * 同一设备  -> 按照mid分组
  * 5分钟内 每6s统计一次  -> window  窗口的长度: 5分钟, 窗口的步长: 6s
  * 三次及以上用不同账号登录 -> 统计登录的账号的数
  * 并领取优惠劵 -> 把其他的行为过滤掉, 浏览商品的行为得留下来
  *
  * 同时达到以上要求则产生一条预警日志 -> 日志信息写到es中
  * 同一设备，每分钟只记录一次预警。-> 靠es来保证. (spark-streaming不处理)
    * */
    //3.分析数据
    //3.1 分组
    val eventLogGroupedStream = eventLogStream
      .map(eventLog => (eventLog.mid, eventLog))
      .groupByKey()
    //产生预警信息
    val alertInfoStream = eventLogGroupedStream.map({
      case (mid, logIt) => {
        // 保存5分钟内登陆的领取优惠券所有 不同用户 (建立向es写数据, 用的是java客户端, 不支持sclaa的集合, 所以, 使用java的Set)
        val uidSet = new util.HashSet[String]()
        // 存储5分钟内所有的事件类型
        val eventList = new util.ArrayList[String]()
        // 存储领取优惠券的那些商品id(同uid一样用set去重)
        val itemSet = new util.HashSet[String]()
        // 是否浏览过商品. 默认没有
        for (event <- logIt) {
          //遍历到一个event就添加一次到list中！
          eventList.add(event.eventId)
          //定义浏览标记 默认未点击
          var isClickItem = false
          //模式匹配event判断内容中是否含有 coupon关键字
          import scala.util.control.Breaks._
          breakable {
            event.eventId match {
              case "coupon" =>
                //记录领取的用户
                uidSet.add(event.uid)
                // 优惠券对应的商品id
                itemSet.add(event.itemId)
              case "clickItem" =>
                isClickItem = true
                //跳出循环分析下一个mid
                break
              //如果用户有浏览商品行为，则将标记转为true 定义标记默认false
              case _ => //其他事件不处理
            }
          }
          // 返回预警信息.
          // (是否产生预警信息(boolean),   预警信息的封装 ) 元组
          (!isClickItem && uidSet.size() >= 3, AlertInfo(mid, uidSet, itemSet, eventList))

        }
      }
    })
    //测试是否有数据
    // sourceStream.print(1000)
    //eventLogStream.print(1000)
    alertInfoStream.print(1000)

    ssc.start()
    ssc.awaitTermination()

  }
}
