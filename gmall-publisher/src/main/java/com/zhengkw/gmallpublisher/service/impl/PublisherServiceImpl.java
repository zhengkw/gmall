package com.zhengkw.gmallpublisher.service.impl;

import com.zhengkw.gmallpublisher.mapper.DauMapper;
import com.zhengkw.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName:PublisherServiceImpl
 * @author: zhengkw
 * @description: 日活接口实现类
 * @date: 20/05/29下午 11:23
 * @version:1.0
 * @since: jdk 1.8
 */
@Component("PublisherServiceImpl")
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    DauMapper dau;

    @Override
    public Long showDau(String date) {
        return dau.showDau(date);
    }
}
