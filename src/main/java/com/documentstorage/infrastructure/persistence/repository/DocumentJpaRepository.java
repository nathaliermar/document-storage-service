package com.documentstorage.infrastructure.persistence.repository;

import com.documentstorage.infrastructure.persistence.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentJpaRepository extends JpaRepository<Document, UUID> {
    List<Document> findByOwnerId(UUID ownerId);
}
