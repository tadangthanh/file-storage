package vn.thanh.permissionservice.service;

import vn.thanh.permissionservice.entity.DocumentCategoryMap;

import java.util.List;

public interface IDocumentCategoryMapService {
    void deleteAllByDocumentIds(List<Long> documentIds);

    DocumentCategoryMap add(Long documentId, Long categoryId);

    Long getCategoryByDocumentId(Long documentId);
}
