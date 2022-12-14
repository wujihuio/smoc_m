package com.smoc.cloud.message.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置属性文件
 **/
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "com.manpuman.cloud.smoc.message")
public class MessageProperties {

    //手机号文件根目录
    private String mobileFileRootPath;

    //手机号条数限制
    private int mobileCountLimit;
}
