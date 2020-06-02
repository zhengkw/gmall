package com.zhengkw.gmall.realtime.bean

import java.text.SimpleDateFormat
import java.util.Date

/**
 * @ClassName:EventLog
 * @author: zhengkw
 * @description:
 *              未封装打印出来的DS数据
 *   {"logType":"event",
 *   "area":"beijing",
 *   "uid":"4020",
 *   "eventId":"coupon","itemId":47,
 *   "os":"android","nextPageId":30,"appId":"gmall",
 *   "mid":"mid_33","pageId":13,"ts":1591032087144}
 *
 *   封装后数据
 *   EventLog(mid_78,4717,gmall,
 *   xinjiang,android,event,coupon,25,10,41,
 *   1591033040292,2020-06-02,01)
 * @date: 20/06/02上午 1:30
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
case class EventLog(mid: String,
                    uid: String,
                    appId: String,
                    area: String,
                    os: String,
                    logType: String,
                    eventId: String,
                    pageId: String,
                    nextPageId: String,
                    itemId: String,
                    ts: Long,
                    var logDate: String = null,
                    var logHour: String = null) {
  val d = new Date(ts)
  logDate = new SimpleDateFormat("yyyy-MM-dd").format(d)
  logHour = new SimpleDateFormat("HH").format(d)
}
