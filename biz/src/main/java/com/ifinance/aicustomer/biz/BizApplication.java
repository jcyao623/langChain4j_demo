package com.ifinance.aicustomer.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 互联网金融智能客服系统启动类。
 */
@SpringBootApplication(scanBasePackages = "com.ifinance.aicustomer")
@MapperScan("com.ifinance.aicustomer.service.mapper")
public class BizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
