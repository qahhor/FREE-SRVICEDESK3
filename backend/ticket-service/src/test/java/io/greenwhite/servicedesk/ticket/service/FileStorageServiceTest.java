package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.ticket.config.FileStorageProperties;
import io.greenwhite.servicedesk.ticket.dto.AttachmentResponse;
import io.greenwhite.servicedesk.ticket.model.Attachment;
import io.greenwhite.servicedesk.ticket.model.Project;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.AttachmentRepository;
import io.greenwhite.servicedesk.ticket.repository.CommentRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileStorageService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FileStorageService Tests")
class FileStorageServiceTest {

    @Mock
    private FileStorageProperties fileStorageProperties;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private FileStorageService fileStorageService;

    private Path tempDir;
    private UUID ticketId;
    private UUID userId;
    private UUID attachmentId;
    private Ticket testTicket;
    private User testUser;
    private Project testProject;
    private Attachment testAttachment;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("file-storage-test");

        ticketId = UUID.randomUUID();
        userId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        testProject = Project.builder()
                .key("TEST")
                .name("Test Project")
                .build();
        testProject.setId(UUID.randomUUID());

        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.AGENT)
                .active(true)
                .build();
        testUser.setId(userId);

        testTicket = Ticket.builder()
                .subject("Test Ticket")
                .description("Test Description")
                .project(testProject)
                .requester(testUser)
                .build();
        testTicket.setId(ticketId);

        testAttachment = Attachment.builder()
                .ticket(testTicket)
                .uploadedBy(testUser)
                .originalFilename("test.txt")
                .storedFilename("stored-test.txt")
                .filePath(tempDir.resolve("stored-test.txt").toString())
                .contentType("text/plain")
                .fileSize(100L)
                .storageType("LOCAL")
                .build();
        testAttachment.setId(attachmentId);

        // Setup mocks for constructor
        when(fileStorageProperties.getUploadDir()).thenReturn(tempDir.toString());
        when(fileStorageProperties.getMaxFileSize()).thenReturn(10485760L);
        when(fileStorageProperties.getAllowedExtensions()).thenReturn("txt,pdf,jpg,png");
        when(fileStorageProperties.getBaseDownloadUrl()).thenReturn("/api/v1/attachments");

        fileStorageService = new FileStorageService(
            fileStorageProperties,
            attachmentRepository,
            ticketRepository,
            commentRepository,
            userRepository
        );
        // Initialize the service (calls @PostConstruct)
        fileStorageService.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        Files.walk(tempDir)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // ignore
                }
            });
    }

    @Test
    @DisplayName("Should store file for ticket successfully")
    void shouldStoreFileForTicketSuccessfully() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Hello World".getBytes()
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(testAttachment);

        // When
        AttachmentResponse response = fileStorageService.storeFileForTicket(file, ticketId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOriginalFilename()).isEqualTo("test.txt");
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    @DisplayName("Should throw exception for empty file")
    void shouldThrowExceptionForEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> fileStorageService.storeFileForTicket(emptyFile, ticketId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot upload empty file");
    }

    @Test
    @DisplayName("Should throw exception for file exceeding max size")
    void shouldThrowExceptionForFileTooLarge() {
        // Given
        when(fileStorageProperties.getMaxFileSize()).thenReturn(10L); // 10 bytes max
        
        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large.txt",
            "text/plain",
            "This is a very long content that exceeds the limit".getBytes()
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> fileStorageService.storeFileForTicket(largeFile, ticketId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File size exceeds");
    }

    @Test
    @DisplayName("Should throw exception for invalid file extension")
    void shouldThrowExceptionForInvalidExtension() {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "test.exe",
            "application/octet-stream",
            "content".getBytes()
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> fileStorageService.storeFileForTicket(invalidFile, ticketId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("Should get ticket attachments")
    void shouldGetTicketAttachments() {
        // Given
        when(attachmentRepository.findByTicketId(ticketId)).thenReturn(Arrays.asList(testAttachment));

        // When
        List<AttachmentResponse> attachments = fileStorageService.getTicketAttachments(ticketId);

        // Then
        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getOriginalFilename()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("Should delete attachment successfully")
    void shouldDeleteAttachmentSuccessfully() {
        // Given
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(testAttachment));

        // When
        fileStorageService.deleteAttachment(attachmentId, userId);

        // Then
        assertThat(testAttachment.getDeleted()).isTrue();
        verify(attachmentRepository).save(testAttachment);
    }

    @Test
    @DisplayName("Should throw exception when deleting attachment by non-uploader")
    void shouldThrowExceptionWhenDeletingByNonUploader() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(testAttachment));

        // When & Then
        assertThatThrownBy(() -> fileStorageService.deleteAttachment(attachmentId, differentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only delete your own attachments");

        verify(attachmentRepository, never()).save(any());
    }
}
