package com.ifinance.aicustomer.biz;

import com.ifinance.aicustomer.service.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class BizApplicationTests {

    @Autowired
    private ChatService chatService;

    @Test
    void contextLoads() {
        assertNotNull(chatService);
    }
}
