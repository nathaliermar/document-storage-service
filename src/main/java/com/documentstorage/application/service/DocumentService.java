package com.documentstorage.application.service;

import com.documentstorage.application.dto.response.DocumentResponse;
import com.documentstorage.application.dto.response.PresignerUrlResponse;
import com.documentstorage.application.mapper.DocumentMapper;
import com.documentstorage.domain.port.out.DocumentRepository;
import com.documentstorage.infrastructure.persistence.entity.Document;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repository;
    private final StorageStrategy storageStrategy;
    private final DocumentMapper mapper;

    public DocumentResponse upload(MultipartFile file, UUID ownerId) throws IOException {
        String s3Key = buildKey(ownerId, file.getOriginalFilename());
        storageStrategy.upload(s3Key, file.getInputStream(), file.getSize(), file.getContentType());

        Document doc = Document.builder()
                .fileName(file.getOriginalFilename())
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .ownerId(ownerId)
                .build();

        try {
            storageStrategy.upload(s3Key, file.getInputStream(),
                    file.getSize(), file.getContentType());
            return toResponse(repository.save(doc));
        } catch (Exception e) {
            storageStrategy.delete(s3Key);
            throw new RuntimeException("Upload failed, storage rolled back", e);
        }
    }

    public PresignerUrlResponse getPresignedUrl(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
        String url = storageStrategy.generatePresignedUrl(doc.getS3Key(), Duration.ofMinutes(15));
        return new PresignerUrlResponse(url, Instant.now().plus(Duration.ofMinutes(15)));
    }

    public void delete(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
        storageStrategy.delete(doc.getS3Key());
        repository.delete(doc);
    }

    private DocumentResponse toResponse(Document doc) {
        return mapper.toResponse(doc);
    }

    private String buildKey(UUID ownerId, String fileName) {
        return "medical-request/" + ownerId + "/" + UUID.randomUUID() + "-" + fileName;
    }
}