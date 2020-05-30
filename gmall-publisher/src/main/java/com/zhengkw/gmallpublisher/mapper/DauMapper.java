package com.zhengkw.gmallpublisher.mapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * InterfaceName:DauMapper
 *
 * @author: zhengkw
 * @description: mybatismapper
 * @date: 20/05/29下午 11:50
 * version:
 * @since: jdk 1.8
 */

public interface DauMapper {
    Long showDau(String date);

    List<Map<String, Object>> showHourInfo(String date);
}
