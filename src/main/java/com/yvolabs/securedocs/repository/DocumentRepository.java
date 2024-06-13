package com.yvolabs.securedocs.repository;

import com.yvolabs.securedocs.dto.api.IDocument;
import com.yvolabs.securedocs.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import static com.yvolabs.securedocs.constant.SqlConstants.SELECT_COUNT_DOCUMENTS_QUERY;
import static com.yvolabs.securedocs.constant.SqlConstants.SELECT_DOCUMENTS_QUERY;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 13/06/2024
 */

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    @Query(countQuery = SELECT_COUNT_DOCUMENTS_QUERY, value = SELECT_DOCUMENTS_QUERY, nativeQuery = true)
    Page<IDocument> findDocuments(Pageable pageable);
}
