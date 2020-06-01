package com.zhengkw.gmallpublisher.mapper;

import java.util.List;
import java.util.Map;

/**
 * InterfaceName:OrderMapper
 *
 * @author: zhengkw
 * @description: 订单mapper
 * @date: 20/06/01下午 2:14
 * version:
 * @since: jdk 1.8
 */
public interface OrderMapper {
    //获取当日销售额
    Double getTotalAmount(String date);

    //获得小时销售额明细
    List<Map<String, Object>> getHourAmount(String date);
}
