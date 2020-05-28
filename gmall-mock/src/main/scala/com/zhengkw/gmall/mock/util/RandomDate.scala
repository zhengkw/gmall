package com.zhengkw.gmall.mock.util

import java.util.Date

/**
 * @ClassName:RandomDate
 * @author: zhengkw
 * @description:
 * @date: 20/05/28上午 11:55
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object RandomDate {
  def apply(startDate: Date, stopDate: Date, step: Int) = {
    val randomDate = new RandomDate
    val avgStepTime = (stopDate.getTime - startDate.getTime) / step
    randomDate.maxStepTime = 4 * avgStepTime
    randomDate.lastDateTIme = startDate.getTime
    randomDate
  }
}

class RandomDate {
  // 上次 action 的时间
  var lastDateTIme: Long = _
  // 每次最大的步长时间
  var maxStepTime: Long = _

  /**
   * 得到一个随机时间
   *
   * @return
   */
  def getRandomDate = {
    // 这次操作的相比上次的步长
    val timeStep = RandomNumUtil.randomLong(0, maxStepTime)
    lastDateTIme += timeStep
    new Date(lastDateTIme)
  }

}
