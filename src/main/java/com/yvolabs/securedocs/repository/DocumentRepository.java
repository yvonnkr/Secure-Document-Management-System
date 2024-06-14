package com.yvolabs.securedocs.repository;

import com.yvolabs.securedocs.dto.api.IDocument;
import com.yvolabs.securedocs.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static com.yvolabs.securedocs.constant.SqlConstants.*;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 13/06/2024
 */

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    @Query(countQuery = SELECT_COUNT_DOCUMENTS_QUERY, value = SELECT_DOCUMENTS_QUERY, nativeQuery = true)
    Page<IDocument> findDocuments(Pageable pageable);

    @Query(countQuery = SELECT_COUNT_DOCUMENTS_BY_NAME_QUERY, value = SELECT_DOCUMENTS_BY_NAME_QUERY, nativeQuery = true)
    Page<IDocument> findDocumentsByName(@Param("documentName") String documentName, Pageable pageable);

    @Query(value = SELECT_DOCUMENT_BY_DOCUMENT_ID_QUERY, nativeQuery = true)
    Optional<IDocument> findDocumentByDocumentId(String documentId); //instead of @param we use ?1 = 1=position of the param

    Optional<DocumentEntity> findByDocumentId(String documentId);
}
