package com.documentstorage.infrastructure.persistence.adapter;

import com.documentstorage.domain.port.out.DocumentRepository;
import com.documentstorage.infrastructure.persistence.entity.Document;
import com.documentstorage.infrastructure.persistence.repository.DocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final DocumentJpaRepository jpaRepository;

    @Override
    public Document save(Document doc) {
        return jpaRepository.save(doc);
    }

    @Override
    public Optional<Document> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void delete(Document doc) {
        jpaRepository.delete(doc);
    }

    @Override
    public List<Document> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId);
    }
}
