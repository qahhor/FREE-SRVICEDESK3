package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.ticket.dto.CommentResponse;
import io.greenwhite.servicedesk.ticket.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CommentController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController Tests")
class CommentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private UUID ticketId;
    private UUID commentId;
    private CommentResponse testCommentResponse;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        testCommentResponse = CommentResponse.builder()
                .id(commentId)
                .ticketId(ticketId)
                .ticketNumber("TEST-1")
                .content("Test comment content")
                .isInternal(false)
                .isAutomated(false)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should get comments for ticket")
    void shouldGetCommentsForTicket() throws Exception {
        // Given
        when(commentService.getCommentsByTicketId(eq(ticketId), eq(true)))
                .thenReturn(Arrays.asList(testCommentResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/tickets/{ticketId}/comments", ticketId)
                        .param("includeInternal", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].content").value("Test comment content"));
    }

    @Test
    @DisplayName("Should get comment by ID")
    void shouldGetCommentById() throws Exception {
        // Given
        when(commentService.getCommentById(commentId)).thenReturn(testCommentResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId.toString()));
    }

    @Test
    @DisplayName("Should get comment count")
    void shouldGetCommentCount() throws Exception {
        // Given
        when(commentService.countComments(ticketId)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/tickets/{ticketId}/comments/count", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }
}
