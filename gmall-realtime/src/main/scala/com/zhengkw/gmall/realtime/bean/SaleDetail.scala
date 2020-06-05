package com.zhengkw.gmall.realtime.bean

/**
 * @ClassName:SaleDetail
 * @author: zhengkw
 * @description:
 * @date: 20/06/06上午 12:31
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
case class SaleDetail(var order_detail_id: String = null,
                      var order_id: String = null,
                      var order_status: String = null,
                      var create_time: String = null,
                      var user_id: String = null,
                      var sku_id: String = null,
                      var user_gender: String = null,
                      var user_age: Int = 0,
                      var user_level: String = null,
                      var sku_price: Double = 0D,
                      var sku_name: String = null,
                      var dt: String = null)
