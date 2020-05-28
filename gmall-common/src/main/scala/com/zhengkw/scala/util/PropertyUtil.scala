package com.zhengkw.scala.util

import java.util.Properties

/**
 * @descrption: 属性文件读取
 * @return:
 * @date: 20/05/28 下午 8:49
 * @author: zhengkw
 */
object PropertyUtil {

  /**
   * 属性文件
   *
   * @param fileName     属性文件
   * @param propertyName 属性名
   */
  def getProperty(fileName: String, propertyName: String) = {
    // 1. 读取文件内容
    val is = PropertyUtil.getClass.getClassLoader.getResourceAsStream(fileName)
    val properties = new Properties()
    properties.load(is)
    // 2. 根据属性名得到属性值
    properties.getProperty(propertyName)
  }
}
