package com.zhengkw.gmall.realtime.bean

/**
 * @ClassName:OrderDetail
 * @author: zhengkw
 * @description:
 * @date: 20/06/06上午 12:30
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */

case class OrderDetail(id: String,
                       order_id: String,
                       sku_name: String,
                       sku_id: String,
                       order_price: String,
                       img_url: String,
                       sku_num: String)
