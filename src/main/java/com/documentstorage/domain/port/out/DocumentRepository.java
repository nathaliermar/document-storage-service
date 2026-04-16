package com.documentstorage.domain.port.out;

import com.documentstorage.infrastructure.persistence.entity.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    Document save(Document doc);
    Optional<Document> findById(UUID id);
    void delete(Document doc);
    List<Document> findByOwnerId(UUID ownerId);
}
