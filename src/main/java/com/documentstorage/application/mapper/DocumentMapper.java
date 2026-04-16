package com.documentstorage.application.mapper;

import com.documentstorage.application.dto.response.DocumentResponse;
import com.documentstorage.infrastructure.persistence.entity.Document;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    DocumentResponse toResponse(Document document);
}
