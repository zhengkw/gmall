package com.zhengkw.gmallpublisher.service;

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
}