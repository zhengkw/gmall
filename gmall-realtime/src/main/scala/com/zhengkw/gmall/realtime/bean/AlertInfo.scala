package com.zhengkw.gmall.realtime.bean

/**
 * @ClassName:AlertInfo
 * @author: zhengkw
 * @description:
 * @date: 20/06/02上午 9:39
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
case class AlertInfo(mid: String,
                     uids: java.util.HashSet[String],
                     itemIds: java.util.HashSet[String],
                     events: java.util.ArrayList[String])