package com.zhengkw.gmalllogger;

import com.alibaba.fastjson.JSONObject;
import com.zhengkw.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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
       sendToKafka(log);
        return "ok";
    }

    //利用spring提供的模板来操作kafka，配置放在application.xml里面！
    @Autowired
    private KafkaTemplate<String, String> kafka;

    /**
     * @param log
     * @descrption: 将log发送给kafka
     * @return: void
     * @date: 20/05/28 下午 2:05
     * @author: zhengkw
     */
    private void sendToKafka(String log) {
        //发送启动日志到kafka
        // 1. 写一个生产者
        // 2. 不同的日志发送不到不同的topic
        if (log.contains("startup")) {
            kafka.send(Constant.STARTUP_TOPIC, log);
        } else {
            kafka.send(Constant.EVENT_TOPIC, log);
        }
    }

    /**
     * @param log
     * @descrption: 将log日志落盘
     * @return: void
     * @date: 20/05/28 下午 2:05
     * @author: zhengkw
     */
    private void saveToLocalDisk(String log) {
        logger.info(log);
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
