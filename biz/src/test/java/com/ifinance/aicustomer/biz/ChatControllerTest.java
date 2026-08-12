package com.ifinance.aicustomer.biz;

import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void shouldReturnChatResponse() throws Exception {
        ChatResponse response = new ChatResponse(
                "session-1", "您好，请问有什么可以帮您？",
                LocalDateTime.of(2026, 8, 12, 10, 0), "qwen-plus");
        when(chatService.chat(any(ChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"你好\",\"sessionId\":\"session-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sessionId").value("session-1"))
                .andExpect(jsonPath("$.data.message").value("您好，请问有什么可以帮您？"));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnHistory() throws Exception {
        ChatMessageRecord record = new ChatMessageRecord(
                1L, "session-1", ChatRole.USER, "你好",
                null, LocalDateTime.of(2026, 8, 12, 10, 0));
        when(chatService.history("session-1")).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/chat/history").param("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].content").value("你好"));
    }
}
