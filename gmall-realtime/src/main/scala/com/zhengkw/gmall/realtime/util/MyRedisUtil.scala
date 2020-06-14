package com.zhengkw.gmall.realtime.util

import com.zhengkw.common.util.PorpertiesUtil
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
 * @ClassName:RedisUtil
 * @author: zhengkw
 * @description:
 * @date: 20/05/29上午 11:16
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
object MyRedisUtil {
  private val conf: JedisPoolConfig = new JedisPoolConfig
  conf.setMaxTotal(100)
  conf.setMaxIdle(40)
  conf.setMinIdle(10)
  conf.setBlockWhenExhausted(true) // 忙碌的时候是否等待
  conf.setMaxWaitMillis(1000 * 60) // 最大等待时间
  conf.setTestOnBorrow(true) // 取客户端的时候, 是否做测试
  conf.setTestOnReturn(true)
  conf.setTestOnCreate(true)

  val host = PorpertiesUtil.getProperty("redis.host", "config.properties")
  val port = PorpertiesUtil.getProperty("redis.port", "config.properties").toInt
  private val pool = new JedisPool(conf, host, port)

  //def getClient = pool.getResource()
  def getClient = {
    val client = new Jedis(host, port, 60 * 1000)
    //必须要连接！
    client.connect()
    client
  }
}
