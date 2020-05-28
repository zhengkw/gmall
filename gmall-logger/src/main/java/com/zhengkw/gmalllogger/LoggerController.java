package com.zhengkw.gmalllogger;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName:LoggerController
 * @author: zhengkw
 * @description: 日志服务器
 * @date: 20/05/28下午 1:46
 * @version:1.0
 * @since: jdk 1.8
 */
@RestController
public class LoggerController {
    private Logger logger = LoggerFactory.getLogger(LoggerController.class);

    @PostMapping("/log")
    private String doLog(@RequestParam("log") String log) {
        //添加时间戳
        log = addTs(log);
        //  System.out.println(log);
        //落盘
        saveToLocalDisk(log);
        //send给kafka
        //  sendToKafka(log);
        return "ok";
    }

    /**
     * @param log
     * @descrption: 将log发送给kafka
     * @return: void
     * @date: 20/05/28 下午 2:05
     * @author: zhengkw
     */
    private void sendToKafka(String log) {
        logger.info(log);
    }

    /**
     * @param log
     * @descrption: 将log日志落盘
     * @return: void
     * @date: 20/05/28 下午 2:05
     * @author: zhengkw
     */
    private void saveToLocalDisk(String log) {

    }

    /**
     * @param log
     * @descrption: 给log日志添加一个服务器时间戳
     * @return: java.lang.String
     * @date: 20/05/28 下午 2:06
     * @author: zhengkw
     */
    private String addTs(String log) {
        JSONObject jsonObject = JSONObject.parseObject(log);
        jsonObject.put("ts", System.currentTimeMillis());
        log = jsonObject.toJSONString();
        return log;
    }
}
