package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.enums.ChannelType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.dto.CreateTicketRequest;
import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import io.greenwhite.servicedesk.ticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for TicketController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController Tests")
class TicketControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicketSuccessfully() throws Exception {
        // Given
        UUID projectId = UUID.randomUUID();
        CreateTicketRequest request = new CreateTicketRequest();
        request.setSubject("Test Ticket");
        request.setDescription("Test Description");
        request.setPriority(TicketPriority.HIGH);
        request.setChannel(ChannelType.WEB_FORM);
        request.setProjectId(projectId);

        TicketDTO response = TicketDTO.builder()
                .id(UUID.randomUUID())
                .ticketNumber("TEST-1")
                .subject("Test Ticket")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.HIGH)
                .build();

        when(ticketService.createTicket(any(CreateTicketRequest.class), any()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value("TEST-1"))
                .andExpect(jsonPath("$.data.subject").value("Test Ticket"));
    }

    @Test
    @DisplayName("Should get all tickets successfully")
    void shouldGetAllTicketsSuccessfully() throws Exception {
        // Given
        TicketDTO ticket = TicketDTO.builder()
                .id(UUID.randomUUID())
                .ticketNumber("TEST-1")
                .subject("Test Ticket")
                .build();

        Page<TicketDTO> page = new PageImpl<>(Collections.singletonList(ticket), PageRequest.of(0, 20), 1);
        when(ticketService.getAllTickets(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/tickets")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].ticketNumber").value("TEST-1"));
    }

    @Test
    @DisplayName("Should get ticket by ID successfully")
    void shouldGetTicketByIdSuccessfully() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();
        TicketDTO ticket = TicketDTO.builder()
                .id(ticketId)
                .ticketNumber("TEST-1")
                .subject("Test Ticket")
                .build();

        when(ticketService.getTicket(ticketId)).thenReturn(ticket);

        // When & Then
        mockMvc.perform(get("/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(ticketId.toString()));
    }

    @Test
    @DisplayName("Should update ticket status successfully")
    void shouldUpdateTicketStatusSuccessfully() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();
        TicketDTO updatedTicket = TicketDTO.builder()
                .id(ticketId)
                .status(TicketStatus.RESOLVED)
                .build();

        when(ticketService.updateTicketStatus(eq(ticketId), eq(TicketStatus.RESOLVED)))
                .thenReturn(updatedTicket);

        // When & Then
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("Should get ticket by number successfully")
    void shouldGetTicketByNumberSuccessfully() throws Exception {
        // Given
        String ticketNumber = "TEST-1";
        TicketDTO ticket = TicketDTO.builder()
                .id(UUID.randomUUID())
                .ticketNumber(ticketNumber)
                .subject("Test Ticket")
                .build();

        when(ticketService.getTicketByNumber(ticketNumber)).thenReturn(ticket);

        // When & Then
        mockMvc.perform(get("/tickets/number/" + ticketNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value(ticketNumber));
    }

    @Test
    @DisplayName("Should assign ticket successfully")
    void shouldAssignTicketSuccessfully() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        TicketDTO assignedTicket = TicketDTO.builder()
                .id(ticketId)
                .assigneeId(assigneeId)
                .status(TicketStatus.OPEN)
                .build();

        when(ticketService.assignTicket(eq(ticketId), eq(assigneeId)))
                .thenReturn(assignedTicket);

        // When & Then
        mockMvc.perform(patch("/tickets/" + ticketId + "/assign")
                        .param("assigneeId", assigneeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assigneeId").value(assigneeId.toString()));
    }
}
