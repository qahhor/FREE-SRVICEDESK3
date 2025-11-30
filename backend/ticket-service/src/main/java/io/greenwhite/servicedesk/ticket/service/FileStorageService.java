package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.ticket.config.FileStorageProperties;
import io.greenwhite.servicedesk.ticket.dto.AttachmentResponse;
import io.greenwhite.servicedesk.ticket.model.Attachment;
import io.greenwhite.servicedesk.ticket.model.Comment;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.AttachmentRepository;
import io.greenwhite.servicedesk.ticket.repository.CommentRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for file storage and attachment management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store file for a ticket
     */
    @Transactional
    public AttachmentResponse storeFileForTicket(MultipartFile file, UUID ticketId, UUID userId) {
        log.info("Storing file for ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return storeFile(file, ticket, null, user);
    }

    /**
     * Store file for a comment
     */
    @Transactional
    public AttachmentResponse storeFileForComment(MultipartFile file, UUID commentId, UUID userId) {
        log.info("Storing file for comment: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return storeFile(file, null, comment, user);
    }

    /**
     * Store file to local filesystem
     */
    private AttachmentResponse storeFile(MultipartFile file, Ticket ticket, Comment comment, User user) {
        // Validate file
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Check if the file's name contains invalid characters
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFilename);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create attachment record
            Attachment attachment = Attachment.builder()
                .ticket(ticket)
                .comment(comment)
                .uploadedBy(user)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(targetLocation.toString())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storageType("LOCAL")
                .build();

            attachment = attachmentRepository.save(attachment);

            log.info("File stored successfully: {}", storedFilename);
            return mapToResponse(attachment);

        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));

        try {
            Path filePath = Paths.get(attachment.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + attachment.getStoredFilename());
            }
        } catch (MalformedURLException ex) {
            log.error("File not found: {}", attachment.getStoredFilename(), ex);
            throw new RuntimeException("File not found: " + attachment.getStoredFilename(), ex);
        }
    }

    /**
     * Get attachments for a ticket
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getTicketAttachments(UUID ticketId) {
        return attachmentRepository.findByTicketId(ticketId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get attachments for a comment
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getCommentAttachments(UUID commentId) {
        return attachmentRepository.findByCommentId(commentId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Delete attachment
     */
    @Transactional
    public void deleteAttachment(UUID attachmentId, UUID userId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));

        // Check if user is the uploader or has permission
        if (!attachment.getUploadedBy().getId().equals(userId)) {
            // TODO: Check if user has ADMIN role
            throw new RuntimeException("You can only delete your own attachments");
        }

        // Soft delete
        attachment.setDeleted(true);
        attachmentRepository.save(attachment);

        // TODO: Delete physical file (optional, can be done by cleanup job)
        log.info("Attachment deleted: {}", attachmentId);
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        // Check file size
        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new RuntimeException(String.format("File size exceeds maximum limit of %d bytes",
                    fileStorageProperties.getMaxFileSize()));
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);
        List<String> allowedExtensions = Arrays.asList(
                fileStorageProperties.getAllowedExtensions().split(","));

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new RuntimeException(String.format("File type '%s' is not allowed. Allowed types: %s",
                    extension, fileStorageProperties.getAllowedExtensions()));
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "";
    }

    /**
     * Map Attachment entity to AttachmentResponse DTO
     */
    private AttachmentResponse mapToResponse(Attachment attachment) {
        return AttachmentResponse.builder()
            .id(attachment.getId())
            .ticketId(attachment.getTicket() != null ? attachment.getTicket().getId() : null)
            .commentId(attachment.getComment() != null ? attachment.getComment().getId() : null)
            .uploadedBy(attachment.getUploadedBy().getId())
            .uploaderName(attachment.getUploadedBy().getFullName())
            .originalFilename(attachment.getOriginalFilename())
            .storedFilename(attachment.getStoredFilename())
            .contentType(attachment.getContentType())
            .fileSize(attachment.getFileSize())
            .formattedFileSize(attachment.getFormattedFileSize())
            .downloadUrl(fileStorageProperties.getBaseDownloadUrl() + "/" + attachment.getId() + "/download")
            .isImage(attachment.isImage())
            .storageType(attachment.getStorageType())
            .createdAt(attachment.getCreatedAt())
            .build();
    }
}
