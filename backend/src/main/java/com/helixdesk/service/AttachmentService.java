package com.helixdesk.service;

import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.TicketAttachment;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketService ticketService;
    private final Path uploadDir;

    public AttachmentService(
            TicketAttachmentRepository attachmentRepository,
            TicketService ticketService,
            @Value("${helixdesk.uploads.dir:./uploads}") String uploadDir
    ) {
        this.attachmentRepository = attachmentRepository;
        this.ticketService = ticketService;
        this.uploadDir = Path.of(uploadDir);
    }

    public List<TicketAttachment> list(Long ticketId, UserAccount actor) {
        Ticket ticket = ticketService.getVisible(ticketId, actor);
        return attachmentRepository.findByTicketIdOrderByCreatedAtDesc(ticket.getId());
    }

    @Transactional
    public TicketAttachment store(Long ticketId, UserAccount actor, MultipartFile file) {
        Ticket ticket = ticketService.getVisible(ticketId, actor);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        try {
            Files.createDirectories(uploadDir);
            String stored = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path target = uploadDir.resolve(stored);
            file.transferTo(target);
            TicketAttachment attachment = new TicketAttachment();
            attachment.setTicket(ticket);
            attachment.setUploadedBy(actor);
            attachment.setOriginalFilename(file.getOriginalFilename());
            attachment.setStoredPath(target.toString());
            attachment.setContentType(file.getContentType());
            attachment.setSizeBytes(file.getSize());
            return attachmentRepository.save(attachment);
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store attachment");
        }
    }
}
