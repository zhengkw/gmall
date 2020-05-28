package com.zhengkw.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @ClassName:PorpertiesUtil
 * @author: zhengkw
 * @description: properties文件读取工具类
 * @date: 20/05/28下午 8:03
 * @version:1.0
 * @since: jdk 1.8
 */
public class PorpertiesUtil {
    public static String value = "";
    private static Properties props;

    /**
     * @param propertyName 属性名
     * @param fileName     文件名
     * @descrption: 获取属性值
     * @return: java.lang.String
     * @date: 20/05/28 下午 8:27
     * @author: zhengkw
     */
    public static String getProperty(String propertyName, String fileName) {
        props = new Properties();
        InputStream in = null;
        try {
            //方式1
            //通过类加载器获取properties文件流
          //  in = PorpertiesUtil.class.getClassLoader().getResourceAsStream(fileName);
            //方式2
            //通过类进行获取properties文件流 必须加斜杠
           in = PorpertiesUtil.class.getResourceAsStream("/"+fileName);
            //加载文件
            props.load(in);
            //获取属性值！
            value = props.getProperty(propertyName);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
