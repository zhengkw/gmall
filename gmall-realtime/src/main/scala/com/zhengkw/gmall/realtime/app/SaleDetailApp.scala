package com.zhengkw.gmall.realtime.app

import java.util.Properties

import com.alibaba.fastjson.JSON
import com.zhengkw.common.Constant
import com.zhengkw.gmall.realtime.bean.{OrderDetail, OrderInfo, SaleDetail, UserInfo}
import com.zhengkw.gmall.realtime.util.{MyKafkaUtil, MyRedisUtil}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import redis.clients.jedis.Jedis

/**
 * @ClassName:SaleDetailApp
 * @author: zhengkw
 * @description:
 * `init_data`( do_date_string VARCHAR(20) ,
 * order_incr_num INT,user_incr_num INT   ,
 * if_truncate BOOLEAN  )
 *
 * call init_data('2020-06-01',2,2,FALSE);
 *
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
   * @descrption: 将orderInfo数据缓存到redis中
   * @param orderInfo
   * @param client
   * @return: void
   * @date: 20/06/06 上午 9:35
   * @author: zhengkw
   */
  def cacheOrderInfo(orderInfo: OrderInfo, client: Jedis) = {
    saveToRedis("order_info:" + orderInfo.id, orderInfo, client, 30 * 60)
  }

  /**
   * @descrption:
   * @param key
   * @param value
   * @param client
   * @param timeout
   * @return: java.lang.String
   * @date: 20/06/07 下午 12:15
   * @author: zhengkw
   */
  def saveToRedis(key: String, value: AnyRef, client: Jedis, timeout: Int) = {
    val content = Serialization.write(value)(DefaultFormats)
    client.setex(key, timeout, content)
  }

  /**
   * 缓存orderDetail
   *
   * @param orderDetail
   * @param client
   * @return
   */
  def cacheOrderDetail(orderDetail: OrderDetail, client: Jedis) = {
    saveToRedis("order_detail:" + orderDetail.order_id + ":" + orderDetail.id, orderDetail, client, 30 * 60)
  }

  /**
   * @descrption: 全外连接
   *              join的本质是将2个表变成一个宽表，需要有连接的字段！
   *              es中没有提供多表连接。而spark中提供了此功能！
   * @param orderInfoStream
   * @param orderDetailStream
   * @return: join后的流
   * @date: 20/06/06 上午 1:18
   * @author: zhengkw
   */
  def fullJoin(orderInfoStream: DStream[OrderInfo], orderDetailStream: DStream[OrderDetail]) = {
    //如果需要对流进行join，那么流的结构必须是KV的！
    // 提供的API里面是通过K来进行连接的！
    val orderIdAndOrderInfo: DStream[(String, OrderInfo)] =
    orderInfoStream.map(info => (info.id, info))
    val orderIdAndOrderDetail: DStream[(String, OrderDetail)] =
      orderDetailStream.map(info => (info.order_id, info))
    //将双流进行join操作
    orderIdAndOrderInfo.fullOuterJoin(orderIdAndOrderDetail)
      .mapPartitions((it: Iterator[(String, (Option[OrderInfo], Option[OrderDetail]))]) =>
        //每个分区里创建客户端
      {
        val client = MyRedisUtil.getClient
        //如果用map则，Iterator[List[SaleDetail]] it里面不是直接存放saledetail，所以直接展开！
        val result: Iterator[SaleDetail] = it.flatMap({
          //同批次获得
          case (orderId, (Some(orderInfo), Some(orderDetail))) =>
            // 1. 把order_info信息写入到缓存(因为order_detail信息有部分信息可能迟到)
            cacheOrderInfo(orderInfo, client)
            //2.将信息join到一个样例类中！这里缺少user信息！
            val saleDetail = SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
            //3.去order_detail中查询缓存,先获取detail所以的缓存key！
            import scala.collection.JavaConversions._
            //java集合转scala集合方便后续操作
            val keys = client.keys("order_detail:" + orderInfo.id + ":*").toList
            //遍历key获取value
            val saleDetails: List[SaleDetail] = keys.map(key => {
              val orderDetail: OrderDetail = JSON.parseObject(client.get(key), classOf[OrderDetail])
              // 删除对应的key, 如果不删, 有可能造成数据重复(因为detail属于一对多中的多表，所以join完以后必须删除！)
              client.del(key)
              SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
            })
            saleDetail :: saleDetails //将同批次join的结果和不同批次join结果共同返回！
          //info延后到达
          case (orderId, (None, Some(orderDetail)))
          =>
            // 1. 去order_info的缓存中查找
            val orderInfoJson = client.get("order_info:" + orderDetail.order_id)
            if (orderInfoJson == null) {
              // 3. 如果不存在, 则order_detail缓存
              cacheOrderDetail(orderDetail, client)
              Nil
            } else {
              // 2. 如果存在, 则join
              val orderInfo = JSON.parseObject(orderInfoJson, classOf[OrderInfo])
              SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail) :: Nil
            }

          //detail延后到达
          case (orderId, (Some(orderInfo), None)) =>

            cacheOrderInfo(orderInfo, client)
            import scala.collection.JavaConversions._
            val keys: List[String] = client.keys("order_detail:" + orderInfo.id + ":*").toList // 转成scala集合
            val saleDetails: List[SaleDetail] = keys.map(key => {
              val orderDetail: OrderDetail = JSON.parseObject(client.get(key), classOf[OrderDetail])
              client.del(key)
              SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
            })
            saleDetails
        })
        client.close()
        result

      })
  }

  /**
   * @descrption: 将user信息添加到saleDetail中
   * @param saleDetailStream
   * @param sc
   * @return: void
   * @date: 20/06/14 下午 7:50
   * @author: zhengkw
   */
  def joinUser(saleDetailStream: DStream[SaleDetail], sc: SparkContext) = {
    //获取sparkSession对象！
    val spark: SparkSession = SparkSession.builder().config(sc.getConf).getOrCreate()
    // 导入隐式转换
    import spark.implicits._
    val props = new Properties()
    props.setProperty("user", "root")
    props.setProperty("password", "sa")
    val url = "jdbc:mysql://hadoop102:3306/gmall1128"
    // 利用sparkSql将user数据合并到saleDetail中
    saleDetailStream.transform(rdd =>

      /**
       * 读取jdbc的2种方式并join TODO ??????
       *  1.在driver端直接使用rdd进行join
       *  2.在rdd的每个分区中完成join
       */ {

      val userDS = spark.read
        .jdbc(url, "user_info", props)
        .as[UserInfo]
        //将数据转换成kv形式，为了方便join
        .map(info => (info.id, info))
      //将ds转换成rdd·
      val userInfoRDD = userDS.rdd

      //将saleDetail中的rdd进行map
      val saleDetailRDD = rdd.map(detail => (detail.user_id, detail))

      //将两个rdd进行join
      saleDetailRDD.join(userInfoRDD)
        .map {
          case (_, (saleDetail, userInfo)) =>
            saleDetail.mergeUserInfo(userInfo)
        }
      //返回map后的rdd
    }
    )

  }


  def main(args: Array[String]): Unit = {
    // 1. 读数据
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SaleDetailApp")
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(3))
    //获取双流数据
    val (orderInfoStream, orderDetailStream) = getOrderInfoAndOrderDetailStreams(ssc)
    val saleDetailStream = fullJoin(orderInfoStream, orderDetailStream)
    //将用户数据添加到流中
    val resultStream = joinUser(saleDetailStream, ssc.sparkContext)
    //测试 打印最终的流
    resultStream.print
    ssc.start()
    ssc.awaitTermination()
  }
}
