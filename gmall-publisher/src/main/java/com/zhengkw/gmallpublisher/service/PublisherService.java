package com.zhengkw.gmallpublisher.service;

import java.util.List;
import java.util.Map;

/**
 * InterfaceName:PublisherService
 *
 * @author: zhengkw
 * @description: 日活统计接口
 * @date: 20/05/29下午 11:21
 * version:
 * @since: jdk 1.8
 */
public interface PublisherService {
    Long showDau(String date);

    //日活小时明细
    Map<String, Long> showHourInfo(String date);

    //获取当天的销售总额
    Double getTotalAmount(String date);

    //获取当天的每小时销售额明细
    Map<String, Double> getHourAmount(String date);
}
