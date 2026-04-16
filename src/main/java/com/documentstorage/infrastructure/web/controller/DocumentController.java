package com.documentstorage.infrastructure.web.controller;

import com.documentstorage.application.dto.response.DocumentResponse;
import com.documentstorage.application.dto.response.PresignerUrlResponse;
import com.documentstorage.application.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestPart MultipartFile file,
            @RequestParam UUID ownerId) throws IOException {
        return ResponseEntity.status(201).body(service.upload(file, ownerId));
    }

    @GetMapping("/{id}/presigned-url")
    public ResponseEntity<PresignerUrlResponse> getUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getPresignedUrl(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
