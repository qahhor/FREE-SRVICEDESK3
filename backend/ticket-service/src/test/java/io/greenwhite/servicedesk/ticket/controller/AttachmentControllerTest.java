package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.ticket.dto.AttachmentResponse;
import io.greenwhite.servicedesk.ticket.service.FileStorageService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AttachmentController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttachmentController Tests")
class AttachmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AttachmentController attachmentController;

    private UUID ticketId;
    private UUID commentId;
    private UUID attachmentId;
    private AttachmentResponse testAttachment;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        testAttachment = AttachmentResponse.builder()
                .id(attachmentId)
                .ticketId(ticketId)
                .originalFilename("test.txt")
                .storedFilename("stored-test.txt")
                .contentType("text/plain")
                .fileSize(100L)
                .formattedFileSize("100 B")
                .downloadUrl("/api/v1/attachments/" + attachmentId + "/download")
                .isImage(false)
                .storageType("LOCAL")
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(attachmentController).build();
    }

    @Test
    @DisplayName("Should get ticket attachments")
    void shouldGetTicketAttachments() throws Exception {
        // Given
        when(fileStorageService.getTicketAttachments(ticketId))
                .thenReturn(Arrays.asList(testAttachment));

        // When & Then
        mockMvc.perform(get("/api/v1/tickets/{ticketId}/attachments", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].originalFilename").value("test.txt"));
    }

    @Test
    @DisplayName("Should get comment attachments")
    void shouldGetCommentAttachments() throws Exception {
        // Given
        when(fileStorageService.getCommentAttachments(commentId))
                .thenReturn(Arrays.asList(testAttachment));

        // When & Then
        mockMvc.perform(get("/api/v1/comments/{commentId}/attachments", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].originalFilename").value("test.txt"));
    }
}
