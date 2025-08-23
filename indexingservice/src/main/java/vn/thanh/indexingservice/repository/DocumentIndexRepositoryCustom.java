package vn.thanh.indexingservice.repository;

import java.util.List;

public interface DocumentIndexRepositoryCustom {
    void markDocumentsIndex(List<Long> indexIds, boolean value);
}
