package com.yvolabs.securedocs.service.impl;

import com.yvolabs.securedocs.dto.Document;
import com.yvolabs.securedocs.dto.api.IDocument;
import com.yvolabs.securedocs.entity.DocumentEntity;
import com.yvolabs.securedocs.entity.UserEntity;
import com.yvolabs.securedocs.exception.ApiException;
import com.yvolabs.securedocs.repository.DocumentRepository;
import com.yvolabs.securedocs.repository.UserRepository;
import com.yvolabs.securedocs.service.DocumentService;
import com.yvolabs.securedocs.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.yvolabs.securedocs.constant.Constants.FILE_STORAGE_DIRECTORY;
import static com.yvolabs.securedocs.utils.DocumentUtils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 13/06/2024
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public Page<IDocument> getDocuments(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name"));
        return documentRepository.findDocuments(pageRequest);
    }

    @Override
    public Page<IDocument> getDocuments(int page, int size, String name) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name"));
        return documentRepository.findDocumentsByName(name, pageRequest);
    }

    @Override
    public Collection<Document> saveDocuments(String userId, List<MultipartFile> documents) {
        List<Document> newDocuments = new ArrayList<>();
        UserEntity userEntity = userRepository.findUserByUserId(userId).orElse(null);
        Path storage = Paths.get(FILE_STORAGE_DIRECTORY).toAbsolutePath().normalize();

        try {
            for (MultipartFile document : documents) {
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(document.getOriginalFilename()));
                if ("..".contains(fileName)) {
                    throw new ApiException(String.format("Invalid file name: %s", fileName));
                }
                DocumentEntity documentEntity = DocumentEntity.builder()
                        .documentId(UUID.randomUUID().toString())
                        .name(fileName)
                        .owner(userEntity)
                        .extension(getExtension(fileName))
                        .uri(getDocumentUri(fileName))
                        .size(document.getSize())
                        .formattedSize(byteCountToDisplaySize(document.getSize()))
                        .icon(setIcon(getExtension(fileName)))
                        .build();

                DocumentEntity savedDocumentEntity = documentRepository.save(documentEntity);

                // store file locally - in prod store in a cloud storage e.g. aws s3
                Files.copy(document.getInputStream(), storage.resolve(fileName), REPLACE_EXISTING);

                Document newDocument = fromDocumentEntity(
                        savedDocumentEntity,
                        userService.getUserById(savedDocumentEntity.getCreatedBy()),
                        userService.getUserById(savedDocumentEntity.getUpdatedBy())
                );

                newDocuments.add(newDocument);
            }

            return newDocuments;
        } catch (Exception exception) {
            throw new ApiException("Unable to save documents");

        }
    }


    @Override
    public IDocument updateDocument(String documentId, String name, String description) {
        try {
            DocumentEntity documentEntity = getDocumentEntity(documentId);

            // update physical file
            Path document = Paths.get(FILE_STORAGE_DIRECTORY).resolve(documentEntity.getName()).toAbsolutePath().normalize();
            Files.move(document, document.resolveSibling(name), REPLACE_EXISTING);

            // update entity
            documentEntity.setName(name);
            documentEntity.setDescription(description);
            documentRepository.save(documentEntity);

            return getDocumentByDocumentId(documentId);

        } catch (Exception exception) {
            throw new ApiException("Unable to update document");
        }
    }


    @Override
    public IDocument getDocumentByDocumentId(String documentId) {
        return documentRepository.findDocumentByDocumentId(documentId)
                .orElseThrow(() -> new ApiException("Document not found with document_id: " + documentId));
    }

    @Override
    public Resource getResource(String documentName) {
        try {
            Path filePath = Paths.get(FILE_STORAGE_DIRECTORY).toAbsolutePath().normalize().resolve(documentName);
            if (!Files.exists(filePath)) {
                throw new ApiException("Document not found with document_name: " + documentName);
            }
            return new UrlResource(filePath.toUri());

        } catch (Exception exception) {
            throw new ApiException("Unable to download resource");
        }
    }

    @Override
    public void deleteDocument(String documentId) {

    }

    private DocumentEntity getDocumentEntity(String documentId) {
        return documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new ApiException("Document not found with document_id: " + documentId));
    }
}
