package com.zhengkw.gmall.realtime.bean

/**
 * @ClassName:OrderInfo
 * @author: zhengkw
 * @description:
 * @date: 20/06/01上午 8:58
 * @version:1.0
 * @since: jdk 1.8 scala 2.11.8
 */
case class OrderInfo(id: String,
                     province_id: String,
                     var consignee: String,
                     order_comment: String,
                     var consignee_tel: String,
                     order_status: String,
                     payment_way: String,
                     user_id: String,
                     img_url: String,
                     total_amount: Double,
                     expire_time: String,
                     delivery_address: String,
                     create_time: String,
                     operate_time: String,
                     tracking_no: String,
                     parent_order_id: String,
                     out_trade_no: String,
                     trade_body: String,
                     var create_date: String = null,
                     var create_hour: String = null) {

  //脱敏数据  人名  电话
  consignee = consignee.substring(0, 1) + printStar(consignee.length - 2)
  //一个圆括号就一个捕获组  $1代表捕获组1 左边这个！
  consignee_tel = consignee_tel.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")
  //create_time  2020-06-01 07:41:29
  create_date = create_time.substring(0, 10)
  create_hour = create_time.substring(create_time.length - 9, create_time.length - 6)


  def printStar(len: Int) = {
    print("*" * len)
  }
}



