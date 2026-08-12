package com.ifinance.aicustomer.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 互联网金融智能客服系统启动类。
 */
@SpringBootApplication(scanBasePackages = "com.ifinance.aicustomer")
@EnableJpaRepositories(basePackages = "com.ifinance.aicustomer.service.repository")
@EntityScan(basePackages = "com.ifinance.aicustomer.service.entity")
public class BizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
