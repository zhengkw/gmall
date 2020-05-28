package com.zhengkw.gmalllogger;

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
    @PostMapping("/log")
    private String getLogMsg(@RequestParam("log") String log) {
        //对log进行解析

        //添加时间戳
        //落盘
        //send给kafka

        return "ok";
    }
}
