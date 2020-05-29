package com.zhengkw.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.zhengkw.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:PublisherController
 * @author: zhengkw
 * @description: 日活控制层
 * [{"id":"dau","name":"新增日活","value":1200},
 * {"id":"new_mid","name":"新增设备","value":233} ]
 * @date: 20/05/29下午 11:24
 * @version:1.0
 * @since: jdk 1.8
 */
@RestController
public class PublisherController {
    @Autowired
    private PublisherService service;

    //  http://localhost:8070/realtime-total?date=2020-05-29
    @GetMapping("/realtime-total")
    public String showDau(@RequestParam("date") String date) {
        Long dau = service.showDau(date);
/*[{"id":"dau","name":"新增日活","value":1200},
{"id":"new_mid","name":"新增设备","value":233} ]
*/

        ArrayList<Map<String, String>> result = new ArrayList<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("id", "dau");
        map1.put("name", "新增日活");
        map1.put("value", dau.toString());
        result.add(map1);

        Map<String, String> map2 = new HashMap<>();
        map2.put("id", "new_mid");
        map2.put("name", "新增设备");
        map2.put("value", "233");
        result.add(map2);
        return JSON.toJSONString(result);


    }
}
