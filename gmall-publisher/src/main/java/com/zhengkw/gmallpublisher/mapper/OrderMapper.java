package com.zhengkw.gmallpublisher.mapper;

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
    Double getTotalAmount(String date);
}
