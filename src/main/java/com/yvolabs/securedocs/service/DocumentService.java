package com.yvolabs.securedocs.service;

import com.yvolabs.securedocs.dto.Document;
import com.yvolabs.securedocs.dto.api.IDocument;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 13/06/2024
 */

public interface DocumentService {
    Page<IDocument> getDocuments(int page, int size);

    Page<IDocument> getDocuments(int page, int size, String name);

    Collection<Document> saveDocuments(String userId, List<MultipartFile> documents);

    IDocument updateDocument(String documentId, String name, String description);

    void deleteDocument(String documentId);

    IDocument getDocumentByDocumentId(String documentId);

    Resource getResource(String documentName);
}
