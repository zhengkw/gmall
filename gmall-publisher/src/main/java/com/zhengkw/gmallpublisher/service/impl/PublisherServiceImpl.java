package com.zhengkw.gmallpublisher.service.impl;

import com.zhengkw.gmallpublisher.mapper.DauMapper;
import com.zhengkw.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:PublisherServiceImpl
 * @author: zhengkw
 * @description: 日活接口实现类
 * @date: 20/05/29下午 11:23
 * @version:1.0
 * @since: jdk 1.8
 */
@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    DauMapper dau;

    /**
     * @param date
     * @descrption: 日活
     * @return: java.lang.Long
     * @date: 20/05/30 上午 10:56
     * @author: zhengkw
     */
    @Override
    public Long showDau(String date) {
        return dau.showDau(date);
    }

    /**
     * @param date
     * @descrption: 小时明细
     * @return: java.util.Map<java.lang.String, java.lang.Long>
     * @date: 20/05/30 上午 10:56
     * @author: zhengkw
     */
    @Override
    public Map<String, Long> showHourInfo(String date) {
        Map<String, Long> result = new HashMap<>();
        List<Map<String, Object>> list = dau.showHourInfo(date);
        for (Map<String, Object> map : list) {
            String key = (String) map.get("LOGHOUR");
            Long value = (Long) map.get("COUNT");
            result.put(key, value);
        }
        return result;
    }


}
