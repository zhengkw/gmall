package com.zhengkw.canal

import java.net.InetSocketAddress
import java.util

import com.alibaba.fastjson.JSONObject
import com.alibaba.otter.canal.client.CanalConnectors
import com.alibaba.otter.canal.protocol.CanalEntry.{EventType, RowChange}
import com.alibaba.otter.canal.protocol.{CanalEntry, Message}
import com.zhengkw.common.Constant
import com.zhengkw.util.MyKafkaUtil

/**
 * @ClassName:CanalClient
 * @author: zhengkw
 * @description: canal客户端操作
 *               mysql端测试 存储过程调用 造数据
 *               # 日期  订单个数 用户数 是否删除以前的数据
 *               call init_data("2019-05-16", 10,2,false)
 * @date: 20/05/31下午 1:25
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object CanalClient {
  /**
   * @descrption: 解析数据
   * @param rowDataList 数据集 一个rowData就是一行数据
   * @param tableName   表名
   * @param eventType   事件类型 insert delete update EventType.INSERT枚举类
   * @return: voidt
   * @date: 20/05/31 下午 5:59
   * @author: zhengkw
   */
  def parseData(rowDataList: util.List[CanalEntry.RowData],
                tableName: String,
                eventType: CanalEntry.EventType) = {
    // 计算订单总额 order_info
    if (tableName == "order_info" && eventType == EventType.INSERT && rowDataList != null && rowDataList.size() > 0) {
      import scala.collection.JavaConversions._
      for (rowData <- rowDataList) {
        val result = new JSONObject()
        // 一个rowData表示一行数据, 所有列组成一个json对象, 写入到Kafka中
        val columnList: util.List[CanalEntry.Column] = rowData.getAfterColumnsList
        for (column <- columnList) { // column 列
          val key: String = column.getName // 列名
          val value = column.getValue // 列值
          result.put(key, value)
       //   println(result)
        }
        // 把数据写入到kafka中. 用一个生产者
        MyKafkaUtil.send(Constant.ORDER_INFO_TOPIC, result.toJSONString)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    // 1. 连接到canal服务器
    val connector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop102", 11111), "example", "", "")
    //连接到canal
    connector.connect()
    // 2. 订阅你要处理的具体表 gmall1128下所有的表
    connector.subscribe("gmall1128.*")
    // 3. 读取数据, 解析
    // 一直监听mysql数据变化，所以死循环
    while (true) {
      // 100表示最多一次拉取由于100条sql导致的数据的变化
      val message: Message = connector.get(100)
      //获取到entries
      val entries = message.getEntries
      //判断entries集合里有数据！
      if (entries != null && !entries.isEmpty) {
        //导入隐式转换 将java集合转换为scala集合方便操作！
        import scala.collection.JavaConversions._
        //遍历entries
        for (entry <- entries) {
          //针对每一个entry进行操作 获取storevalue
          val storeValue = entry.getStoreValue
          // 每个storeVales一个RowChange
          val rowChange: RowChange = RowChange.parseFrom(storeValue)
          // 每个rowChange中多个RowData. 一个RowData就表示一行数据
          val rowDataList: util.List[CanalEntry.RowData] = rowChange.getRowDatasList
          parseData(rowDataList, entry.getHeader.getTableName, rowChange.getEventType)
        }
      }
      else {
        println("没有拉倒数据, 2s之后继续拉....")
        Thread.sleep(2000)
      }
    }
  }
}
